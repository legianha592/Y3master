package com.y3technologies.masters.service;

import com.y3technologies.masters.dto.ConsigneeDto;
import com.y3technologies.masters.dto.ConsigneePlaceDto;
import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.dto.PartnersDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.PartnerLocationFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.dto.filter.PlacesFilter;
import com.y3technologies.masters.model.Partners;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author Sivasankari Subramaniam
 */
public interface ConsigneeService {
    Partners save(ConsigneeDto consigneeDto);

    ConsigneeDto findById(Long id);

    Page<ConsigneeDto> findBySearch(PartnersFilter filter, Long tenantUserId);

    Partners update(Long id, ConsigneeDto consigneeDto);

    ConsigneeDto updateStatus(Long id, Boolean isActive);

    Long savePlace(ConsigneePlaceDto consigneePlaceDto);

    Page<ConsigneePlaceDto> findByPlacesSearch(PlacesFilter placesFilter, Long tenantUserId);

    ConsigneePlaceDto findByPlaceId(Long id);

    Long updatePlace(ConsigneePlaceDto consigneePlaceDto);

    Long updatePlaceStatus(Long id, Boolean isActive);

    List<ConsigneeDto> findConsigneeByCustomerId(Long id, Long tenantId);

    List<LocationDto> findLocationsByConsigneeId(PartnerLocationFilter partnerLocationFilter, Boolean isSearchDropdown, Long tenantUserId, boolean isCallFromController);

    List<ConsigneePlaceDto> findPlacesByLocationId(PartnerLocationFilter partnerLocationFilter);

    List<Partners> findByFilter(PartnersFilter filter);

    Long createOrFind(ConsigneeDto consigneeDto);

    List<Partners> getConsigneeToExport(Long tenantId, Long tenantUserId, PartnersFilter filter);

    int countByConsigneeName(String consigneeName, Long tenantId, Long customerId);

    ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId, Long currentTenantUserId);

    void downloadExcel(HttpServletResponse response, Long currentTenantId, Long tenantUserId, PartnersFilter filter);

    List<Long> findConsigneeIdsByConsigneeName(Long tenantId, String consignee);

    Map<Long, List<LocationDto>> findLocationsByMultipleConsigneeId(PartnerLocationFilter partnerLocationFilter, Boolean isSearchDropdown);

    List<PartnersDto> createOrUpdateMultiple(List<ConsigneeDto> consigneeDtoList);
}
