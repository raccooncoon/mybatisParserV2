package com.example.mybatisparser;

import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ControllerParsingRunner implements ApplicationRunner {

    private final ControllerMapperService controllerMapperService;

    @Override
    public void run(ApplicationArguments args) {
        controllerMapperService.process();
    }
}
