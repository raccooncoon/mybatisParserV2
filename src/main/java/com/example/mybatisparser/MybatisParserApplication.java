package com.example.mybatisparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@Import(CorsConfig.class)
@SpringBootApplication
public class MybatisParserApplication {

    public static final String SOURCE_PATH = "../"; //todo: 경로 환경 변수로 받을 수 있게 없는 경우 기본 값으로 설정

    public static void main(String[] args) {
        SpringApplication.run(MybatisParserApplication.class, args);
    }

}
