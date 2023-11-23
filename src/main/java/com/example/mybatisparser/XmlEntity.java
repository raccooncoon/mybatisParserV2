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
@Builder
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
    private String fileName;
    @Column
    private String mapperId;
    @Column
    private String mapperNameSpace;
    @Column
    private String mapperName;
    @Column(columnDefinition = "CLOB")
    private String mapperBody;
    @Column
    private String mapperType;

}
