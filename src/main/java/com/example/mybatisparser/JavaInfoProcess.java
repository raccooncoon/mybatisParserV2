package com.example.mybatisparser;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class JavaInfoProcess {

    private final JavaInfoRepository javaInfoRepository;


    private static String getServiceName() {
        return "serviceName";
    }

    public void process() {
        String methodCall = "mergeTaxSavePhoneNumber";

        List<JavaInfoEntity> javaInfoEntityList = javaInfoRepository.findByMethodCallsContainingAndServiceName(methodCall, getServiceName());

        javaInfoEntityList.forEach(javaInfo -> {
            List<JavaInfoEntity> idList = javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(methodCall, javaInfo.getClassName(), getServiceName());
            processNode(javaInfo, idList);
        });
    }

    private void processNode(JavaInfoEntity javaInfo, List<JavaInfoEntity> idList) {
        List<JavaInfoEntity> contains = javaInfoRepository.findByMethodCallsContainsAndClassFieldsContainsAndServiceName(javaInfo.getMethodName(), javaInfo.getClassName(), getServiceName());
    }
}
