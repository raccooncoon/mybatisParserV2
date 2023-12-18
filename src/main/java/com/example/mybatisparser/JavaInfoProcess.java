package com.example.mybatisparser;

import com.example.mybatisparser.entity.JavaInfoEntity;
import com.example.mybatisparser.entity.TableViewEntity;
import com.example.mybatisparser.entity.XmlEntity;
import com.example.mybatisparser.recode.JavaNodeTableRecord;
import com.example.mybatisparser.repository.JavaInfoRepository;
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
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class JavaInfoProcess {

    private final JavaInfoRepository javaInfoRepository;
    private final XmlRepository xmlRepository;
    private final TableViewRepository tableViewRepository;
    private static final Set<String> MAPPER_TYPES = Set.of("insert", "update", "delete");
    private static final String TABLE_NAME_PATH = "./tables.csv";

    public void nodeCsv() {
        // 대상 테이블 이름 csv 파일에서 가져오기
        List<String> tablesNames = getTablesName();
        log.info("tablesNames : {}", tablesNames);

        Stream<JavaNodeTableRecord> javaNodeTableRecordStream = tablesNames.stream()
                .flatMap(tableName ->
                        xmlRepository.findByMapperBodyContains(tableName)
                                .filter(c -> MAPPER_TYPES.contains(c.getMapperType()))
                                //.filter(c -> c.getServiceName().equals("coral-release"))
                                .flatMap(xmlEntity -> getJavaInfoEntityStream(xmlEntity)
                                        .map(javaInfo -> getJavaNodeTableRecord(tableName, xmlEntity, javaInfo))
                                )
                );

        List<JavaNodeTableRecord> list = javaNodeTableRecordStream.toList();
        log.info("list.size : {}", list.size());

        list.forEach(this::extractedTable);

    }

    private static JavaNodeTableRecord getJavaNodeTableRecord(String tableName, XmlEntity xmlEntity, JavaInfoEntity javaInfo) {
        return new JavaNodeTableRecord(javaInfo, xmlEntity, List.of(javaInfo.getId().toString()), tableName);
    }


    public String extractUrl(JavaInfoEntity javaInfoEntity) {
        String baseUrl = ""; // 기본 URL 경로를 초기화

        // 클래스 어노테이션 파싱
        if (javaInfoEntity.getClassAnnotations() != null) {
            String[] classAnnotationsArray = javaInfoEntity.getClassAnnotations().split(",");
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
        if (javaInfoEntity.getMethodAnnotations() != null) {
            String[] methodAnnotationsArray = javaInfoEntity.getMethodAnnotations().split(",");
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


    private Stream<JavaInfoEntity> getJavaInfoEntityStream(XmlEntity xmlEntity) {
        var mapperId = xmlEntity.getMapperId();
        var serviceName = xmlEntity.getServiceName();
        var searchParameter = mapperId.contains(".")
                ? "[" + '"' + mapperId + '"' + "]"
                : "[" + mapperId + "]";

        return mapperId.contains(".")
                ? javaInfoRepository.findByMethodParametersContainingAndServiceName(searchParameter, serviceName)
                : javaInfoRepository.findByMethodCallsContainingAndServiceName(searchParameter, serviceName);
    }


    private void extractedTable(JavaNodeTableRecord javaNodeTableRecord) {
//        log.info("javaNodeTableRecord : {}", javaNodeTableRecord);
//        log.info("javaNodeTableRecord.currentJavaInfoEntity : {}", javaNodeTableRecord.currentJavaInfoEntity());

        List<JavaInfoEntity> nextJavaInfos = javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(
                javaNodeTableRecord.currentJavaInfoEntity().getMethodName(),
                javaNodeTableRecord.currentJavaInfoEntity().getClassName(),
                javaNodeTableRecord.xmlEntity().getServiceName());


        String serviceName = javaNodeTableRecord.xmlEntity().getServiceName();
        log.info("serviceName : {}", serviceName);
        String mapperId = javaNodeTableRecord.xmlEntity().getMapperId();
        log.info("mapperId : {}", mapperId);
        log.info("nextJavaInfos : {}", nextJavaInfos.size());

        // 조회 값이 없는 경우 저장 후 완료
        if (nextJavaInfos.isEmpty()) {
            log.info("nextJavaInfos is empty");
            saveTableView(javaNodeTableRecord);
            return;
        }

        nextJavaInfos.forEach(nextJavaInfo -> {
            List<String> allIds = new ArrayList<>(javaNodeTableRecord.javaInfoIds());

            String lastId = javaNodeTableRecord.javaInfoIds().get(javaNodeTableRecord.javaInfoIds().size() - 1);
            log.info("lastId : {}", lastId);

            String nextInfoId = nextJavaInfo.getId().toString();
            log.info("nextInfoId : {}", nextInfoId);

            allIds.add(nextInfoId);
            log.info("allIds : {}", allIds);

            extractedTable(new JavaNodeTableRecord(nextJavaInfo, javaNodeTableRecord.xmlEntity(), allIds, javaNodeTableRecord.tableName()));

            /*if (lastId.equals(nextInfoId)) {
                saveTableView(javaNodeTableRecord);
            } else {
                extractedTable(new JavaNodeTableRecord(nextJavaInfo, javaNodeTableRecord.xmlEntity(), allIds, javaNodeTableRecord.tableName()));
            }*/

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
                .url(getUrl(javaNodeTableRecord))
                .firstId(firstId)
                .lastId(lastId)
                .ids(javaNodeTableRecord.javaInfoIds().toString())
                .build());
    }

    private String getUrl(JavaNodeTableRecord javaNodeTableRecord) {
//        if (extractUrl(javaNodeTableRecord.currentJavaInfoEntity()).isEmpty()) {
//            // url이 없는 경우 한 번 더 검색
//            javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(
//                    javaNodeTableRecord.currentJavaInfoEntity().getMethodName(),
//                    javaNodeTableRecord.currentJavaInfoEntity().getClassName(),
//                    javaNodeTableRecord.xmlEntity().getServiceName());
//            return "없음";
//        }
        return extractUrl(javaNodeTableRecord.currentJavaInfoEntity());
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
}
