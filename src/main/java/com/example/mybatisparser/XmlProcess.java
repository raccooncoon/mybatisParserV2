package com.example.mybatisparser;

import lombok.AllArgsConstructor;
import org.apache.ibatis.parsing.XPathParser;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


@Service
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
                    //.peek(System.out::println)
                    .map(Path::toFile)
                    .peek(System.out::println)
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
        return xmlRepository.save(XmlEntity.builder()
                .serviceName(getServicesName(xnodeRecord.file().getPath()))
                .mapperId(xnodeRecord.xNode().getStringAttribute("id"))
                .mapperNameSpace(xnodeRecord.xNode().getParent().getStringAttribute("namespace"))
                .mapperName(xnodeRecord.xNode().getParent().getStringAttribute("namespace").substring(xnodeRecord.xNode().getParent().getStringAttribute("namespace").lastIndexOf(".") + 1))
                .mapperType(xnodeRecord.xNode().getName())
                .mapperBody(xnodeRecord.xNodeBody())
                .filePath(xnodeRecord.file().getPath())
                .fileName(xnodeRecord.file().getName())
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

    private String removeCData(String xmlContent) {
        // CDATA 섹션을 제거하는 정규 표현식
        String patternString = "<!\\[CDATA\\[(.*?)\\]\\]>";
        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xmlContent);

        // CDATA 섹션을 제거한 문자열 반환
        return matcher.replaceAll("");
    }

    private List<XnodeRecord> getXNodeList(File file) {

        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            // XML 파일 내용을 문자열로 읽어오기
            String xmlContent = new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);

            // CDATA 제거
            String xmlWithoutCData = removeCData(xmlContent);

            // XPathParser로 파싱
            XPathParser parser = new XPathParser(new ByteArrayInputStream(xmlWithoutCData.getBytes()), false, null, null);

            List<String> xPaths = List.of(
                    "/mapper/insert",
                    "/mapper/update",
                    "/mapper/delete",
                    "/mapper/select",
                    "/sqlMap/insert",
                    "/sqlMap/update",
                    "/sqlMap/delete",
                    "/sqlMap/select");

            return xPaths.stream()
                    .flatMap(expression -> parser.evalNodes(expression).stream().map(
                            xNode -> new XnodeRecord(
                                    xNode.toString(),
                                    file,
                                    xNode
                            )
                    )).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}



