package com.example.mybatisparser;

import com.example.mybatisparser.entity.JavaInfoEntity;
import com.example.mybatisparser.entity.NodeEntity;
import com.example.mybatisparser.entity.TableViewEntity;
import com.example.mybatisparser.entity.XmlEntity;
import com.example.mybatisparser.recode.JavaNodeRecord;
import com.example.mybatisparser.recode.JavaNodeTableRecord;
import com.example.mybatisparser.repository.JavaInfoRepository;
import com.example.mybatisparser.repository.NodeRepository;
import com.example.mybatisparser.repository.TableViewRepository;
import com.example.mybatisparser.repository.XmlRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class JavaInfoProcess {

    private final JavaInfoRepository javaInfoRepository;
    private final NodeRepository nodeRepository;
    private final XmlRepository xmlRepository;
    private final TableViewRepository tableViewRepository;

    public void process() {

        List<String> distinctServiceNames = xmlRepository.findDistinctServiceNames();
        log.info("distinctServiceNames : {}", distinctServiceNames);

        distinctServiceNames.forEach(serviceName -> {
            List<XmlEntity> xmlEntities = xmlRepository.findByServiceNameAndMapperTypeIn(
                    serviceName,
                    List.of("insert", "update", "delete")
            );
            xmlEntities.forEach(xmlEntity -> {
                String mapperId = xmlEntity.getMapperId();
                log.info("mapperId : {}", mapperId);
                Stream.concat(
                        javaInfoRepository.findByMethodCallsContainingAndServiceName("[" + mapperId + "]", xmlEntity.getServiceName()),
                        javaInfoRepository.findByMethodParametersContainingAndServiceName('"' + mapperId + '"', xmlEntity.getServiceName())
                ).map(javaInfo -> new JavaNodeRecord(
                        javaInfo,
                        xmlEntity,
                        List.of(javaInfo.getId().toString()
                        )
                )).forEach(this::extracted);
            });
        });
    }

    // 재귀호출
    private void extracted(JavaNodeRecord javaNodeRecord) {
        log.info("javaNodeRecord : {}", javaNodeRecord);
        List<JavaInfoEntity> nextJavaInfos = javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(
                javaNodeRecord.currentJavaInfoEntity().getMethodName(),
                javaNodeRecord.currentJavaInfoEntity().getClassName(),
                javaNodeRecord.xmlEntity().getServiceName());

        // 조회 값이 없는 경우 저장 후 완료
        if (nextJavaInfos.isEmpty()) {
            log.info("nextJavaInfos is empty");
            repositorySave(javaNodeRecord);
            return;
        }

        nextJavaInfos.forEach(nextJavaInfo -> {
            List<String> allIds = new ArrayList<>(javaNodeRecord.javaInfoIds());
            String nextInfoId = nextJavaInfo.getId().toString();

            // 자기 자신이 이미 포함 되어 있지 않은 경 우에만 재귀 호출
            if (!allIds.contains(nextInfoId)) {
                allIds.add(nextInfoId);
                extracted(new JavaNodeRecord(nextJavaInfo, javaNodeRecord.xmlEntity(), allIds));
            } else {
                log.info(">>>>>>>>>>>>>>> 포함 되어 있는 경우 저장 후 종료  >>>>>>>>>>>>>>>>>");
                repositorySave(javaNodeRecord);
            }
        });
    }

    private void repositorySave(JavaNodeRecord javaNodeRecord) {
        log.info("javaInfoIds : {}", javaNodeRecord.javaInfoIds());
        String firstId = javaNodeRecord.javaInfoIds().get(0);
        String lastId = javaNodeRecord.javaInfoIds().get(javaNodeRecord.javaInfoIds().size() - 1);
        nodeRepository.save(NodeEntity.builder()
                .ids(javaNodeRecord.javaInfoIds())
                .firstId(firstId)
                .lastId(lastId)
                .packageName(javaNodeRecord.currentJavaInfoEntity().getPackageName())
                .className(javaNodeRecord.currentJavaInfoEntity().getClassName())
                .methodName(javaNodeRecord.currentJavaInfoEntity().getMethodName())
                .serviceName(javaNodeRecord.currentJavaInfoEntity().getServiceName())
                .url(extractUrl(javaNodeRecord.currentJavaInfoEntity().getClassAnnotations(), javaNodeRecord.currentJavaInfoEntity().getMethodAnnotations()))
                .fileName(javaNodeRecord.currentJavaInfoEntity().getFileName())
                .build());
    }

    public String extractUrl(String classAnnotations, String methodAnnotations) {
        String baseUrl = ""; // 기본 URL 경로를 초기화

        // 클래스 어노테이션 파싱
        if (classAnnotations != null) {
            String[] classAnnotationsArray = classAnnotations.split(",");
            for (String annotation : classAnnotationsArray) {
                if (annotation.trim().startsWith("@RequestMapping")) {
                    // @RequestMapping 어노테이션을 찾았을 때, URL 경로를 추출
                    String[] parts = annotation.split("\"");
                    if (parts.length >= 2) {
                        baseUrl = parts[1];
                    }
                }
            }
        }

        // 메서드 어노테이션 파싱
        if (methodAnnotations != null) {
            String[] methodAnnotationsArray = methodAnnotations.split(",");
            for (String annotation : methodAnnotationsArray) {
                if (annotation.trim().startsWith("@RequestMapping")) {
                    // @RequestMapping 어노테이션을 찾았을 때, URL 경로를 추출
                    String[] parts = annotation.split("\"");
                    if (parts.length >= 2) {
                        baseUrl += parts[1]; // 클래스 URL과 메서드 URL을 연결
                    }
                }
            }
        }

        return baseUrl;
    }


    public final String TABLE_NAME_PATH = "./tables.csv";
    // 저장 경로
    public final String TARGET_PATH = "./result.csv";

    // 테이블 이름을 읽어서 csv 파일로 저장
    public void nodeCsv() {

        // 대상 테이블 이름 csv 파일에서 가져오기
        List<String> tablesNames = getTablesName();

        log.info("tablesNames : {}", tablesNames);

        Stream<JavaNodeTableRecord> javaNodeTableRecordStream = tablesNames.stream()
                .flatMap(
                        tableName ->
                                xmlRepository.findByMapperBodyContains(tableName)
                                        .peek(c -> log.info("{}", c))
                                        .filter(c -> !c.getMapperType().equals("select")) // select 문은 제외
                                        //.filter(c -> c.getMapperType().equals("insert") || c.getMapperType().equals("update") || c.getMapperType().equals("delete"))
                                        .peek(c -> log.info("{}", c.getMapperType()))
                                        .flatMap(xmlEntity -> {

                                                    String mapperId = xmlEntity.getMapperId();
                                                    log.info("mapperId : {}", mapperId);
                                                    String mapperType = xmlEntity.getMapperType();
                                                    log.info("mapperType : {}", mapperType);
                                                    String serviceName = xmlEntity.getServiceName();
                                                    log.info("serviceName : {}", serviceName);
                                                    return Stream.concat(
                                                                    javaInfoRepository.findByMethodCallsContainingAndServiceName("[" + mapperId + "]", xmlEntity.getServiceName()),
                                                                    javaInfoRepository.findByMethodParametersContainingAndServiceName('"' + mapperId + '"', xmlEntity.getServiceName())
                                                            )
                                                            .peek(javaInfoEntity -> log.info("javaInfoEntity : {}", javaInfoEntity))
                                                            .map(javaInfo -> new JavaNodeTableRecord(
                                                                    javaInfo,
                                                                    xmlEntity,
                                                                    List.of(javaInfo.getId().toString()),
                                                                    tableName
                                                            ));
                                                }
                                        )
                );


        List<JavaNodeTableRecord> list = javaNodeTableRecordStream.toList();

        log.info("list.size : {}", list.size());

//        List<String> mapperIdList = list.stream().map(c -> c.xmlEntity().getMapperId()).toList();
//        log.info("mapperIdList : {}", mapperIdList);

        extractedTable(list.get(0));

//        javaNodeTableRecordStream.forEach(this::extractedTable);

//        List<JavaNodeTableRecord> list = javaNodeTableRecordStream.toList();
//
//        log.info("list : {}", list);

        //        try {
//            writeToCsv(list);
//        } catch (IOException e) {
//            log.info("e : {}", e);
//            throw new RuntimeException(e);
//        }

    }


    private void extractedTable(JavaNodeTableRecord javaNodeTableRecord) {
        log.info("javaNodeTableRecord : {}", javaNodeTableRecord);
        List<JavaInfoEntity> nextJavaInfos = javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(
                javaNodeTableRecord.currentJavaInfoEntity().getMethodName(),
                javaNodeTableRecord.currentJavaInfoEntity().getClassName(),
                javaNodeTableRecord.xmlEntity().getServiceName());

        // 조회 값이 없는 경우 저장 후 완료
        if (nextJavaInfos.isEmpty()) {
            log.info("nextJavaInfos is empty");
            saveTableView(javaNodeTableRecord);
            return;
        }

        nextJavaInfos.forEach(nextJavaInfo -> {
            List<String> allIds = new ArrayList<>(javaNodeTableRecord.javaInfoIds());
            String nextInfoId = nextJavaInfo.getId().toString();

            // 자기 자신이 이미 포함되어 있지 않은 경우에만 재귀 호출
            if (!allIds.contains(nextInfoId)) {
                allIds.add(nextInfoId);
                extractedTable(new JavaNodeTableRecord(nextJavaInfo, javaNodeTableRecord.xmlEntity(), allIds, javaNodeTableRecord.tableName()));
            } else {
                log.info(">>>>>>>>>>>>>>> 포함 되어 있는 경우 저장 후 종료  >>>>>>>>>>>>>>>>>");
                saveTableView(javaNodeTableRecord);
            }
        });
    }

    private void saveTableView(JavaNodeTableRecord javaNodeTableRecord) {
        log.info("javaInfoIds : {}", javaNodeTableRecord.javaInfoIds());
        String firstId = javaNodeTableRecord.javaInfoIds().get(0);
        String lastId = javaNodeTableRecord.javaInfoIds().get(javaNodeTableRecord.javaInfoIds().size() - 1);
        tableViewRepository.save(TableViewEntity.builder()
                .tableName(javaNodeTableRecord.tableName())
                .mapperId(javaNodeTableRecord.xmlEntity().getMapperId())
                .mapperType(javaNodeTableRecord.xmlEntity().getMapperType())
                .serviceName(javaNodeTableRecord.xmlEntity().getServiceName())
                .className(javaNodeTableRecord.currentJavaInfoEntity().getClassName())
                .methodName(javaNodeTableRecord.currentJavaInfoEntity().getMethodName())
                .packageName(javaNodeTableRecord.currentJavaInfoEntity().getPackageName())
                .url(extractUrl(javaNodeTableRecord.currentJavaInfoEntity().getClassAnnotations(), javaNodeTableRecord.currentJavaInfoEntity().getMethodAnnotations()))
                .firstId(firstId)
                .lastId(lastId)
                .ids(javaNodeTableRecord.javaInfoIds().toString())
                .build());
    }

    private List<String> getTablesName() {

        try (Reader reader = new FileReader(TABLE_NAME_PATH)) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
            List<String> tableNames = new ArrayList<>();
            for (CSVRecord csvRecord : csvParser) {
                tableNames.add(csvRecord.get(0));
            }
            return tableNames;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

//    private void writeToCsv(List<CsvRecord> csvRecords) throws IOException {
//
//        List<String> header = Arrays.stream(CsvRecord.class.getDeclaredFields()).map(Field::getName).toList();
//
//        // 파일을 열어서 데이터를 추가하기 위해 FileWriter를 이용합니다.
//        try (FileWriter fileWriter = new FileWriter(TARGET_PATH, false);
//             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {
//
//            csvPrinter.printRecord(header);
//
//            // 데이터 레코드를 추가합니다.
//            for (CsvRecord csvRecord : csvRecords) {
//                csvPrinter.printRecord(
//                        csvRecord.mapperId(),
//                        csvRecord.mapperType(),
//                        csvRecord.nodeEntity().getId(),
//                        csvRecord.nodeEntity().getIds(),
//                        csvRecord.nodeEntity().getUrl(),
//                        csvRecord.nodeEntity().getServiceName(),
//                        csvRecord.nodeEntity().getClassName(),
//                        csvRecord.nodeEntity().getMethodName(),
//                        csvRecord.nodeEntity().getPackageName()
//                );
//            }
//        }
//    }
}
