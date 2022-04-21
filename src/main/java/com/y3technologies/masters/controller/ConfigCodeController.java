package com.y3technologies.masters.controller;


import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.dto.ConfigCodeDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.ConfigCodeFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.ConfigCode;
import com.y3technologies.masters.model.ConfigCodeUsageLevelEnum;
import com.y3technologies.masters.service.ConfigCodeService;
import com.y3technologies.masters.util.MessagesUtilities;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Config Code Controller
 * 
 * @author suxia
 * 
 */

@RestController
@RequestMapping(value = "/${api.version.masters}/configCode", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class ConfigCodeController extends BaseController {

	@Autowired
	private ConfigCodeService configCodeService;

	@Autowired
	private MessagesUtilities messagesUtilities;

	@Autowired
	private MastersApplicationPropertiesConfig mastersApplicationPropertiesConfig;

	@PostMapping("/create")
	public ResponseEntity create(@RequestBody @Valid ConfigCodeDto dto) {
		ConfigCode model = new ConfigCode();
		try {
			if(!validateUsageLevel(dto.getUsageLevel())){
				throw new TransactionException("exception.usageLevel.invalid");
			}

			model.setCode(dto.getCode());
			model.setDescription(dto.getDescription());
			model.setUsageLevel(dto.getUsageLevel().toUpperCase());
			model.setActiveInd(dto.getActiveInd());
			model.setConfigValue(dto.getConfigValue());

			configCodeService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.configCode.duplicate");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@GetMapping("/retrieve")
	public ResponseEntity retrieve(@RequestParam("id") Long id) {
		Optional<ConfigCode> model = configCodeService.getById(id);
		if(model.isEmpty()) throw new TransactionException("exception.configCode.invalid");
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@PostMapping("/update")
	public ResponseEntity update(@RequestBody @Valid ConfigCodeDto dto) {
		ConfigCode model = null;
		try {
			if(dto.getId() == null) throw new TransactionException("exception.configCode.invalid");

			Optional<ConfigCode> savedConfigCode = configCodeService.getById(dto.getId());
			if(savedConfigCode.isEmpty()) throw new TransactionException("exception.configCode.invalid");

			if(!validateUsageLevel(dto.getUsageLevel())){
				throw new TransactionException("exception.usageLevel.invalid");
			}

			model = savedConfigCode.get();
			model.setCode(dto.getCode());
			model.setDescription(dto.getDescription());
			model.setUsageLevel(dto.getUsageLevel().toUpperCase());
			model.setActiveInd(dto.getActiveInd());
			model.setConfigValue(dto.getConfigValue());
			configCodeService.save(model);

		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.configCode.duplicate");
		} catch (ObjectOptimisticLockingFailureException  e) {
			throw new TransactionException("exception.configCode.version");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@GetMapping("/updateStatus")
	public ResponseEntity updateStatus(@RequestParam("id") Long id, @RequestParam("status") Boolean status) {
		if(id == null || !configCodeService.existsById(id)) {
			throw new TransactionException("exception.configCode.invalid");
		}

		try {
			configCodeService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			new TransactionException("exception.configCode.invalid");
		} catch (ObjectOptimisticLockingFailureException e) {
			throw new TransactionException("exception.configCode.version");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PostMapping("/findAll")
	public ResponseEntity findAll() {
		List<ConfigCode> list = configCodeService.findAll();
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@Operation(description = "Get Config Code List by Usage Level")
	@GetMapping("/findByUsageLevel")
	public ResponseEntity findByUsageLevel(@RequestParam(name = "usageLevel", required = true) String usageLevel) {
		List<String> enumList = new ArrayList<>();
		for (ConfigCodeUsageLevelEnum e: ConfigCodeUsageLevelEnum.values()) {
			enumList.add(messagesUtilities.getResourceMessage(e.getUsageLevel(), LocaleContextHolder.getLocale()));
		}
		if(!validateUsageLevel(usageLevel)){
			throw new TransactionException("exception.usageLevel.invalid");
		}
		List<ConfigCode> list = configCodeService.findByUsageLevel(usageLevel);
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@PostMapping(path = "/filterBy", params = {PAGE, SIZE, SORTBY})
	public ResponseEntity<Page<ConfigCode>> filterBy(
			@RequestParam(value = PAGE, defaultValue = "0") int page,
			@RequestParam(value = SIZE, defaultValue = "20") int size,
			@RequestParam(value = SORTBY, defaultValue = "code,asc|activeInd,asc", required = false) String sortBy,
			@RequestBody ConfigCode configCode){

		Map<String, String> filterParams = new HashMap<>();
		if(configCode.getUsageLevel() != null && !configCode.getUsageLevel().equals("")){
			filterParams.put("usageLevel", configCode.getUsageLevel());
		}

		if(configCode.getCode() != null && !configCode.getCode().equals("")){
			filterParams.put("code", configCode.getCode());
		}

		if(configCode.getDescription() != null && !configCode.getDescription().equals("")){
			filterParams.put("description", configCode.getDescription());
		}

		if(configCode.getConfigValue() != null && !configCode.getConfigValue().equals("")){
			filterParams.put("configValue", configCode.getConfigValue());
		}

		Page<ConfigCode> results = configCodeService.findAllConfigCodeWithPagination(
				getPageable(page, size, sortBy), filterParams);
		return ResponseEntity.status(HttpStatus.OK).body(results);
	}

	@Operation(description = "Get Usage Level List")
	@GetMapping("/usageLevel/findAll")
	public ResponseEntity retrieveUsageLevel() {
		List<String> enumList = new ArrayList<>();
		for (ConfigCodeUsageLevelEnum e: ConfigCodeUsageLevelEnum.values()) {
			enumList.add(messagesUtilities.getResourceMessage(e.getUsageLevel(), LocaleContextHolder.getLocale()));
		}
		return ResponseEntity.status(HttpStatus.OK).body(enumList);
	}

	private boolean validateUsageLevel(String usageLevel){
		List<String> enumList = new ArrayList<>();
		for (ConfigCodeUsageLevelEnum e: ConfigCodeUsageLevelEnum.values()) {
			enumList.add(messagesUtilities.getResourceMessage(e.getUsageLevel(), LocaleContextHolder.getLocale()));
		}
		if (usageLevel == null) {
			return false;
		}
		String[] itemArr = usageLevel.replace("|", "::").split("::");
		for (String item : itemArr)
		{
			if (!enumList.contains(item.toUpperCase()))
			{
				return false;
			}
		}
		return true;
	}

	@GetMapping("/retrieveByCode")
	public ResponseEntity retrieveByCode(@RequestParam("code") String code) {
		ConfigCodeDto model = configCodeService.getByCode(code);
		if(null == model) throw new TransactionException("exception.configCode.invalid");
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@PutMapping("/updateTptRequestSeqGenConfigValue")
	public ResponseEntity updateTptRequestSeqGenConfigValue(@RequestParam("code") String code, @RequestParam("configValue") String incrementValue) {
		ConfigCodeDto model = configCodeService.updateTptRequestSeqGenConfigValue(code, incrementValue);
		if(null == model) throw new TransactionException("exception.configCode.invalid");
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@PostMapping(value = "/uploadFiles")
	public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
		ExcelResponseMessage excelResponseMessage = configCodeService.uploadFile(file);
		return ResponseEntity.status(HttpStatus.OK).body(excelResponseMessage);
	}

	@PostMapping(value = "/downloadExcel")
	public void downloadExcel(HttpServletResponse response, @RequestBody ConfigCodeFilter filter) {
		configCodeService.downloadExcel(response, filter);
	}
}
