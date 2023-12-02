package com.example.mybatisparser.services;

import com.example.mybatisparser.JavaInfoProcess;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class JavaInfoService {

    private final JavaInfoProcess javaInfoProcess;
    @Async
    public void startJavaNodeProcess() {
//        javaInfoProcess.process(); // java 파일 node 저장
    }

    @Async
    public void deleteNode() {
        javaInfoProcess.deleteNode();
    }

}

