package com.example.mybatisparser.services;

import com.example.mybatisparser.entity.NodeEntity;
import com.example.mybatisparser.recode.UrlDTO;
import com.example.mybatisparser.repository.JavaInfoRepository;
import com.example.mybatisparser.repository.NodeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
public class UrlService {

    private final NodeRepository nodeRepository;
    private final JavaInfoRepository javaInfoRepository;

    public Page<UrlDTO> getMapperId(String ServiceName, String mapperId, Pageable pageable) {

        Stream<String> methodCalls = javaInfoRepository.findByMethodCallsContainingAndServiceName(mapperId, ServiceName)
                .map(javaInfoEntity -> javaInfoEntity.getId().toString());

        Stream<String> methodParams = javaInfoRepository.findByMethodParametersContainingAndServiceName(mapperId, ServiceName)
                .map(javaInfoEntity -> javaInfoEntity.getId().toString());

        List<String> selectMapperIds = Stream.concat(methodCalls, methodParams).toList();

        Page<NodeEntity> byFirstIdIn = nodeRepository.findByFirstIdIn(selectMapperIds, pageable);

        log.info("list : {}", byFirstIdIn);

        List<UrlDTO> list = byFirstIdIn.stream().map(nodeEntity -> {
            log.info("nodeEntity : {}", nodeEntity.getId());
            log.info("nodeEntity : {}", nodeEntity.getFirstId());
            log.info("nodeEntity : {}", nodeEntity.getLastId());

            return new UrlDTO(
                    nodeEntity.getId(),
                    nodeEntity.getUrl(),
                    selectMapperIds,
                    nodeEntity.getIds(),
                    nodeEntity.getPackageName(),
                    nodeEntity.getClassName(),
                    nodeEntity.getMethodName(),
                    nodeEntity.getServiceName(),
                    nodeEntity.getFileName()
            );

        }).toList();

        return new PageImpl<>(list, byFirstIdIn.getPageable(), byFirstIdIn.getTotalElements());
    }

}

