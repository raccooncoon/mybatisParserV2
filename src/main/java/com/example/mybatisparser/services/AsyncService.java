package com.example.mybatisparser.services;

import com.example.mybatisparser.JavaInfoProcess;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AsyncService {

    private final JavaInfoProcess javaInfoProcess;
    private final XmlService xmlService;
    private final JavaInfoService controllerMapperService;

    @Async
    public void startJavaInfoProcess() {
        controllerMapperService.process(); // java 정보 파일 저장
    }

    @Async
    public void startXmlProcess() {
        xmlService.startXmlParsing(); // Xml 저장
    }

    @Async
    public void nodeCsv() {
        javaInfoProcess.nodeCsv();
    }

}

