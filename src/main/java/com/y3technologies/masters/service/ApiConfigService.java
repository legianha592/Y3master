package com.y3technologies.masters.service;

import com.y3technologies.masters.dto.apiConfig.AddConfigRequest;
import com.y3technologies.masters.dto.apiConfig.ApiConfigDTO;
import com.y3technologies.masters.dto.apiConfig.ApiConfigFilter;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.apiConfig.ApiConfig;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletResponse;

public interface ApiConfigService {

    ApiConfig findById(Long id);

    ApiConfig save(AddConfigRequest request, Lookup lookup, Partners customer, Long tenantId);

    ApiConfig update(ApiConfig config);

    String generateApiKey();

    boolean existsByLookupAndCustomer(Lookup lookup, Partners customer);

    Page<ApiConfigDTO> findAllApiConfigWithPagination(ApiConfigFilter filter, Long tenantId);

    void downloadExcel(ApiConfigFilter filter, Long tenantId, HttpServletResponse response);

    boolean checkExistApiConfig(ApiConfigFilter filter);
}
