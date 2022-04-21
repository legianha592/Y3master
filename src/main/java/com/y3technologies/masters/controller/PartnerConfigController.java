package com.y3technologies.masters.controller;

import com.y3technologies.masters.dto.PartnerConfigDto;
import com.y3technologies.masters.dto.filter.PartnerConfigFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.ConfigCode;
import com.y3technologies.masters.model.PartnerConfig;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.service.ConfigCodeService;
import com.y3technologies.masters.service.PartnerConfigService;
import com.y3technologies.masters.service.PartnersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Partner Config Controller
 * 
 * @author suxia
 * 
 */

@RestController
@RequestMapping("/${api.version.masters}/partnerConfig")
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class PartnerConfigController {
	
	@Autowired
	private PartnerConfigService partnerConfigService;

	@Autowired
	private ConfigCodeService configCodeService;

	@Autowired
	private PartnersService partnersService;
	
	@PostMapping(value = "/create")
	public ResponseEntity create(@RequestBody @Valid PartnerConfigDto dto) {
		PartnerConfig model = new PartnerConfig();
		try {
			Optional<ConfigCode> configCode = configCodeService.getById(dto.getConfigCodeId());
			Partners partners = partnersService.findById(dto.getPartnerId());
			if(partners == null) throw new TransactionException("exception.partner.invalid");
			if(configCode.isEmpty()) throw new TransactionException("exception.configCode.invalid");

			model.setConfigCode(configCode.get());
			model.setPartners(partners);
			model.setValue(dto.getValue());
			model.setActiveInd(dto.getActiveInd());

			partnerConfigService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.partnerConfig.duplicate");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model.getId());
	}
	
	@PostMapping(value = "/update")
	public ResponseEntity update(@RequestBody @Valid PartnerConfigDto dto) {
		if(dto.getId() == null || partnerConfigService.findById(dto.getId()) == null) {
			throw new TransactionException("exception.partnerConfig.invalid");
		}

		Optional<ConfigCode> configCode = configCodeService.getById(dto.getConfigCodeId());
		Partners partners = partnersService.findById(dto.getPartnerId());
		if(partners == null) throw new TransactionException("exception.partner.invalid");
		if(configCode.isEmpty()) throw new TransactionException("exception.configCode.invalid");

		PartnerConfig model = partnerConfigService.findById(dto.getId());
		model.setValue(dto.getValue());
		model.setPartners(partners);
		model.setConfigCode(configCode.get());
		model.setActiveInd(dto.getActiveInd());

		try {
			partnerConfigService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.partnerConfig.duplicate");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}
	
	@GetMapping(value = "/retrieve")
	public ResponseEntity retrieve(@RequestParam("id") Long id) {
		if (id == null || partnerConfigService.findById(id) == null) {
			throw new TransactionException("exception.partnerConfig.invalid");
		}
		PartnerConfigDto model = partnerConfigService.getById(id);
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}
	
	@GetMapping(value = "/getAllPartnerConfigs")
    public ResponseEntity getAllPartnerConfigs(@RequestParam("partnerId") Long partnerId) {
        List<PartnerConfigDto> configsList = partnerConfigService.getAllPartnerConfigs(partnerId);
        return ResponseEntity.status(HttpStatus.OK).body(configsList);
    }

	@GetMapping("/findBySearch")
	public ResponseEntity<Page<PartnerConfig>> findBySearch(PartnerConfigFilter filter) {

		return ResponseEntity.status(HttpStatus.OK).body(partnerConfigService.findBySearch(filter));
	}
	
	@GetMapping(value = "/updateStatus")
	public ResponseEntity updateStatus(@RequestParam("id") Long id, @RequestParam("status") Boolean status) {
		if (id == null || partnerConfigService.findById(id) == null) {
			throw new TransactionException("exception.partnerConfig.invalid");
		}
		try {
			partnerConfigService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			throw new TransactionException("exception.partnerConfig.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

}
