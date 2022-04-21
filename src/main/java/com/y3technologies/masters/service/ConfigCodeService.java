package com.y3technologies.masters.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.y3technologies.masters.dto.ConfigCodeDto;
import com.y3technologies.masters.dto.filter.ConfigCodeFilter;
import com.y3technologies.masters.model.ConfigCode;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface ConfigCodeService {

	ConfigCode save(ConfigCode model);

	void updateStatus(Long id, Boolean status);

	boolean existsById(Long id);

	Optional<ConfigCode> getById(Long id);

	List<ConfigCode> findAll();

	void delete(ConfigCode configCode);

	List<ConfigCode> findByUsageLevel(String usageLevel);

	Page<ConfigCode> findAllConfigCodeWithPagination(Pageable pageable, Map<String, String> map);

	ConfigCodeDto getByCode(String code);

	ConfigCodeDto updateTptRequestSeqGenConfigValue(String code, String incrementBy);

	List<ConfigCode> getAll();

	int countByCondition(String configCode);

	ExcelResponseMessage uploadFile(MultipartFile file);

	void downloadExcel(HttpServletResponse response, ConfigCodeFilter filter);
}
