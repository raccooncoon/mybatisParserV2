package com.example.mybatisparser.repository;

import com.example.mybatisparser.entity.XmlEntity;
import com.example.mybatisparser.entity.XmlEntityPK;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface XmlRepository extends JpaRepository<XmlEntity, XmlEntityPK> {

    List<XmlEntity> findByServiceNameAndMapperTypeIn(String services, List<String> mapperTypes);
//    Page<XmlEntity> findByMapperIdContainsAndMapperTypeIn(String mapperId, List<String> mapperTypes, Pageable pageable);
//
//    Page<XmlEntity> findByMapperTypeContains(String mapperId, Pageable pageable);
//
//    //    Page<XmlEntity> findByMapperBodyContains(String mapperId, Pageable pageable);
    Page<XmlEntity> findByMapperBodyContainsAndMapperTypeIn(String mapperId, List<String> mapperTypes, Pageable pageable);

    @Query("SELECT DISTINCT x.serviceName FROM XmlEntity x")
    List<String> findDistinctServiceNames();
}
