package com.y3technologies.masters.service;

import java.util.List;

import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.EquipmentFilter;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.y3technologies.masters.model.Equipment;
import org.springframework.web.multipart.MultipartFile;

public interface EquipmentService {
	
	void save(Equipment model);
	
	boolean existsById(Long id);
	
	List<Equipment> listByParam(Equipment model);

	DataTablesOutput<Equipment> query(DataTablesInput input);

	Equipment getById(Long id);
	
	void updateStatus(Long id, Boolean status);

	List<Equipment> findByTenantId(Long tenantId, EquipmentFilter filter);

    int countByUnitAidc1(String unitAidc1, Long currentTenantId);

	ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId);
}
