package com.example.mybatisparser;

import org.apache.ibatis.parsing.XNode;

import java.io.File;

public record XnodeRecord(
        String xNodeBody,
        File file,
        XNode xNode
) {
}
