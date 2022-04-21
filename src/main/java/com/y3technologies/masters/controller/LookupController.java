package com.y3technologies.masters.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.ListingFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.util.MessagesUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.y3technologies.masters.dto.LookupDto;
import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.service.LookupService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lookup Controller
 * 
 * @author suxia
 * 
 */

@RestController
@RequestMapping(value = "/${api.version.masters}/lookup", consumes = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.ALL_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class LookupController extends BaseController{

	private final LookupService lookupService;

	@Autowired
	MessagesUtilities messagesUtilities;

	@PostMapping(value = "/listByParam")
	public @ResponseBody ResponseEntity listByParam(@RequestBody Lookup lookup) {
		List<Lookup> list = lookupService.listByParam(lookup);
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@GetMapping("/findBySearch")
	public ResponseEntity<Page<Lookup>> findBySearch(LookupFilter filter) {
		filter.setTenantId(getCurrentTenantId());
		return ResponseEntity.status(HttpStatus.OK).body(lookupService.findBySearch(filter));
	}

	@PostMapping("/create")
	public ResponseEntity create(@RequestBody @Valid LookupDto lookupDto) {

		Lookup lookup = new Lookup();

		try {
			// Check if duplicate with default lookup
			List<Lookup> existingLookupList = lookupService.getAllLookup(0L);
			existingLookupList.forEach(i -> {
				if (i.getLookupCode().equals(lookupDto.getLookupCode())
						&& i.getLookupType().equals(lookupDto.getLookupType())) {
					throw new TransactionException("exception.lookup.duplicate");
				}
			});
			BeanCopier copier = BeanCopier.create(LookupDto.class, Lookup.class, false);
			copier.copy(lookupDto, lookup, null);

			lookupService.save(lookup);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.lookup.duplicate");
		}

		return ResponseEntity.status(HttpStatus.OK).body(lookup.getId());
	}

	@GetMapping("/retrieve/{id}")
	public ResponseEntity findById(@PathVariable("id") Long id) {

		Lookup lookup = lookupService.findById(id);
		return ResponseEntity.status(HttpStatus.OK).body(lookup);
	}

	@PostMapping("/update")
	public ResponseEntity update(@RequestBody LookupDto lookupDto) {

		Lookup lookup = new Lookup();

		try {
			Lookup existingLookup = lookupService.findById(lookupDto.getId());
			if (existingLookup.getTenantId() == 0)
				throw new TransactionException("exception.lookup.uneditable");

			// Check if duplicate with default lookup
			List<Lookup> existingLookupList = lookupService.getAllLookup(0L);
			existingLookupList.forEach(i -> {
				if (i.getLookupCode().equals(lookupDto.getLookupCode())
						&& i.getLookupType().equals(lookupDto.getLookupType())) {
					throw new TransactionException("exception.lookup.duplicate");
				}
			});

			BeanCopier copier = BeanCopier.create(LookupDto.class, Lookup.class, false);
			copier.copy(lookupDto, lookup, null);

			if (lookup.getId() == null || lookupService.findById(lookup.getId()) == null) {
				throw new TransactionException("exception.lookup.invalid");
			}

			lookupService.save(lookup);

		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.lookup.duplicate");
		}

		return ResponseEntity.status(HttpStatus.OK).body(lookup);
	}

	@GetMapping(value = "/updateStatus")
	public ResponseEntity updateStatus(@RequestParam("id") Long id, @RequestParam("status") Boolean status) {
		try {
			Lookup lookup = lookupService.findById(id);
			if (lookup.getTenantId() == 0)
				throw new TransactionException("exception.lookup.uneditable");
			lookupService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			throw new TransactionException("exception.lookup.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@GetMapping("/findByFilter")
	public ResponseEntity<List<Lookup>> findByFilter(LookupFilter filter) {

		List<Lookup> lookupList = lookupService.findByFilter(filter);
		if(StringUtils.isNoneBlank(filter.getLookupType()) && filter.getLookupType().equals(AppConstants.TPT_REQUEST_STATUS_TYPE)){
			lookupList.forEach(lookup -> {
				lookup.setLookupDescription(
						messagesUtilities.getResourceMessage(lookup.getLookupCode(), LocaleContextHolder.getLocale()));
			});
		}

		return ResponseEntity.status(HttpStatus.OK).body(lookupList);
	}

	@PostMapping(value = "/uploadFiles")
	public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
		ExcelResponseMessage excelResponseMessage = lookupService.uploadFile(file, getCurrentTenantId());
		return ResponseEntity.status(HttpStatus.OK).body(excelResponseMessage);
	}

	@PostMapping(value = "/downloadExcel")
	public void downloadExcel(HttpServletResponse response, @RequestBody LookupFilter filter) {
		if (filter == null){
			filter = new LookupFilter();
		}
		filter.setTenantId(getCurrentTenantId());
		lookupService.downloadExcel(response, filter);
	}
    
    // API for get list lookup
    @GetMapping("/list")
    public ResponseEntity<List<LookupDto>> listApi(@RequestParam String type) {
        return ResponseEntity.ok(lookupService.listLookupDtoByLookupType(type));
    }
}
