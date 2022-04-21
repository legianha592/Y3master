package com.y3technologies.masters.controller;

import java.util.ArrayList;
import java.util.List;

import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.TenantUserInfoDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static org.springframework.data.domain.Sort.Direction.fromString;

public abstract class BaseController {

    public static final String PAGE = "PAGE";
    public static final String SIZE = "SIZE";
    public static final String SORTBY = "SORTBY";
    public static final String SEARCHBY = "SEARCHBY";
    public static final String SEARCHVALUE = "SEARCHVALUE";

    protected Pageable getPageable(int page, int size, String sortBy) {
        page = Math.max(page, 0);
        size = size <= 0? 20: size;

        String[] result = sortBy.split("\\|");

        List<Sort.Order> orders = new ArrayList<>();
        for (String order : result) {
            orders.add(new Sort.Order(order.toUpperCase().contains("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, order.split(",")[0]));
        }
        orders.add(new Sort.Order(fromString(AppConstants.CommonSortDirection.DESCENDING), AppConstants.CommonPropertyName.ID));
        return PageRequest.of(page, size, Sort.by(orders));
    }

    protected Long getCurrentTenantId() {
        SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
                getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
        if (sessionUserInfoDTO != null) {
            return sessionUserInfoDTO.getAasTenantId();
        }
        return null;
    }

    protected Long getCurrentTenantUserId() {
        SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
                getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
        if (sessionUserInfoDTO != null) {
            return sessionUserInfoDTO.getAasTenantUserId();
        }
        return null;
    }

    protected TenantUserInfoDTO getTenantUserInfoDTO() {
        TenantUserInfoDTO tenantUserInfoDTO = (TenantUserInfoDTO) RequestContextHolder.getRequestAttributes().
                getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
        if (tenantUserInfoDTO != null) {
            return tenantUserInfoDTO;
        }
        return null;
    }
}
