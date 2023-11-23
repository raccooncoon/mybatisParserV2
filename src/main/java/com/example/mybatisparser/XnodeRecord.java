package com.example.mybatisparser;

import org.apache.ibatis.parsing.XNode;

import java.io.File;

public record XnodeRecord(
        File file,
        XNode xNode
) {
}
