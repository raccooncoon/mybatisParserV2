package com.example.mybatisparser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Pageable;

import static com.example.mybatisparser.MybatisParserApplication.SOURCE_PATH;

@Slf4j
@Service
@AllArgsConstructor
public class XmlService {

    private final XmlRepository xmlRepository;

    private final XmlProcess xmlProcess;


    @Async
    public void startXmlParsing() {
        List<XnodeRecord> xnodeRecordList = xmlProcess.process();

        List<XmlEntity> list = xnodeRecordList.stream().map(xnodeRecord -> XmlEntity.builder()
                .serviceName(getServicesName(xnodeRecord.file().getPath()))
                .mapperId(xnodeRecord.xNode().getStringAttribute("id"))
                .mapperNameSpace(xnodeRecord.xNode().getParent().getStringAttribute("namespace"))
                .mapperName(xnodeRecord.xNode().getParent().getStringAttribute("namespace").substring(xnodeRecord.xNode().getParent().getStringAttribute("namespace").lastIndexOf(".") + 1))
                .mapperType(xnodeRecord.xNode().getName())
                .mapperBody(xnodeRecord.xNode().toString().replaceAll("[^\\x00-\\x7F]", ""))
                .filePath(xnodeRecord.file().getPath())
                .fileName(xnodeRecord.file().getName())
                .build()).toList();

        xmlRepository.saveAll(list);

    }

    private static String getServicesName(String filePath) {

        String stringWithoutDotSlash = filePath.substring(SOURCE_PATH.length()); // 2는 "./"의 길이

        // 다음 '/'까지의 부분 문자열 가져오기
        int endIndex = stringWithoutDotSlash.indexOf("/");
        if (endIndex != -1) {
            return stringWithoutDotSlash.substring(0, endIndex);
        } else {
            System.out.println("No '/' found in the string after './'.");
        }
        return "";
    }

    public XmlEntity getXmlEntityById(Long id) {
        return xmlRepository.findById(id).orElseThrow();
    }

    public Page<XmlEntity> getXmlEntityByMapperId(String mapperId, Pageable pageable) {
        return xmlRepository.findByMapperIdContains(mapperId, pageable);
    }

    public Page<XmlEntity> getXmlEntityByMapperType(String mapperId, Pageable pageable) {
        return xmlRepository.findByMapperTypeContains(mapperId, pageable);
    }

    public Page<XmlDTO> getCUDXmlEntityByMapperBodyLike(String mapperId, Pageable pageable, List<String> mapperTypes) {

        Page<XmlEntity> xmlEntityPage = xmlRepository.findByMapperBodyContainsAndMapperTypeIn(mapperId, mapperTypes, pageable);

        List<XmlDTO> xmlDTOList = xmlEntityPage.getContent().stream().map(xmlEntity -> new XmlDTO(
                xmlEntity.getId(),
                xmlEntity.getServiceName(),
                xmlEntity.getFilePath(),
                xmlEntity.getFileName(),
                xmlEntity.getMapperId(),
                xmlEntity.getMapperNameSpace(),
                xmlEntity.getMapperName(),
                xmlEntity.getMapperBody(),
                xmlEntity.getMapperType()
        )).toList();

        return new PageImpl<>(xmlDTOList, xmlEntityPage.getPageable(), xmlEntityPage.getTotalElements());
        }
    }

