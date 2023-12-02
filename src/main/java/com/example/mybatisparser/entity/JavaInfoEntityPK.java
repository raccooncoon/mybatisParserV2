package com.example.mybatisparser.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JavaInfoEntityPK implements Serializable {
    private String packageName;
    private String className;
    private String methodName;
}
