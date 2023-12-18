package com.example.mybatisparser.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Builder
public class UrlEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String serviceName;

    @Column
    private String className;

    @Column
    private String methodName;

    @Column
    private String url;

    @Column
    private String firstClassName;

    @Column
    private String firstMethodName;


}
