package com.example.mybatisparser;

import com.example.mybatisparser.entity.JavaInfoEntity;
import com.example.mybatisparser.entity.NodeEntity;
import com.example.mybatisparser.entity.XmlEntity;
import com.example.mybatisparser.recode.JavaNodeRecord;
import com.example.mybatisparser.repository.JavaInfoRepository;
import com.example.mybatisparser.repository.NodeRepository;
import com.example.mybatisparser.repository.XmlRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
//@Transactional
public class JavaInfoProcess {

    private final JavaInfoRepository javaInfoRepository;
    private final NodeRepository nodeRepository;
    private final XmlRepository xmlRepository;

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

                List<JavaInfoEntity> firstJavaInfos = Stream.concat(
                        javaInfoRepository.findByMethodCallsContainingAndServiceName("["+mapperId+"]", xmlEntity.getServiceName()).stream(),
                        javaInfoRepository.findByMethodParametersContainingAndServiceName('"'+mapperId+'"', xmlEntity.getServiceName()).stream()
                ).toList();

                firstJavaInfos.stream()
                        .map(javaInfo -> new JavaNodeRecord(
                                javaInfo,
                                xmlEntity,
                                List.of(javaInfo.getId().toString())
                        ))
                        .forEach(this::extracted);
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
            return;
        }

        nextJavaInfos.forEach(nextJavaInfo -> {
            List<String> allIds = new ArrayList<>(javaNodeRecord.javaInfoIds());
            String nextInfoId = nextJavaInfo.getId().toString();

            // 자기 자신이 이미 포함되어 있지 않은 경우에만 재귀 호출
            if (!allIds.contains(nextInfoId)) {
                allIds.add(nextInfoId);
                extracted(new JavaNodeRecord(nextJavaInfo, javaNodeRecord.xmlEntity(), allIds));
            }
        });
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

    public void deleteNode() {
        nodeRepository.deleteAll();
    }
}
