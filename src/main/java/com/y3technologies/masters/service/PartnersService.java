package com.y3technologies.masters.service;

import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.y3technologies.masters.dto.PartnersDto;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.model.Partners;


public interface PartnersService {

	Partners save(PartnersDto dto);

	PartnersDto getById(Long id);

	Partners findById(Long id);

	DataTablesOutput<Partners> query(DataTablesInput input);

	void updateStatus(Long id, Boolean status);

	List<PartnersDto> getByTenantIdAndPartnerTypes(Long tenantId, String partnerType);

	List<Partners> findByFilter(PartnersFilter filter);

	List<Partners> findByFilterIgnoreCase(PartnersFilter filter);

	Partners findByPartnerCodeAndTenantId(String partnerCode, Long tenantId);

	List<Partners> findByPartnerNameAndLookupCode(String partnerName, String lookupCode);

	List<Partners> findByTenantIdAndPartnerNameAndLookupCode (Long tenantId, String partnerName, String lookupCode);

    Partners findByPartnerNameAndTenantId(String partnerName, Long tenantId);
}
