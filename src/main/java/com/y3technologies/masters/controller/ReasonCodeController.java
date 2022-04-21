package com.y3technologies.masters.controller;

import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.ReasonCodeFilter;
import com.y3technologies.masters.dto.table.ReasonCodeTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.ReasonCode;
import com.y3technologies.masters.service.ReasonCodeService;
import com.y3technologies.masters.util.SortingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * Reason Code Controller
 * 
 * @author suxia
 * 
 */

@RestController
@RequestMapping("/${api.version.masters}/reasonCode")
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class ReasonCodeController extends BaseTableController<ReasonCodeTableDto, ReasonCode>{
	
	@Autowired
	private ReasonCodeService reasonCodeService;

	@PostMapping(value = "/create")
	public @ResponseBody ResponseEntity create(@RequestBody @Valid ReasonCode model) throws Exception {
		try {
			reasonCodeService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.reasonCode.duplicate");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model.getId());
	}
	
	@PostMapping(value = "/update")
	public @ResponseBody ResponseEntity update(@RequestBody @Valid ReasonCode model) {
		if(model.getId() == null || !reasonCodeService.existsById(model.getId())) {
			throw new TransactionException("exception.reasonCode.invalid");
		}
		try {
			reasonCodeService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.reasonCode.duplicate");
		} catch (ObjectOptimisticLockingFailureException  e) {
			throw new TransactionException("exception.reasonCode.version");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}
	
	@GetMapping(value = "/retrieve")
	public @ResponseBody ResponseEntity retrieve(@RequestParam("id") Long id) {
		ReasonCode model = reasonCodeService.getById(id);
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}
	
	@GetMapping(value = "/listByParam")
	public @ResponseBody ResponseEntity listByParam(@RequestBody ReasonCode model) {
		List<ReasonCode> list = reasonCodeService.listByParam(model);
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@GetMapping("/findBySearch")
	public ResponseEntity findBySearch(ReasonCodeFilter filter) {

		List<ReasonCode> list = reasonCodeService.findBySearch(filter);
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}
	
	@PostMapping(value = "/query")
    public @ResponseBody ResponseEntity loadByAjax(@RequestBody DataTablesInput input) {
		SortingUtils.addDefaultSortExtra(input);
		DataTablesOutput<ReasonCode> table = reasonCodeService.query(input);
		DataTablesOutput<ReasonCodeTableDto> tableDto = convertToTableDto(table);
		return ResponseEntity.status(HttpStatus.OK).body(tableDto);
	}
	
	@GetMapping(value = "/updateStatus")
	public @ResponseBody ResponseEntity updateStatus(@RequestParam("id") Long id, @RequestParam("status") Boolean status) {
		try {
			reasonCodeService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			throw new TransactionException("exception.reasonCode.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PutMapping(value = "/addReasonCodesForNewUser")
	public @ResponseBody ResponseEntity addReasonCodesForNewUser(@RequestParam("tenantId") Long tenantId) {
		if (null == tenantId) {
			throw new TransactionException("exception.milestone.tenant.id.null");
		}
		reasonCodeService.populateSystemReasonCodeForUser(tenantId);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@Override
	public ReasonCodeTableDto mapTableDto(ReasonCode data) {
		ReasonCodeTableDto tableDto = new ReasonCodeTableDto();
		BeanCopier copier = BeanCopier.create(ReasonCode.class, ReasonCodeTableDto.class, false);
		copier.copy(data,tableDto , null);
		return tableDto;
	}

	@PostMapping(value = "/uploadFiles")
	public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file){
		ExcelResponseMessage responseMessage = reasonCodeService.uploadExcel(file, getCurrentTenantId());
		return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
	}

	@PostMapping(value = "/downloadExcel")
	public void downloadExcel(HttpServletResponse response, @RequestBody ReasonCodeFilter filter){
		reasonCodeService.downloadExcel(response, getCurrentTenantId(), filter);
	}
}
