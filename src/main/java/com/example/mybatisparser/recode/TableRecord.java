package com.example.mybatisparser.recode;

import com.example.mybatisparser.entity.NodeEntity;

public record TableRecord(
        NodeEntity nodeEntity,
        String mapperId,
        String mapperType,
        String tableName
) {
}
