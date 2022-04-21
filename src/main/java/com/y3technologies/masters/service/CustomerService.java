package com.y3technologies.masters.service;

import com.y3technologies.masters.dto.CustomerDto;
import com.y3technologies.masters.dto.DropdownPartnerDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.model.Partners;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface CustomerService {

	Partners save(CustomerDto dto);

	CustomerDto getById(Long id);

	Partners findById(Long id);

	Page<Partners> findBySearch(PartnersFilter filter, Long tenantUserId, String token);

	void updateStatus(Long id, Boolean status);

	List<Partners> findByFilter(PartnersFilter filter, Long tenantUserId, boolean isCallFromFindByFilterController, String token);

    List<Partners> getAllByTenantId(Long tenantId);

    int countByCustomerName(String customerName, Long currentTenantId);

	ExcelResponseMessage uploadExcel (MultipartFile file, Long tenantId);

    void downloadExcel (PartnersFilter filter, HttpServletResponse response, Long tenantUserId);

    Partners findByName(Long tenantId, String customerName);

	Page<DropdownPartnerDto> findByCustomerName(Long tenantId, Long tenantUserId, String term, Pageable pageable);

    boolean existById(Long customerId);
}
