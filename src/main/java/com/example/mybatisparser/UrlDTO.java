package com.example.mybatisparser;

import java.util.List;

public record UrlDTO(
        Long id,
        String serviceName,
        JavaInfoEntity javaInfoEntity,
        String Url,
        List<String> byFirstIdIn,
        List<String> ids
) {
}
