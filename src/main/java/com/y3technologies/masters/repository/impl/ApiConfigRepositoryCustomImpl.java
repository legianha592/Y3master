package com.y3technologies.masters.repository.impl;


import com.y3technologies.masters.dto.apiConfig.ApiConfigFilter;
import com.y3technologies.masters.model.apiConfig.ApiConfig;
import com.y3technologies.masters.repository.ApiConfigRepositoryCustom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Objects;

@Repository
public class ApiConfigRepositoryCustomImpl implements ApiConfigRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public Page<ApiConfig> findAllApiConfigWithPagination(ApiConfigFilter filter, Long tenantId) {

        StringBuilder query = new StringBuilder(
                        "FROM api_config a\n" +
                        "LEFT JOIN partners b ON a.customer_id = b.id\n" +
                        "LEFT JOIN lookup c ON a.lookup_id = c.id\n" +
                        "WHERE a.tenant_id = :tenantId\n" +
                        "AND c.lookup_type = 'API_CONFIG'\n");

        // filter by list api id
        if (Objects.requireNonNull(filter.getApiId()).size() > 0) {
            StringBuilder apiId = new StringBuilder();
            filter.getApiId().forEach(id -> {
                apiId.append(id);
                apiId.append(",");
            });
            apiId.deleteCharAt(apiId.length() - 1);
            query.append("AND c.id IN (");
            query.append(apiId);
            query.append(")\n");
        }

        // filter by customer name
        if (StringUtils.isNotBlank(filter.getCustomerName())) {
            query.append("AND b.partner_name like :customerName\n");
        }

        // filter by api type
        if (StringUtils.isNotBlank(filter.getApiType())) {
            query.append("AND c.lookup_description = :lookupDescription\n");
        }

        // sorting
        if (filter.getSort().size() > 0) {
            query.append("ORDER BY");
            query.append(" ");
            filter.getSort().forEach(order -> {
                switch (order.getProperty()) {
                    case "apiName":
                        query.append("c.lookup_code");
                        break;
                    case "customerName":
                        query.append("b.partner_name");
                        break;
                    case "url":
                        query.append("a.url");
                        break;
                    case "urn":
                        query.append("a.urn");
                        break;
                    case "id":
                        query.append("a.id");
                        break;
                    default:
                        query.append("a.created_date");
                        break;
                }
                query.append(" ");
                query.append(order.getDirection());
                query.append(", ");
            });
            query.deleteCharAt(query.length() - 2);
        }

        StringBuilder totalQueryStr = new StringBuilder("SELECT COUNT(a.id) ").append(query);

        if(filter.getPageSize() > 0) {
            // get by page size and page no
            query.append("LIMIT");
            query.append(" ");
            int from = filter.getPageNo() * filter.getPageSize();
            query.append(from);
            query.append(",");
            query.append(filter.getPageSize());
        }

        StringBuilder fetchDataQueryStr = new StringBuilder("SELECT a.* ").append(query);

        // create query and set param
        Query totalQuery = entityManager.createNativeQuery(totalQueryStr.toString());
        Query fetchDataQuery = entityManager.createNativeQuery(fetchDataQueryStr.toString(), ApiConfig.class);
        totalQuery.setParameter("tenantId", tenantId);
        fetchDataQuery.setParameter("tenantId", tenantId);
        if (StringUtils.isNotBlank(filter.getCustomerName())) {
          totalQuery.setParameter("customerName", "%" + filter.getCustomerName() + "%");
          fetchDataQuery.setParameter("customerName", "%" + filter.getCustomerName() + "%");
        }
        if (StringUtils.isNotBlank(filter.getApiType())) {
          totalQuery.setParameter("lookupDescription", filter.getApiType());
          fetchDataQuery.setParameter("lookupDescription", filter.getApiType());
        }

        List<ApiConfig> apiConfigs = (List<ApiConfig>) fetchDataQuery.getResultList();
        Long total = Long.valueOf(totalQuery.getSingleResult().toString());
        if(filter.getPageSize() <= 0) {
          filter.setPageSize(total.intValue());
        }
        return new PageImpl<ApiConfig>(apiConfigs, PageRequest.of(filter.getPageNo(), filter.getPageSize()), total);
    }
}
