package com.y3technologies.masters.service;

import com.y3technologies.masters.dto.PartnerConfigDto;
import com.y3technologies.masters.dto.filter.PartnerConfigFilter;
import com.y3technologies.masters.model.PartnerConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import java.util.List;

public interface PartnerConfigService {
	
	void  save(PartnerConfig model);
	
	PartnerConfigDto getById(Long id);

	PartnerConfig findById(Long id);

	List<PartnerConfigDto> getAllPartnerConfigs(Long partnerId);

	void updateStatus(Long id, Boolean status);

	Page<PartnerConfig> findBySearch(PartnerConfigFilter filter);

}
