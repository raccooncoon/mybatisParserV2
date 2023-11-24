package com.example.mybatisparser;

public record XmlDTO(
        Long id,
        String serviceName,
        //      String filePath,
        String fileName,
        String mapperId,
        //      String mapperNameSpace,
        String mapperName,
        //String mapperBody,
        String mapperType
) {
}
