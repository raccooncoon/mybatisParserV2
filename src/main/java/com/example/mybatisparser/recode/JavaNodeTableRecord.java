package com.example.mybatisparser.recode;

import com.example.mybatisparser.entity.JavaInfoEntity;
import com.example.mybatisparser.entity.XmlEntity;

import java.util.List;

public record JavaNodeTableRecord(
        JavaInfoEntity currentJavaInfoEntity,
        XmlEntity xmlEntity,
        List<String> javaInfoIds,
        String tableName
) {
}
