package com.example.mybatisparser;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class JavaInfoProcess {

    private final JavaInfoRepository javaInfoRepository;
    private final NodeRepository nodeRepository;
    private final XmlRepository xmlRepository;

    private static String getServiceName() {
        return "coral-release";
    }

    public void process() {

        List<XmlEntity> xmlEntities = xmlRepository.findByServiceNameAndMapperTypeIn(
                getServiceName(),
                List.of("create", "update", "delete")
        );

        xmlEntities.forEach(xmlEntity -> {
            log.info("xmlEntity : {}", xmlEntity.getId());
            log.info("xmlEntity : {}", xmlEntity.getMapperType());
            log.info("xmlEntity : {}", xmlEntity.getServiceName());
            log.info("xmlEntity : {}", xmlEntity.getMapperId());

            processJavaInfo(xmlEntity.getMapperId(), xmlEntity.getServiceName());
        });

//        processJavaInfo("getRefundPossibleInfo", getServiceName());
    }

    private void processJavaInfo(String methodCall, String serviceName) {
        List<JavaInfoEntity> firstJavaInfos = javaInfoRepository.findByMethodCallsContainingAndServiceName(methodCall, serviceName);
        firstJavaInfos.forEach(javaInfo -> {
                    JavaNodeRecord javaNodeRecord = new JavaNodeRecord(javaInfo, List.of(javaInfo.getId().toString()));
                    extracted(javaNodeRecord, serviceName);
                }
        );
    }

    public void deleteNode() {
        nodeRepository.deleteAll();
    }

    private void extracted(JavaNodeRecord javaNodeRecord, String serviceName) {
        List<JavaInfoEntity> nextJavaInfos = javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(
                javaNodeRecord.currentJavaInfoEntity().getMethodName(), javaNodeRecord.currentJavaInfoEntity().getClassName(), serviceName);

        // 조회 값이 없는 경우 저장 후 완료
        if (nextJavaInfos.isEmpty()) {
//            log.info("nextJavaInfos is empty");
            log.info("javaInfoIds : {}", javaNodeRecord.javaInfoIds());
            nodeRepository.save(NodeEntity.builder()
                    .ids(javaNodeRecord.javaInfoIds())
                    .firstId(javaNodeRecord.javaInfoIds().get(0))
                    .lastId(javaNodeRecord.javaInfoIds().get(javaNodeRecord.javaInfoIds().size() - 1))
                    .build());
            return;
        }

        nextJavaInfos.forEach(nextJavaInfo -> {
            List<String> allIds = new ArrayList<>(javaNodeRecord.javaInfoIds());
            String nextInfoId = nextJavaInfo.getId().toString();

            // 자기 자신이 이미 포함되어 있지 않은 경우에만 재귀 호출
            if (!allIds.contains(nextInfoId)) {
                allIds.add(nextInfoId);
                extracted(new JavaNodeRecord(nextJavaInfo, allIds), serviceName);
            }
        });
    }
}
