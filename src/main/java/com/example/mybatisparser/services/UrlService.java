package com.example.mybatisparser.services;

import com.example.mybatisparser.recode.UrlDTO;
import com.example.mybatisparser.repository.UrlRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;


    public Page<UrlDTO> findByServiceNameAndClassNameAndMethodName(String servicesName, String mapperNameSpace, String mapperId, org.springframework.data.domain.Pageable pageable) {
        return urlRepository.findByServiceNameAndClassNameAndMethodName(servicesName, mapperNameSpace, mapperId, pageable)
                .map(urlEntity -> new UrlDTO(
                        urlEntity.getId(),
                        urlEntity.getServiceName(),
                        urlEntity.getClassName(),
                        urlEntity.getMethodName(),
                        urlEntity.getUrl()
                ));
    }
}

