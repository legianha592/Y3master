package com.y3technologies.masters.repository;

import com.y3technologies.masters.dto.DriverDto;
import com.y3technologies.masters.dto.TenantDetail;

import java.util.List;

public interface DriverRepositoryCustom {
    int countByCondition (DriverDto driverDto);

    List<Long> findDriverIdsByDriverName(Long tenantId, String driverName);

    List<TenantDetail> getListNoForTenantDTO(String sortParam);
}
