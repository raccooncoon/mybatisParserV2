package com.example.mybatisparser;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
public class XmlController {

    final XmlService xmlService;
    @GetMapping("/start")
    public String startXmlParsing() {
        xmlService.startXmlParsing();
        return "hi";
    }
    // todo 재실행 가는 기능 추가

    @GetMapping("id/{id}")
    public XmlEntity getXmlEntityById(@PathVariable Long id) {
        return xmlService.getXmlEntityById(id);
    }

    @GetMapping("mapperId/{mapperId}")
    public Page<XmlEntity> getXmlEntityByMapperId(
            @PathVariable String mapperId,
            @PageableDefault Pageable pageable
    ) {
        return xmlService.getXmlEntityByMapperId(mapperId, pageable);
    }

    @GetMapping("mapperType/{mapperType}")
    public Page<XmlEntity> getXmlEntityByMapperType(
            @PathVariable String mapperType,
            @PageableDefault Pageable pageable) {
        return xmlService.getXmlEntityByMapperType(mapperType, pageable);
    }

    @GetMapping("mapperBody/{mapperBody}")
    public Page<XmlDTO> getCUDXmlEntityByMapperBody(
            @PathVariable String mapperBody,
            @PageableDefault Pageable pageable,
            @RequestParam List<String> mapperTypes) {
        return xmlService.getCUDXmlEntityByMapperBodyLike(mapperBody, pageable, mapperTypes);
    }

}
