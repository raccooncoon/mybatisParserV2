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
                    List.of("create", "update", "delete")
            );

            xmlEntities.forEach(xmlEntity -> {
                String mapperId = xmlEntity.getId().getMapperId();
                log.info("xmlEntity : {}", mapperId);

                List<JavaInfoEntity> firstJavaInfos = (mapperId.contains("."))
                        ? javaInfoRepository.findByMethodParametersContainingAndServiceName(mapperId, serviceName)
                        : javaInfoRepository.findByMethodCallsContainingAndServiceName(mapperId, serviceName);

                firstJavaInfos.stream()
                        .map(javaInfo -> new JavaNodeRecord(javaInfo, List.of(javaInfo.getId().toString()), serviceName))
                        .forEach(this::extracted);
            });
        });
    }

    // 재귀호출
    private void extracted(JavaNodeRecord javaNodeRecord) {
        log.info("javaNodeRecord : {}", javaNodeRecord);
        List<JavaInfoEntity> nextJavaInfos = javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(
                javaNodeRecord.currentJavaInfoEntity().getId().getMethodName(),
                javaNodeRecord.currentJavaInfoEntity().getId().getClassName(),
                javaNodeRecord.serviceName());

        // 조회 값이 없는 경우 저장 후 완료
        if (nextJavaInfos.isEmpty()) {
            log.info("nextJavaInfos is empty");
            log.info("javaInfoIds : {}", javaNodeRecord.javaInfoIds());
            nodeRepository.save(NodeEntity.builder()
                    .ids(javaNodeRecord.javaInfoIds())
                    .firstId(javaNodeRecord.javaInfoIds().get(0))
                    .lastId(javaNodeRecord.javaInfoIds().get(javaNodeRecord.javaInfoIds().size() - 1))
                    .serviceName(javaNodeRecord.serviceName())
                    .build());
            return;
            //return
        }

        nextJavaInfos.forEach(nextJavaInfo -> {
            List<String> allIds = new ArrayList<>(javaNodeRecord.javaInfoIds());
            String nextInfoId = nextJavaInfo.getId().toString();

            // 자기 자신이 이미 포함되어 있지 않은 경우에만 재귀 호출
            if (!allIds.contains(nextInfoId)) {
                allIds.add(nextInfoId);
                JavaNodeRecord nodeRecord = new JavaNodeRecord(nextJavaInfo, allIds, javaNodeRecord.serviceName());
                extracted(nodeRecord);
            }
        });
    }

    public void deleteNode() {
        nodeRepository.deleteAll();
    }
}
