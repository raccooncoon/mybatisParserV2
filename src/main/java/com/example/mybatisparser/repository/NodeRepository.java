package com.example.mybatisparser.repository;

import com.example.mybatisparser.entity.NodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Stream;

public interface NodeRepository extends JpaRepository<NodeEntity, Long> {

    Page<NodeEntity> findByFirstIdIn(List<String> firstIds, Pageable pageable);
    List<NodeEntity> findByFirstIdIn(List<String> firstIds);
    Stream<NodeEntity> findByFirstId(String firstId);
}
