package com.example.mybatisparser.services;

import com.example.mybatisparser.recode.XmlDTO;
import com.example.mybatisparser.XmlProcess;
import com.example.mybatisparser.repository.XmlRepository;
import com.example.mybatisparser.entity.XmlEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

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

/*
    public XmlEntity getXmlEntityById(Long id) {
        return xmlRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 id가 없습니다. id=" + id));
    }

    public Page<XmlDTO> getXmlEntityByMapperId(String mapperId, Pageable pageable, List<String> mapperTypes) {
        return getXmlDTOS(xmlRepository.findByMapperIdContainsAndMapperTypeIn(mapperId, mapperTypes, pageable));
    }

    public Page<XmlEntity> getXmlEntityByMapperType(String mapperId, Pageable pageable) {
        return xmlRepository.findByMapperTypeContains(mapperId, pageable);
    }
*/

    public Page<XmlDTO> getCUDXmlEntityByMapperBodyLike(String mapperId, Pageable pageable, List<String> mapperTypes) {
        return getXmlDTOS(xmlRepository.findByMapperBodyContainsAndMapperTypeIn(mapperId, mapperTypes, pageable));
    }

    private static PageImpl<XmlDTO> getXmlDTOS(Page<XmlEntity> xmlEntityPage) {
        List<XmlDTO> xmlDTOList = xmlEntityPage.getContent().stream().map(xmlEntity -> new XmlDTO(
                xmlEntity.getServiceName(),
                xmlEntity.getFilePath(),
                xmlEntity.getId().getFileName(),
                xmlEntity.getId().getMapperId(),
                xmlEntity.getMapperNameSpace(),
                xmlEntity.getMapperName(),
                xmlEntity.getMapperBody(),
                xmlEntity.getMapperType()
        )).toList();

        return new PageImpl<>(xmlDTOList, xmlEntityPage.getPageable(), xmlEntityPage.getTotalElements());
    }
}

