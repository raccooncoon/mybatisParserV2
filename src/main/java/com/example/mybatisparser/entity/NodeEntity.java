package com.example.mybatisparser.entity;

import com.example.mybatisparser.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Builder
public class NodeEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    @Convert(converter = StringListConverter.class)
    private List<String> ids;

    @Column
    private String firstId;

    @Column
    private String lastId;

    @ManyToOne
    private JavaInfoEntity javaInfoEntity;

}
