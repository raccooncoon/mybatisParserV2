package com.example.mybatisparser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Builder
public class XmlEntity {

    @EmbeddedId
    private XmlEntityPK id;

    @Column
    private String serviceName;

    @Column
    private String filePath;

    @Column
    private String mapperName;

    @Column
    private String mapperNameSpace;

//    @Column(columnDefinition = "CLOB")
    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String mapperBody;

    @Column
    private String mapperType;

}
