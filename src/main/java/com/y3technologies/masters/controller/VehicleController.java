package com.y3technologies.masters.controller;

import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.EmailDto;
import com.y3technologies.masters.dto.PartnersDto;
import com.y3technologies.masters.dto.VehicleDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.TptRequestReportFilter;
import com.y3technologies.masters.dto.filter.VehicleFilter;
import com.y3technologies.masters.dto.table.VehicleTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Driver;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.Vehicle;
import com.y3technologies.masters.service.DriverService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.PartnersService;
import com.y3technologies.masters.service.VehicleService;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.SortingUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.mapping.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Shao Hui
 */

@RestController
@RequestMapping(value = "/${api.version.masters}/vehicle", consumes = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.ALL_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class VehicleController extends BaseTableController<VehicleTableDto, Vehicle> {

	private final VehicleService vehicleService;
	private final DriverService driverService;

	@Autowired
	private PartnersService partnersService;

	@Autowired
	private ExcelClient excelClient;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private LookupService lookupService;

	@PostMapping(value = "/create")
	public ResponseEntity create(@RequestBody @Valid VehicleDto vehicleDto) {
		List<PartnersDto> partnersDtoList = partnersService.getByTenantIdAndPartnerTypes(getCurrentTenantId(), AppConstants.PartnerType.TRANSPORTER);

		if(vehicleDto.getTransporterId() != null){
			PartnersDto dto = partnersDtoList.stream().filter(partnersDto -> partnersDto.getId().equals(vehicleDto.getTransporterId())).findAny().orElse(null);
			if(dto == null) throw new TransactionException("exception.transporter.invalid");
		}

		Vehicle model = new Vehicle();
		try {
			model = vehicleService.save(vehicleDto);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.vehicle.duplicate");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model.getId());
	}

	@PostMapping(value = "/update")
	public ResponseEntity update(@RequestBody @Valid VehicleDto model) {
		if (model.getId() == null || vehicleService.getById(model.getId()) == null) {
			throw new TransactionException("exception.vehicle.invalid");
		}

		List<PartnersDto> partnersDtoList = partnersService.getByTenantIdAndPartnerTypes(getCurrentTenantId(), AppConstants.PartnerType.TRANSPORTER);

		if(model.getTransporterId() != null){
			PartnersDto dto = partnersDtoList.stream().filter(partnersDto -> partnersDto.getId().equals(model.getTransporterId())).findAny().orElse(null);
			if(dto == null) throw new TransactionException("exception.transporter.invalid");
		}

		try {
			vehicleService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.vehicle.duplicate");
		} catch (ObjectOptimisticLockingFailureException e) {
			throw new TransactionException("exception.vehicle.version");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@GetMapping(value = "/retrieve")
	public ResponseEntity retrieve(@RequestParam("id") Long id) {
		VehicleDto model = vehicleService.getById(id);
		if (model == null)
			throw new TransactionException("exception.vehicle.invalid");
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@PostMapping(value = "/query")
	public ResponseEntity loadByAjax(@RequestBody DataTablesInput input) {
		SortingUtils.addDefaultSortExtra(input);
		//If sort by vehicleType and requiredLicenceType will display lookup instead so we will findAll without paging and order and do it later
		int length = input.getLength();
		int start = input.getStart();
		List<Order> tempOrders = input.getOrder();
		List<Order> orders = new ArrayList<>(input.getOrder());
		Boolean specialSort = false;
		for (int i = 0; i < tempOrders.size(); i++){
			Order order = tempOrders.get(i);
			if (order.getColumn().equals(2)||order.getColumn().equals(5)){
				input.setLength(-1); // set length = -1 to get all data
				tempOrders.remove(order);
				specialSort = true;
			}
		}
		Long tenantUserId = super.getCurrentTenantUserId();
		DataTablesOutput<Vehicle> table = vehicleService.query(input, tenantUserId);
		DataTablesOutput<VehicleTableDto> tableDto = convertToTableDto(table);
		List<VehicleTableDto> vehicleTableDtoList = tableDto.getData();
		List<String> lookUpCodes = new ArrayList<>();
		vehicleTableDtoList.forEach(el -> {
			lookUpCodes.add(el.getLicenceTypeRequired());
			lookUpCodes.add(el.getVehicleType());
		});
		List<String> lookUpCodesDistinct = lookUpCodes.stream().distinct().collect(Collectors.toList());
		List<String> lookUpTypes = Arrays.asList(AppConstants.LookupType.VEHICLE_LICENSE_CLASS, AppConstants.LookupType.VEHICLE_TYPE);
		Map<String, Map<String, String>> mapLookup = lookupService.findLookupByTypeAndCode(lookUpTypes, lookUpCodesDistinct);
		vehicleTableDtoList.forEach(vehicle -> {
			vehicle.setLicenceTypeRequired(
					lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.VEHICLE_LICENSE_CLASS, vehicle.getLicenceTypeRequired(), mapLookup)
			);
			vehicle.setVehicleType(
					lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.VEHICLE_TYPE, vehicle.getVehicleType(), mapLookup)
			);
		});
		tableDto.setData(vehicleTableDtoList);
		if (specialSort) {
			vehicleTableDtoList = tableDto.getData();
			customSort(orders, vehicleTableDtoList);
			List<VehicleTableDto> sortedVehicleTableDtoList = vehicleTableDtoList.subList(start, Math.min(length + start, vehicleTableDtoList.size()));
			tableDto.setData(sortedVehicleTableDtoList);
			tableDto.setRecordsFiltered(CollectionUtils.size(vehicleTableDtoList));
		}
		return ResponseEntity.status(HttpStatus.OK).body(tableDto);
	}

	public void customSort(List<Order> orders, List<VehicleTableDto> vehicleTableDtoList) {
		for (Order order: orders){
			Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(order.getDir().equals("asc"));
			//2 is the index of the vehicleType column
			if (order.getColumn().equals(2)){
				vehicleTableDtoList.sort(SortingUtils.addDefaultSortExtraBaseTableDto(
						Comparator.comparing(VehicleTableDto::getVehicleType, sortOrder)
				));
			}
			//5 is the index of the requiredLicenceType column
			if (order.getColumn().equals(5)){
				vehicleTableDtoList.sort(SortingUtils.addDefaultSortExtraBaseTableDto(
						Comparator.comparing(VehicleTableDto::getLicenceTypeRequired, sortOrder)
				));
			}
		}
	}

	@GetMapping(value = "/updateStatus")
	public ResponseEntity updateStatus(@RequestParam("id") Long id,
			@RequestParam("status") Boolean status) {
		try {
			vehicleService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			throw new TransactionException("exception.vehicle.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PostMapping(value = "/findDefault")
	public ResponseEntity<VehicleDto> findDefaultVehicle(@RequestBody @Valid EmailDto emailDto) {

		Driver driver = driverService.findByUserEmailAndTenantId(emailDto.getEmail(), getCurrentTenantId());

		if (driver != null) {
			List<Vehicle> vehicleList = vehicleService.findByDefaultDriverIdAndTenantId(driver.getId(),
					driver.getTenantId());
			if (!vehicleList.isEmpty()) {
				Vehicle vehicle = vehicleList.get(0);
				VehicleDto dto = new VehicleDto();
				final BeanCopier copier = BeanCopier.create(Vehicle.class, VehicleDto.class, false);
				copier.copy(vehicle, dto, null);
				dto.setVehicleId(vehicle.getId());
				dto.setDefaultDriverId(vehicle.getDefaultDriver().getId());
				dto.setDefaultDriverName(vehicle.getDefaultDriver().getName());
				dto.setTransporterId(vehicle.getPartners().getId());
				dto.setTransporterName(vehicle.getPartners().getPartnerName());
				return ResponseEntity.status(HttpStatus.OK).body(dto);
			}else{
				throw new TransactionException("exception.driver.no.default.vehicle");
			}
		} else {
			throw new TransactionException("exception.driver.invalid");
		}
	}

	@GetMapping(value = "/findAllVehicleByTenantId")
	public ResponseEntity<List<VehicleDto>> findAllVehicleByTenantId() {
		List<VehicleDto> vehicleDtoList = vehicleService.findByTenantId(getCurrentTenantId(), getCurrentTenantUserId(), new VehicleFilter());
		return ResponseEntity.status(HttpStatus.OK).body(vehicleDtoList);
	}

	@Override
	public VehicleTableDto mapTableDto(Vehicle data) {
		VehicleTableDto tableDto = new VehicleTableDto();
		BeanCopier copier = BeanCopier.create(Vehicle.class, VehicleTableDto.class, false);
		copier.copy(data, tableDto, null);
		tableDto.setAssetDefaultLoc(data.getAssetDefaultLoc() == null ? null : data.getAssetDefaultLoc().getLocName());
		tableDto.setTransporterId(data.getPartners() == null? null : data.getPartners().getId());
		tableDto.setTransporterName(data.getPartners() == null ? null : data.getPartners().getPartnerName());
		return tableDto;
	}

	@GetMapping("/findByFilter")
	public ResponseEntity<List<Vehicle>> findByFilter(VehicleFilter filter) {
		if(filter.getTransporterId() != null){
			Partners partners = partnersService.findById(filter.getTransporterId());
			if(partners == null) throw new TransactionException("exception.transporter.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(vehicleService.findByFilter(filter));
	}

	@PostMapping(value = "/uploadFiles")
	public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
		ExcelResponseMessage excelResponseMessage = vehicleService.uploadExcel(file, getCurrentTenantId(), getCurrentTenantUserId());
		return ResponseEntity.status(HttpStatus.OK).body(excelResponseMessage);
	}

	@PostMapping(value = "/downloadExcel")
	public void downloadExcel(HttpServletResponse response, @RequestBody VehicleFilter filter) {
		List<VehicleDto> vehicleList = vehicleService.findByTenantId(getCurrentTenantId(), getCurrentTenantUserId(), filter);
		List<String> lookUpCodes = new ArrayList<>();
		vehicleList.forEach(el -> {
			lookUpCodes.add(el.getLicenceTypeRequired());
			lookUpCodes.add(el.getVehicleType());
		});
		List<String> lookUpCodesDistinct = lookUpCodes.stream().distinct().collect(Collectors.toList());
		List<String> lookUpTypes = Arrays.asList(AppConstants.LookupType.VEHICLE_LICENSE_CLASS, AppConstants.LookupType.VEHICLE_TYPE);
		Map<String, Map<String, String>> mapLookup = lookupService.findLookupByTypeAndCode(lookUpTypes, lookUpCodesDistinct);
		vehicleList.forEach(vehicle -> {
			vehicle.setLicenceTypeRequired(
					lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.VEHICLE_LICENSE_CLASS, vehicle.getLicenceTypeRequired(), mapLookup)
			);
			vehicle.setVehicleType(
					lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.VEHICLE_TYPE, vehicle.getVehicleType(), mapLookup)
			);
		});
		this.sortByMultiFields(filter.getSort(), vehicleList);
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.VEHICLE_SETTING_EXPORT);
		excelUtils.exportExcel(response, vehicleList, VehicleDto.class, template);
	}

	private void sortByMultiFields(List<Sort.Order> lstSort, List<VehicleDto> vehicleList) {
		Comparator<VehicleDto> comparator = Comparator.comparing(VehicleDto::getVehicleRegNumber, Comparator.naturalOrder());
		if (lstSort.isEmpty()) {
			vehicleList.sort(comparator);
			return;
		}
		Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(0).getDirection().isAscending());
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_REG_NUMBER)) {
			comparator = Comparator.comparing((VehicleDto::getVehicleRegNumber), sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_TYPE)) {
			comparator = Comparator.comparing(VehicleDto::getVehicleType, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_TRANSPORTER_NAME)) {
			comparator = Comparator.comparing(VehicleDto::getTransporterName, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_LOCATION)) {
			comparator = Comparator.comparing(VehicleDto::getLocName, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_LICENCE_TYPE)) {
			comparator = Comparator.comparing(VehicleDto::getLicenceTypeRequired, sortOrder);
		}
		if (Objects.equals(1, lstSort.size())) {
			comparator = SortingUtils.addDefaultSortExtraBaseDto(comparator);
			vehicleList.sort(comparator);
			return;
		}
		for (int i = 1; i < lstSort.size(); i++) {
			sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(i).getDirection().isAscending());
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_REG_NUMBER)) {
				comparator = comparator.thenComparing(VehicleDto::getVehicleRegNumber, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_TYPE)) {
				comparator = comparator.thenComparing(VehicleDto::getVehicleType, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_TRANSPORTER_NAME)) {
				comparator = comparator.thenComparing(VehicleDto::getTransporterName, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_LOCATION)) {
				comparator = comparator.thenComparing(VehicleDto::getLocName, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.VEHICLE_LICENCE_TYPE)) {
				comparator = comparator.thenComparing(VehicleDto::getLicenceTypeRequired, sortOrder);
			}
		}
		comparator = SortingUtils.addDefaultSortExtraBaseDto(comparator);
		vehicleList.sort(comparator);
	}

	@GetMapping("/getVehicleIdsByVehiclePlateNo")
	public ResponseEntity<List<Long>> findVehicleIdsByVehiclePlateNo(TptRequestReportFilter tptRequestReportFilter) {
		return ResponseEntity.status(HttpStatus.OK).body(
				vehicleService.findVehicleIdsByVehiclePlateNo(
						tptRequestReportFilter.getTenantId(),
						tptRequestReportFilter.getVehiclePlateNo()
				)
		);
	}

	@GetMapping(value = "/findByRegNumber")
	public VehicleDto getVehicleByVehicleNo(@RequestParam("tenantId") Long tenantId, @RequestParam("vehicleNo") String vehicleNo) {
		return vehicleService.findByVehicleNo(tenantId, vehicleNo);
	}
}
