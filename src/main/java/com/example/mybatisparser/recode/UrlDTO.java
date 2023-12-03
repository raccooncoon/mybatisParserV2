package com.example.mybatisparser.recode;

import java.util.List;

public record UrlDTO(
        Long id,
        String url,
        List<String> byFirstIdIn,
        List<String> ids,
        String packageName,
        String className,
        String methodName,
        String serviceName,
        String fileName
) {
}
