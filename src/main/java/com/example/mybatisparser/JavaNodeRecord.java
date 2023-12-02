package com.example.mybatisparser;

import java.util.List;

public record JavaNodeRecord(
        JavaInfoEntity currentJavaInfoEntity,
        List<String> javaInfoIds
) {
}
