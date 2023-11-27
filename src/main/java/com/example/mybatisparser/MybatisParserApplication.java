package com.example.mybatisparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@Import(CorsConfig.class)
@SpringBootApplication
public class MybatisParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisParserApplication.class, args);
    }

}
