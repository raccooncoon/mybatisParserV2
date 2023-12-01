package com.example.mybatisparser;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Builder
public class JavaInfoEntity {

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
    private String packageName;
    @Column
    private String className;
    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
//    @Convert(converter = StringListConverter.class)
    private String classAnnotations;
    @Column
    private String methodName;
    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
//    @Convert(converter = StringListConverter.class)
    private String methodCalls;
    @Column
//    @Convert(converter = StringListConverter.class)
    private String methodAnnotations;
    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
//    @Convert(converter = StringListConverter.class)
    private String classFields;

}
