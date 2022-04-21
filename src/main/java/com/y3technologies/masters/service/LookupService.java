package com.y3technologies.masters.service;

import java.util.List;
import java.util.Map;

import com.y3technologies.masters.dto.LookupDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import org.springframework.data.domain.Page;

import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.model.Lookup;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface LookupService {

	List<Lookup> listByParam(Lookup lookup);

	List<Lookup> getAllLookup(Long tenantId);

	List<Lookup> findByLookupDescriptionAndTenantId(String lookupDescription, Long tenantId);

	Page<Lookup> findBySearch(LookupFilter filter);

	Lookup findById(Long id);

	Lookup save(Lookup lookup);

	void updateStatus(Long id, Boolean status);

	List<Lookup> findByFilter(LookupFilter filter);

	Lookup findByLookupCodeLookupTypeTenantId(LookupFilter filter);

	List<LookupDto> listLookupDtoByLookupType (String lookupType);

	List<Lookup> findByLookupType (String lookupType);

	Lookup findByLookupDescriptionLookupTypeTenantId(LookupFilter filter);

	List<Lookup> findByLookupTypeTenantId(String lookupType, Long tenantId);

	ExcelResponseMessage uploadFile(MultipartFile file, Long tenantId);

	void downloadExcel(HttpServletResponse response, LookupFilter filter);

	List<Lookup> findLookupByTypeAndCodeAndTenantId(LookupFilter filter);

	Map<String, Map<String, String>> findLookupByTypeAndCode(List<String> lookupTypes, List<String> lookupCodes);

	String getLookupDescriptionFromLookupCode(String lookupType, String lookupCode, Map<String, Map<String, String>> mapLookup);

	boolean existById(Long lookupId);
}
