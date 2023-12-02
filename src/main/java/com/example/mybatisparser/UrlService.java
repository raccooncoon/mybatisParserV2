package com.example.mybatisparser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UrlService {

    private final NodeRepository nodeRepository;
    private final JavaInfoRepository javaInfoRepository;

    public Page<UrlDTO> getMapperId(String ServiceName, String mapperId, Pageable pageable) {

        List<String> javaIdList = javaInfoRepository.findByMethodCallsContainingAndServiceName(mapperId, ServiceName)
                .stream().map(javaInfoEntity -> {
                    log.info("javaInfoEntity : {}", javaInfoEntity.getId());
                    log.info("javaInfoEntity : {}", javaInfoEntity.getMethodCalls());
                    log.info("javaInfoEntity : {}", javaInfoEntity.getServiceName());
                    return javaInfoEntity.getId().toString();
                }).toList();


        Page<NodeEntity> byFirstIdIn = nodeRepository.findByFirstIdIn(javaIdList, pageable);

        log.info("list : {}", byFirstIdIn);

        List<UrlDTO> list = byFirstIdIn.stream().map(nodeEntity -> {
            log.info("nodeEntity : {}", nodeEntity.getId());
            log.info("nodeEntity : {}", nodeEntity.getFirstId());
            log.info("nodeEntity : {}", nodeEntity.getLastId());
            log.info("nodeEntity : {}", nodeEntity.getServiceName());

            JavaInfoEntity javaInfoEntity = javaInfoRepository.findById(Long.parseLong(nodeEntity.getLastId())).orElseThrow(
                    () -> new IllegalArgumentException("해당하는 JavaInfo가 없습니다.")
            );


            return new UrlDTO(nodeEntity.getId(), javaInfoEntity.getServiceName(), javaInfoEntity, extractUrl(javaInfoEntity));

        }).toList();

        return new PageImpl<>(list, byFirstIdIn.getPageable(), byFirstIdIn.getTotalElements());
    }



    // URL 추출 메서드
    public String extractUrl(JavaInfoEntity javaInfoEntity) {
        String baseUrl = ""; // 기본 URL 경로를 초기화

        // 클래스 어노테이션 파싱
        if (javaInfoEntity.getClassAnnotations() != null) {
            String[] classAnnotationsArray = javaInfoEntity.getClassAnnotations().split(",");
            for (String annotation : classAnnotationsArray) {
                if (annotation.trim().startsWith("@RequestMapping")) {
                    // @RequestMapping 어노테이션을 찾았을 때, URL 경로를 추출
                    String[] parts = annotation.split("\"");
                    if (parts.length >= 2) {
                        baseUrl = parts[1];
                    }
                }
            }
        }

        // 메서드 어노테이션 파싱
        if (javaInfoEntity.getMethodAnnotations() != null) {
            String[] methodAnnotationsArray = javaInfoEntity.getMethodAnnotations().split(",");
            for (String annotation : methodAnnotationsArray) {
                if (annotation.trim().startsWith("@RequestMapping")) {
                    // @RequestMapping 어노테이션을 찾았을 때, URL 경로를 추출
                    String[] parts = annotation.split("\"");
                    if (parts.length >= 2) {
                        baseUrl += parts[1]; // 클래스 URL과 메서드 URL을 연결
                    }
                }
            }
        }

        return baseUrl;
    }

}

