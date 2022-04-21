package com.y3technologies.masters.service;

import java.util.List;

import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.y3technologies.masters.dto.filter.ReasonCodeFilter;
import com.y3technologies.masters.model.ReasonCode;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface ReasonCodeService {
	
	void save(ReasonCode model);
	
	boolean existsById(Long id);
	
	List<ReasonCode> listByParam(ReasonCode milestone);

	DataTablesOutput<ReasonCode> query(DataTablesInput input);

	ReasonCode getById(Long id);
	
	void updateStatus(Long id, Boolean status);

	List<ReasonCode> findBySearch(ReasonCodeFilter filter);

	void populateSystemReasonCodeForUser(Long tenantId);

	List<ReasonCode> findByTenantId(Long tenantId, ReasonCodeFilter filter);

	int countByCondition(String reasonCode, String reasonDescription, Long tenantId);

	ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId);

	void downloadExcel(HttpServletResponse response, Long currentTenantId, ReasonCodeFilter filter);
}
