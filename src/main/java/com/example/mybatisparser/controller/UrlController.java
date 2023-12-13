package com.example.mybatisparser.controller;

import com.example.mybatisparser.recode.UrlDTO;
import com.example.mybatisparser.services.UrlService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/url")
public class UrlController {

    final UrlService urlService;

    @GetMapping("/hi")
    public String sayHi() {
        System.out.println("hi!!!!");
        return "hi";
    }

    @GetMapping("mapperId/{servicesName}/{mapperId}")
    public Page<UrlDTO> getXmlEntityByMapperId(
            @PathVariable String servicesName,
            @PathVariable String mapperId,
            @PageableDefault Pageable pageable
    ) {
//        return urlService.getMapperId(servicesName, mapperId, pageable);
        return null;
    }

//    @GetMapping("mapperIdV2/{servicesName}/{mapperId}")
//    public Page<UrlDTO> getXmlEntityByMapperIdV2(
//            @PathVariable String servicesName,
//            @PathVariable String mapperId,
//            @PageableDefault Pageable pageable
//    ) {
//        return null;
//        return urlService.getMapperIdV2(servicesName, mapperId, pageable);
//    }




    /*@GetMapping("mapperId/{mapperId}")
    public Page<XmlDTO> getXmlEntityByMapperId(
            @PathVariable String mapperId,
            @PageableDefault Pageable pageable,
            @RequestParam List<String> mapperTypes) {
        return urlService.getXmlEntityByMapperId(mapperId, pageable, mapperTypes);
    }*/

//    @GetMapping("mapperBody/{mapperBody}")
//    public Page<XmlDTO> getCUDXmlEntityByMapperBody(@PageableDefault Pageable pageable) {
//        return null;
//    }

}
