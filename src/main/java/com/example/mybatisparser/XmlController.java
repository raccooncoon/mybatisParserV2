package com.example.mybatisparser;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class XmlController {

    final XmlService xmlService;

    @GetMapping("/hi")
    public String sayHi() {
        System.out.println("hi!!!!");
        return "hi";
    }

    // todo 재실행 가능 기능 추가
    /*
    @GetMapping("/start")
    public String startXmlParsing() {
        System.out.println("start!!!!");
        xmlService.startXmlParsing();
        return "hi";
    }
    */

    @GetMapping("id/{id}")
    public XmlEntity getXmlEntityById(@PathVariable Long id) {
        return xmlService.getXmlEntityById(id);
    }

    @GetMapping("mapperId/{mapperId}")
    public Page<XmlDTO> getXmlEntityByMapperId(
            @PathVariable String mapperId,
            @PageableDefault Pageable pageable,
            @RequestParam List<String> mapperTypes) {
        return xmlService.getXmlEntityByMapperId(mapperId, pageable, mapperTypes);
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
