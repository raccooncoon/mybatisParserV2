package com.example.mybatisparser.recode;

public record XmlDTO(
        Long id,
        String serviceName,
        String mapperType,
        String mapperNameSpace,
        String mapperId,
        String mapperBody
) {
}
