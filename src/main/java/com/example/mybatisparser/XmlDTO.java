package com.example.mybatisparser;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record XmlDTO(
        Long id,
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
