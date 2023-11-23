package com.example.mybatisparser;

import org.apache.ibatis.parsing.XPathParser;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static com.example.mybatisparser.MybatisParserApplication.SOURCE_PATH;

@Service
public class XmlProcess {

    public List<XnodeRecord> process(){
        // 외부 DTD 액세스 허용 설정
        System.setProperty("javax.xml.accessExternalDTD", "all");

        // 대상 XMl 에서 CUD XNode 리스트 가져오기
        return getXnodeList(SOURCE_PATH);
    }

    private List<XnodeRecord> getXnodeList(String folderPath) {
        try (Stream<Path> pathStream = Files.walk(Paths.get(folderPath))) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                    .peek(System.out::println)
                    .map(Path::toFile)
                    .flatMap(file -> getXNodeList(file).stream())
                    .toList();
        } catch (IOException e) {
            // 예외 처리: IOException이 발생하면 빈 리스트 반환 또는 로깅 후 빈 리스트 반환
            e.printStackTrace();
            return List.of();
        }
    }

    private List<XnodeRecord> getXNodeList(File file) {

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            XPathParser parser = new XPathParser(fileInputStream, false, null, null);

            List<String> xPaths = List.of(
                    "/mapper/insert", "/mapper/update", "/mapper/delete","/mapper/select",
                    "/sqlMap/insert", "/sqlMap/update", "/sqlMap/delete", "/sqlMap/select");

            return xPaths.stream()
                    .map(parser::evalNodes)
                    .flatMap(List::stream)
                    .map(xNodes -> new XnodeRecord(file, xNodes))
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}


