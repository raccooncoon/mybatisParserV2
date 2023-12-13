package com.example.mybatisparser.repository;

import com.example.mybatisparser.entity.JavaInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Stream;

public interface JavaInfoRepository extends JpaRepository<JavaInfoEntity, Long> {


    Stream<JavaInfoEntity> findByMethodCallsContainingAndServiceName(String methodCalls, String serviceName);
    Stream<JavaInfoEntity> findByMethodParametersContainingAndServiceName(String methodCalls, String serviceName);
    List<JavaInfoEntity> findByMethodCallsContainsAndClassFieldsContainsAndServiceName(String methodCall, String classField, String serviceName);



}
