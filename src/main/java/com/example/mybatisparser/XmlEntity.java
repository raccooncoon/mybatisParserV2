package com.example.mybatisparser;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "XML_ENTITY")
public class XmlEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String serviceName;
    @Column
    private String filePath;
    @Column
    private String mapperId;
    @Column
    private String mapperNameSpace;
    @Column(columnDefinition = "CLOB")
    private String mapperBody;
    @Column
    private String mapperType;

    @Builder
    public XmlEntity(String serviceName, String filePath, String mapperId, String mapperNameSpace, String mapperBody, String mapperType) {
        this.serviceName = serviceName;
        this.filePath = filePath;
        this.mapperId = mapperId;
        this.mapperNameSpace = mapperNameSpace;
        this.mapperBody = mapperBody;
        this.mapperType = mapperType;
    }

}
