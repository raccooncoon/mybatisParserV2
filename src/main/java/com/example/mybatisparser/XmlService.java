package com.example.mybatisparser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@AllArgsConstructor
public class XmlService {

    private final XmlRepository xmlRepository;
    private final XmlProcess xmlProcess;
    @Async
    public void startXmlParsing() {
         xmlProcess.process();
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

