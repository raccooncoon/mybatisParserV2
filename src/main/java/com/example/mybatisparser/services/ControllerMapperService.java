package com.example.mybatisparser.services;

import com.example.mybatisparser.repository.JavaInfoRepository;
import com.example.mybatisparser.JavaInfoVisitor;
import com.example.mybatisparser.config.ExternalConfig;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
public class ControllerMapperService {

    private final ExternalConfig externalConfig;
    private final JavaInfoRepository javaInfoRepository;

    public void process() {
        // 외부 DTD 액세스 허용 설정
        System.setProperty("javax.xml.accessExternalDTD", "all");

        // 대상 XMl 에서 CUD XNode count
        String folderPath = externalConfig.getFolderPath();

        System.out.println("folderPath = " + folderPath);

        getJavaFileList(folderPath);

    }

    private void getJavaFileList(String folderPath) {
        try (Stream<Path> pathStream = Files.walk(Paths.get(folderPath))) {
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().toLowerCase().endsWith(".java"))
                    .peek(c -> log.info("{}", c))
                    .forEach(this::parseAndVisit);
        } catch (IOException e) {
            log.info("e : {}", e.getMessage());
        }
    }

    private void parseAndVisit(Path path) {
        JavaParser javaParser = new JavaParser();

        try {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(path);
            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                CompilationUnit cu = parseResult.getResult().get();

                new JavaInfoVisitor(javaInfoRepository, externalConfig, path).visit(cu, null);

            }
        } catch (IOException e) {
            log.error("Error parsing file: {}", path, e);
        }
    }
}

