package com.example.mybatisparser.recode;

public record UrlDTO(
        Long id,
        String serviceName,
        String className,
        String methodName,
        String url
) {
}
