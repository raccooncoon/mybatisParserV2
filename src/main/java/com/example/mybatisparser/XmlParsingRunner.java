package com.example.mybatisparser;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("local")
@Component
public class XmlParsingRunner implements ApplicationRunner {

    private final XmlService xmlService;

    public XmlParsingRunner(XmlService xmlService) {
        this.xmlService = xmlService;
    }

    @Override
    public void run(ApplicationArguments args) {
        xmlService.startXmlParsing();
    }
}
