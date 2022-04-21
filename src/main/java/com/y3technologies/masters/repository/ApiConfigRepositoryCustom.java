package com.y3technologies.masters.repository;


import com.y3technologies.masters.dto.apiConfig.ApiConfigFilter;
import com.y3technologies.masters.model.apiConfig.ApiConfig;

import org.springframework.data.domain.Page;

public interface ApiConfigRepositoryCustom{

    Page<ApiConfig> findAllApiConfigWithPagination(ApiConfigFilter filter, Long tenantId);

}
