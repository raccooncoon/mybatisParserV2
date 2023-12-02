package com.example.mybatisparser;

import com.example.mybatisparser.config.ExternalConfig;
import com.example.mybatisparser.entity.XmlEntity;
import com.example.mybatisparser.entity.XmlEntityPK;
import com.example.mybatisparser.recode.XnodeRecord;
import com.example.mybatisparser.repository.XmlRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.parsing.XPathParser;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Service
@Slf4j
@AllArgsConstructor
public class XmlProcess {

    private final XmlRepository xmlRepository;
    private final ExternalConfig externalConfig;

    public long process() {

        // 외부 DTD 액세스 허용 설정
        System.setProperty("javax.xml.accessExternalDTD", "all");

        // 대상 XMl 에서 CUD XNode count
        String folderPath = externalConfig.getFolderPath();

        //System.out.println("folderPath = " + folderPath);

        return getXnodeList(folderPath);
    }

    private long getXnodeList(String folderPath) {
        try (Stream<Path> pathStream = Files.walk(Paths.get(folderPath))) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                    .map(Path::toFile)
                    .peek(c -> log.info("{}", c))
                    .flatMap(file -> getXNodeList(file).stream())
                    .map(this::getSave)
                    .count();
        } catch (IOException e) {
            // 예외 처리: IOException이 발생하면 빈 리스트 반환 또는 로깅 후 빈 리스트 반환
            e.printStackTrace();
            return 0;
        }
    }

    private XmlEntity getSave(XnodeRecord xnodeRecord) {

        String fileName = xnodeRecord.file().getName();
        String mapperId = Optional.ofNullable(xnodeRecord.xNode().getStringAttribute("id")).orElse("no_mapper_id_"+ LocalDate.now());
        XmlEntityPK id = new XmlEntityPK(fileName, mapperId);

        return xmlRepository.save(XmlEntity.builder()
                .id(id)
                .serviceName(getServicesName(xnodeRecord.file().getPath()))
                .mapperNameSpace(xnodeRecord.xNode().getParent().getStringAttribute("namespace"))
                .mapperName(xnodeRecord.xNode().getParent().getStringAttribute("namespace").substring(xnodeRecord.xNode().getParent().getStringAttribute("namespace").lastIndexOf(".") + 1))
                .mapperType(xnodeRecord.xNode().getName())
                .mapperBody(xnodeRecord.xNodeBody())
                .filePath(xnodeRecord.file().getPath())
                .build());
    }

    private String getServicesName(String filePath) {

        String stringWithoutDotSlash = filePath.substring(externalConfig.getFolderPath().length() + 1); // 2는 "./"의 길이

        // 다음 '/'까지의 부분 문자열 가져오기
        int endIndex = stringWithoutDotSlash.indexOf("/");
        if (endIndex != -1) {
            return stringWithoutDotSlash.substring(0, endIndex);
        } else {
            System.out.println("No '/' found in the string after './'.");
        }
        return "";
    }

    private List<XnodeRecord> getXNodeList(File file) {

        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            XPathParser parser = new XPathParser(fileInputStream, false, null, null);
            // todo 파싱시 <![CDATA[ ]]> 내용이 파싱 되지 않는 문제 해결 필요

            return Stream.of("/mapper", "/sqlMap")
                    .flatMap(expression -> parser.evalNodes(expression).stream())
                    .flatMap(nodes -> nodes.getChildren().stream())
//                    .peek(System.out::println)
                    .map(xNode -> new XnodeRecord(xNode.toString(), file, xNode))
//                    .peek(System.out::println)
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}



