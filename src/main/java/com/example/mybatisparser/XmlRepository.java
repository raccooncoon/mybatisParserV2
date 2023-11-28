package com.example.mybatisparser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface XmlRepository extends JpaRepository<XmlEntity, Long> {

    Page<XmlEntity> findByMapperIdContainsAndMapperTypeIn(String mapperId, List<String> mapperTypes, Pageable pageable);
    Page<XmlEntity> findByMapperTypeContains(String mapperId, Pageable pageable);
//    Page<XmlEntity> findByMapperBodyContains(String mapperId, Pageable pageable);
    Page<XmlEntity> findByMapperBodyContainsAndMapperTypeIn(String mapperId, List<String> mapperTypes, Pageable pageable);
}
