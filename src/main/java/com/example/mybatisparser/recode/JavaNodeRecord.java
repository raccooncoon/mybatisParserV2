package com.example.mybatisparser.recode;

import com.example.mybatisparser.entity.JavaInfoEntity;

import java.util.List;

public record JavaNodeRecord(
        JavaInfoEntity currentJavaInfoEntity,
        List<String> javaInfoIds
) {
}
