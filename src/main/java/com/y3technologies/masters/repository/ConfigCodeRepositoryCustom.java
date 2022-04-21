package com.y3technologies.masters.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.y3technologies.masters.model.ConfigCode;

public interface ConfigCodeRepositoryCustom {

    Page<ConfigCode> findAllConfigCodeWithPagination(Pageable pageable, Map<String, String> map);
}
