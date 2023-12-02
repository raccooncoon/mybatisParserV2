package com.example.mybatisparser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NodeRepository extends JpaRepository<NodeEntity, Long> {

    Page<NodeEntity> findByFirstIdIn(List<String> firstIds, Pageable pageable);
}
