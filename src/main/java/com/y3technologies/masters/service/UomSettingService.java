package com.y3technologies.masters.service;

import com.y3technologies.masters.dto.UomSettingDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.UomSettingFilter;
import com.y3technologies.masters.model.UomSetting;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UomSettingService {
	
	void save(UomSetting model);
	
	UomSettingDto getById(Long id);
	
	DataTablesOutput<UomSetting> query(DataTablesInput input);
	
	void updateStatus(Long id, Boolean status);

	List<UomSettingDto> getAllActiveUOM(String uomGroup);

	UomSettingDto getByUomCode(String uomCode);

	List<UomSettingDto> getByUomGroup(String uomGroup);

	List<UomSetting> findByFilter(UomSettingFilter uomSettingFilter);

	List<UomSetting> getAll(UomSettingFilter filter);

	int countByCondition (String uom, String uomGroup);

	List<UomSetting> findByUomAndUomGroup (String uom, String uomGroup);

	ExcelResponseMessage uploadExcel(MultipartFile file);

	List<UomSettingDto> getUomSettingDtoByUom(String uom);
}
