package com.y3technologies.masters.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.dto.DropdownPartnerDto;
import com.y3technologies.masters.model.Partners;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PartnerRepositoryCustom {
    List<Partners> getAllByTenantIdAndPartnerType(Long tenantId, String partnerType);

    List<Long> findConsigneeIdsByConsigneeName(Long tenantId, String consignee);

    Page<DropdownPartnerDto> findByNameAndTypeSelectIdAndName(Long tenantId, String term, String type, Pageable pageable, BooleanExpression predicate);
}
