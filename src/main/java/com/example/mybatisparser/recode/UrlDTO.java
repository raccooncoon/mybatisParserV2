package com.example.mybatisparser.recode;

import com.example.mybatisparser.entity.JavaInfoEntity;

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
