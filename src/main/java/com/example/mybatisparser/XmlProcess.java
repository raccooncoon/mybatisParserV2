package com.example.mybatisparser;

import com.example.mybatisparser.config.ExternalConfig;
import com.example.mybatisparser.entity.XmlEntity;
import com.example.mybatisparser.recode.XnodeRecord;
import com.example.mybatisparser.repository.XmlRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.parsing.XPathParser;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
                    .flatMap(this::getXNodeList)
                    .map(this::getSave)
                    .count();
        } catch (IOException e) {
            // 예외 처리: IOException이 발생하면 빈 리스트 반환 또는 로깅 후 빈 리스트 반환
            e.printStackTrace();
            return 0;
        }
    }

    private XmlEntity getSave(XnodeRecord xnodeRecord) {
        return xmlRepository.save(XmlEntity.builder()
                .mapperId(Optional.ofNullable(xnodeRecord.xNode().getStringAttribute("id")).orElse("no_mapper_id_" + LocalDate.now()))
                .serviceName(getServicesName(xnodeRecord.file().getPath()))
                .mapperNameSpace(xnodeRecord.xNode().getParent().getStringAttribute("namespace"))
                .mapperType(xnodeRecord.xNode().getName())
                .mapperBody(xnodeRecord.xNodeBody())
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

    private Stream<XnodeRecord> getXNodeList(File file) {

        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            XPathParser parser = new XPathParser(fileInputStream, false, null, null);

            return Stream.of("/mapper", "/sqlMap")
                    .flatMap(expression -> parser.evalNodes(expression).stream())
                    //.peek(System.out::println)
                    .flatMap(nodes -> nodes.getChildren().stream())
                    //.peek(xnode -> System.out.println(xnode.getName()))
                    .map(node ->
                            {
                                String id = parser.evalString("//" + node.getPath() + "[@id='" + node.getStringAttribute("id") + "']");
                                return new XnodeRecord(id, file, node);
                            }
                    );
        } catch (Exception e) {
            e.printStackTrace();
            return Stream.of();
        }
    }
}



