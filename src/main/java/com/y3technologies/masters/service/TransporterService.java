package com.y3technologies.masters.service;

import com.y3technologies.masters.dto.DropdownPartnerDto;
import com.y3technologies.masters.dto.TransporterDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.model.Partners;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface TransporterService {

	Partners save(TransporterDto dto);

	TransporterDto getById(Long id, Long tenantId);

	Partners findById(Long id);

	Page<TransporterDto> findBySearch(PartnersFilter filter, Long tenantUserId, String token);

	void updateStatus(Long id, Boolean status);

	List<Partners> findByFilter(PartnersFilter filter, Long tenantUserId, boolean isCallFromFindByFilterController);

	DataTablesOutput<Partners> query(DataTablesInput input);

    List<Partners> findByTenantId(Long currentTenantId);

	List<Partners> getPartnerByTenantId(Long currentTenantId, String partnerType);

	List<Partners> getTranporterToExport(Long tenantId, Long tenantUserId, PartnersFilter filter);

	int countByTransporterName(String transporterName);

	ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId, Long currentTenantUserId);

	void downloadExcel(PartnersFilter filter, HttpServletResponse response, Long currentTenantId, Long tenantUserId);

	Partners findByName(Long tenantId, String transporterName);

	Page<DropdownPartnerDto> findByTransporterName(Long tenantId, Long tenantUserId, String term, Pageable pageable);
}
