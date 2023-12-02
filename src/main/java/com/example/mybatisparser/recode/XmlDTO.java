package com.example.mybatisparser.recode;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record XmlDTO(
        String serviceName,
        @JsonIgnore
        String filePath,
        String fileName,
        String mapperId,
        String mapperNameSpace,
        String mapperName,
        //@JsonIgnore
        String mapperBody,
        String mapperType
) {
}
