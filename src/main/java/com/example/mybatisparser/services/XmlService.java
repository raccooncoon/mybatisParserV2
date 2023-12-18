package com.example.mybatisparser.services;

import com.example.mybatisparser.XmlProcess;
import com.example.mybatisparser.recode.XmlDTO;
import com.example.mybatisparser.repository.XmlRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    public Page<XmlDTO> getCUDXmlEntityByMapperBodyLike(String mapperBody, Pageable pageable, List<String> mapperTypes) {
        return xmlRepository.findByMapperBodyContainsAndMapperTypeInOrderByServiceName(mapperBody, mapperTypes, pageable)
                .map(xmlEntity -> new XmlDTO(
                        xmlEntity.getId(),
                        xmlEntity.getServiceName(),
                        xmlEntity.getMapperType(),
                        xmlEntity.getMapperNameSpace(),
                        xmlEntity.getMapperId(),
                        xmlEntity.getMapperBody()
                ));
    }
}

