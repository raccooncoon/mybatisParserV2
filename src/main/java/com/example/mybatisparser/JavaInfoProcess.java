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

    private static String getServiceName() {
        return "papaya-master";
    }

    public void process() {
        String methodCall = "mergeTaxSavePhoneNumber";
        List<JavaInfoEntity> firstJavaInfos = javaInfoRepository.findByMethodCallsContainingAndServiceName(methodCall, getServiceName());
        //getLog(firstJavaInfos);

        firstJavaInfos.forEach(javaInfo -> {
                    JavaNodeRecord javaNodeRecord = new JavaNodeRecord(javaInfo, List.of(javaInfo.getId().toString()));
                    extracted(javaNodeRecord);
                }
        );
    }

    private void extracted(JavaNodeRecord javaNodeRecord) {
        List<JavaInfoEntity> nextJavaInfos = javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(
                javaNodeRecord.currentJavaInfoEntity().getMethodName(), javaNodeRecord.currentJavaInfoEntity().getClassName(), getServiceName());

        if (nextJavaInfos.isEmpty()) {
//            log.info("nextJavaInfos is empty");
//            log.info("javaInfoIds : {}", javaNodeRecord.javaInfoIds());
            nodeRepository.save(NodeEntity.builder()
                    .ids(javaNodeRecord.javaInfoIds())
                    .firstId(javaNodeRecord.javaInfoIds().get(0))
                    .lastId(javaNodeRecord.javaInfoIds().get(javaNodeRecord.javaInfoIds().size() - 1))
                    .build());
            return;
        }

        nextJavaInfos.forEach(nextJavaInfo -> {
            List<String> allIds = new ArrayList<>(javaNodeRecord.javaInfoIds());
            allIds.add(nextJavaInfo.getId().toString());
            JavaNodeRecord javaNodeRecord1 = new JavaNodeRecord(nextJavaInfo, allIds);
            extracted(javaNodeRecord1);
        });
    }

/*    private static void getLog(List<JavaInfoEntity> list) {
        list.forEach(javaInfoEntity -> {
            log.info("javaInfoEntity : {}", javaInfoEntity.getId());
            log.info("javaInfoEntity : {}", javaInfoEntity.getClassName());
            log.info("javaInfoEntity : {}", javaInfoEntity.getMethodName());
        });
    }*/

}
