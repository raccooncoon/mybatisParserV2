package com.example.mybatisparser.repository;

import com.example.mybatisparser.entity.XmlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.stream.Stream;

public interface XmlRepository extends JpaRepository<XmlEntity, Long> {

    List<XmlEntity> findByServiceNameAndMapperTypeIn(String services, List<String> mapperTypes);
    List<XmlEntity> findByMapperTypeIn(List<String> mapperTypes);
//    Page<XmlEntity> findByMapperIdContainsAndMapperTypeIn(String mapperId, List<String> mapperTypes, Pageable pageable);
//
//    Page<XmlEntity> findByMapperTypeContains(String mapperId, Pageable pageable);
//
//    //    Page<XmlEntity> findByMapperBodyContains(String mapperId, Pageable pageable);
    Page<XmlEntity> findByMapperBodyContainsAndMapperTypeInOrderByServiceName(String mapperBody, List<String> mapperTypes, Pageable pageable);
    Stream<XmlEntity> findByMapperBodyContainsAndMapperTypeIn(String mapperBody, List<String> mapperTypes);
    Stream<XmlEntity> findByMapperBodyContains(String mapperBody);

    @Query("SELECT DISTINCT x.serviceName FROM XmlEntity x")
    List<String> findDistinctServiceNames();
}
