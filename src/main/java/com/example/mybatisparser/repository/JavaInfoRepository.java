package com.example.mybatisparser.repository;

import com.example.mybatisparser.entity.JavaInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JavaInfoRepository extends JpaRepository<JavaInfoEntity, Long> {


    List<JavaInfoEntity> findByMethodCallsContainingAndServiceName(String methodCalls, String serviceName);
    List<JavaInfoEntity> findByMethodCallsContainsAndClassFieldsContainsAndServiceName(String methodCall, String classField, String serviceName);

}
