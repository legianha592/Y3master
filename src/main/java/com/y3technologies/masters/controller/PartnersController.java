package com.y3technologies.masters.controller;

import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.DriverDto;
import com.y3technologies.masters.dto.PartnersDto;
import com.y3technologies.masters.dto.VehicleDriverDto;
import com.y3technologies.masters.dto.filter.DriverFilter;
import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.dto.filter.VehicleFilter;
import com.y3technologies.masters.dto.table.PartnersTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.BaseEntity;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.Vehicle;
import com.y3technologies.masters.service.DriverService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.PartnerLocationService;
import com.y3technologies.masters.service.PartnersService;
import com.y3technologies.masters.service.VehicleService;
import com.y3technologies.masters.util.ConstantVar;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Partners Controller
 *
 * @author suxia
 *
 */

@RestController
@RequestMapping(value = "/${api.version.masters}/partners", consumes = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.ALL_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class PartnersController extends BaseTableController<PartnersTableDto, Partners> {

	private final PartnersService partnersService;

	private final VehicleService vehicleService;

	private final DriverService driverService;

	private final LookupService lookupService;

	private final PartnerLocationService partnerLocationService;

	@PostMapping(value = "/create")
	public @ResponseBody ResponseEntity create(@RequestBody @Valid PartnersDto dto) {
		Partners model = new Partners();
		try {
			model = partnersService.save(dto);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.partner.duplicate");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model.getId());
	}

	@PostMapping(value = "/update")
	public @ResponseBody ResponseEntity update(@RequestBody @Valid PartnersDto dto) {
		if (dto.getId() == null || partnersService.getById(dto.getId()) == null) {
			throw new TransactionException("exception.partner.invalid");
		}
		Partners model = new Partners();
		try {
			model = partnersService.save(dto);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.partner.duplicate");
		} catch (ObjectOptimisticLockingFailureException e) {
			throw new TransactionException("exception.partner.version");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model.getId());
	}

	@GetMapping(value = "/retrieve")
	public @ResponseBody ResponseEntity retrieve(@RequestParam("id") Long id) {
		PartnersDto model = partnersService.getById(id);
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@PostMapping(value = "/query")
	public @ResponseBody ResponseEntity loadByAjax(@RequestBody DataTablesInput input) {
		DataTablesOutput<Partners> table = partnersService.query(input);
		DataTablesOutput<PartnersTableDto> tableDto = convertToTableDto(table);
		return ResponseEntity.status(HttpStatus.OK).body(tableDto);
	}

	@PostMapping(value = "/queryByPartnerTypes/{tenantId}")
	public @ResponseBody ResponseEntity queryByPartnerTypes(@PathVariable("tenantId") Long tenantId,
			@RequestParam("code") String code) {
		LookupFilter lookupFilter = new LookupFilter();
		lookupFilter.setTenantId(0L);
		lookupFilter.setLookupType(AppConstants.PARTNER_TYPE);
		lookupFilter.setLookupCode(code);
		List<Lookup> partnerTypesList = lookupService.findByFilter(lookupFilter);

        if (partnerTypesList.isEmpty()) {
            throw new TransactionException("exception.partner.invalid");
        } else {
            List<PartnersDto> model = partnersService.getByTenantIdAndPartnerTypes(tenantId, partnerTypesList.get(0).getLookupCode());
			return ResponseEntity.status(HttpStatus.OK).body(model);
        }
	}

	@GetMapping(value = "/updateStatus")
	public @ResponseBody ResponseEntity updateStatus(@RequestParam("id") Long id,
			@RequestParam("status") Boolean status) {
		try {
			partnersService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			throw new TransactionException("exception.partner.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@Override
	public PartnersTableDto mapTableDto(Partners data) {
		PartnersTableDto tableDto = new PartnersTableDto();
		BeanCopier copier = BeanCopier.create(Partners.class, PartnersTableDto.class, false);
		copier.copy(data, tableDto, null);
		List<String> types = data.getPartnerTypes().stream().map(s-> s.getLookup().getLookupCode()).collect(Collectors.toList());
		tableDto.setPartnerTypes(String.join(ConstantVar.ORPREFIX, types));
		return tableDto;
	}

	@GetMapping("/findByFilter")
	public ResponseEntity<List<Partners>> findByFilter(PartnersFilter filter) {

		List<Partners> partnersList = partnersService.findByFilter(filter);
		List<Partners> list=
				partnersList.stream()
						.peek (partners -> partners.setPartnerTypeIds(partners.getPartnerTypes().stream()
								.map(partnerType -> partnerType.getLookup().getId())
								.collect(Collectors.toList())))
						.collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@GetMapping("/findByFilterIgnoreCase")
	public ResponseEntity<List<Partners>> findByFilterIgnoreCase(PartnersFilter filter) {

		List<Partners> partnersList = partnersService.findByFilterIgnoreCase(filter);
		List<Partners> list=
				partnersList.stream()
						.peek (partners -> partners.setPartnerTypeIds(partners.getPartnerTypes().stream()
								.map(partnerType -> partnerType.getLookup().getId())
								.collect(Collectors.toList())))
						.collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@GetMapping("/findByPartnerCodeAndTenantId/{partnerCode}/{tenantId}")
	public ResponseEntity<Partners> findByPartnerCodeAndTenantId(@PathVariable String partnerCode,
																 @PathVariable Long tenantId) {

		return ResponseEntity.status(HttpStatus.OK)
				.body(partnersService.findByPartnerCodeAndTenantId(partnerCode, tenantId));
	}

	@PostMapping("/findVehicleDriverByTransporterIds")
	public ResponseEntity<Map<Long, VehicleDriverDto>> testFindVehicleDriverByTransporterIds(@RequestBody PartnersFilter filter) {

		List<Partners> partnersList = partnersService.findByFilter(filter);

		if (CollectionUtils.isEmpty(partnersList)) {
			return ResponseEntity.status(HttpStatus.OK).body(Collections.emptyMap());
		}

		List<Long> partnerIds = partnersList.stream().map(BaseEntity::getId).collect(Collectors.toList());

		VehicleFilter vehicleFilter = new VehicleFilter();
		vehicleFilter.setTransporterIdList(partnerIds);
		vehicleFilter.setTenantId(filter.getTenantId());
		vehicleFilter.setActiveInd(Boolean.TRUE);

		DriverFilter driverFilter = new DriverFilter();
		driverFilter.setTransporterIdList(partnerIds);
		driverFilter.setTenantId(filter.getTenantId());
		driverFilter.setActiveInd(Boolean.TRUE);

		List<Vehicle> vehicleList = vehicleService.findByFilter(vehicleFilter);
		List<DriverDto> driverList = driverService.findByFilter(driverFilter, getCurrentTenantUserId(), false);

		Map<Long, List<Vehicle>> mapVehicle = new HashMap<>();
		vehicleList.forEach(el -> {
			Long transporterId = el.getPartners().getId();
			List<Vehicle> lstVehicle = mapVehicle.get(transporterId);
			if (CollectionUtils.isEmpty(lstVehicle)) {
				lstVehicle = new ArrayList<>();
			}
			lstVehicle.add(el);
			mapVehicle.put(transporterId, lstVehicle);
		});

		Map<Long, List<DriverDto>> mapDriver = new HashMap<>();
		driverList.forEach(el -> {
			Long transporterId = el.getTransporterId();
			List<DriverDto> lstDriver = mapDriver.get(transporterId);
			if (CollectionUtils.isEmpty(lstDriver)) {
				lstDriver = new ArrayList<>();
			}
			lstDriver.add(el);
			mapDriver.put(transporterId, lstDriver);
		});
		Map<Long, VehicleDriverDto> vehicleDriverDtoMap = partnersList
				.stream()
				.collect(Collectors.toMap(BaseEntity::getId,
						partners -> {
							VehicleDriverDto vehicleDriverDto = new VehicleDriverDto();
							vehicleDriverDto.setVehicleList(mapVehicle.get(partners.getId()));
							vehicleDriverDto.setDriverList(mapDriver.get(partners.getId()));
							return vehicleDriverDto;
						}));

		return ResponseEntity.status(HttpStatus.OK).body(vehicleDriverDtoMap);
	}

	@PostMapping("/createOrUpdateMultiplePartnerLocation")
	public ResponseEntity<List<PartnerLocation>> createOrUpdateMultiplePartnerLocation(@RequestBody List<PartnerLocation> partnerLocationList) {
		return ResponseEntity.ok().body(partnerLocationService.createOrUpdateMultiple(partnerLocationList));
	}

	@GetMapping("/findByPartnerNameAndTenantId/{partnerName}/{tenantId}")
	public ResponseEntity<Partners> findByPartnerNameAndTenantId(@PathVariable String partnerName,
																 @PathVariable Long tenantId) {

		return ResponseEntity.status(HttpStatus.OK)
				.body(partnersService.findByPartnerNameAndTenantId(partnerName, tenantId));
	}
}
