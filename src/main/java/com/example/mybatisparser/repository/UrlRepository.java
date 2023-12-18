package com.example.mybatisparser.repository;

import com.example.mybatisparser.entity.UrlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    Page<UrlEntity> findByServiceNameAndClassNameAndMethodName(String servicesName, String className, String methodName, Pageable pageable);
}
