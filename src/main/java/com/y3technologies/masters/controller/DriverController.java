package com.y3technologies.masters.controller;

import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.DriverDto;
import com.y3technologies.masters.dto.TenantDetail;
import com.y3technologies.masters.dto.PartnersDto;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.DriverFilter;
import com.y3technologies.masters.dto.filter.TptRequestReportFilter;
import com.y3technologies.masters.dto.table.DriverTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Driver;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.repository.impl.DriverRepositoryImpl;
import com.y3technologies.masters.service.DriverService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.PartnersService;
import com.y3technologies.masters.service.impl.LookupServiceImpl;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.SortingUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.Column;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shao Hui
 */

@RestController
@RequestMapping(value = "/${api.version.masters}/driver", consumes = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.ALL_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class DriverController extends BaseTableController<DriverTableDto, Driver> {

	private final DriverService driverService;
	private final AddrContactClient addrContactClient;
	private final DriverRepositoryImpl driverRepositoryCustom;

	@Autowired
	private PartnersService partnersService;

	@Autowired
	private ExcelClient excelClient;

	private Map<Long, AddrContact> addrContactMap = new HashMap<>();

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private LookupService lookupService;

	private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

	@PostMapping("/create")
	public @ResponseBody ResponseEntity<Long> create(@RequestBody @Valid DriverDto driverDto) {
		List<PartnersDto> partnersDtoList = partnersService.getByTenantIdAndPartnerTypes(getCurrentTenantId(),
				AppConstants.PartnerType.TRANSPORTER);

		Driver driver = new Driver();
		try {
			BeanCopier copier = BeanCopier.create(DriverDto.class, Driver.class, false);
			copier.copy(driverDto, driver, null);

			if (driverDto.getTransporterId() != null) {
				PartnersDto dto = partnersDtoList.stream()
						.filter(partnersDto -> partnersDto.getId().equals(driverDto.getTransporterId())).findAny()
						.orElse(null);
				if (dto == null)
					throw new TransactionException("exception.transporter.invalid");

				Partners partners = new Partners();
				BeanCopier beanCopier = BeanCopier.create(PartnersDto.class, Partners.class, false);
				beanCopier.copy(dto, partners, null);

				driver.setPartners(partners);
			}

			// save address contact or reuse addressContactId if exist
			Optional<AddrContact> savedAddrContact = Optional
					.ofNullable(addrContactClient.createOrUpdateAddressContact(mapAddrContactDto(driverDto)));
			savedAddrContact.ifPresent(value -> driver.setAddressContactId(value.getId()));

			if (driver.getTenantId() == null) {
				driver.setTenantId(getCurrentTenantId());
			}
			driverService.saveAndCreateTenantAccount(driver, savedAddrContact.get(), driverDto.getUserId());

		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.driver.duplicate");
		}
		return ResponseEntity.status(HttpStatus.OK).body(driver.getId());
	}

	@PostMapping("/update")
	public @ResponseBody ResponseEntity<Driver> update(@RequestBody @Valid DriverDto driverDto) {

		if (driverDto.getId() == null || driverService.getById(driverDto.getId()) == null) {
			throw new TransactionException("exception.driver.invalid");
		}

		List<PartnersDto> partnersDtoList = partnersService.getByTenantIdAndPartnerTypes(getCurrentTenantId(),
				AppConstants.PartnerType.TRANSPORTER);

		Driver driver = new Driver();

		try {
			BeanCopier copier = BeanCopier.create(DriverDto.class, Driver.class, false);
			copier.copy(driverDto, driver, null);

			if (driverDto.getTransporterId() != null) {
				PartnersDto dto = partnersDtoList.stream()
						.filter(partnersDto -> partnersDto.getId().equals(driverDto.getTransporterId())).findAny()
						.orElse(null);
				if (dto == null)
					throw new TransactionException("exception.transporter.invalid");

				Partners partners = new Partners();
				BeanCopier beanCopier = BeanCopier.create(PartnersDto.class, Partners.class, false);
				beanCopier.copy(dto, partners, null);

				driver.setPartners(partners);
			}

			// save address contact or reuse addressContactId if exist
			Optional<AddrContact> savedAddrContact = Optional
					.ofNullable(addrContactClient.createOrUpdateAddressContact(mapAddrContactDto(driverDto)));

			savedAddrContact.ifPresent(value -> driver.setAddressContactId(value.getId()));

			driverService.saveAndCreateTenantAccount(driver, savedAddrContact.get(), driverDto.getUserId());

		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.driver.duplicate");
		} catch (ObjectOptimisticLockingFailureException e) {
			throw new TransactionException("exception.driver.version");
		}
		return ResponseEntity.status(HttpStatus.OK).body(driver);
	}

	@GetMapping(value = "/retrieve")
	public @ResponseBody ResponseEntity retrieve(@RequestParam("id") Long id) {
		Driver driver = driverService.getById(id);
		DriverDto driverDto;
		if (driver != null) {
			driverDto = new DriverDto();
			final BeanCopier copier = BeanCopier.create(Driver.class, DriverDto.class, false);
			copier.copy(driver, driverDto, null);
			Long addressContactId = driver.getAddressContactId();
			AddrContact addrContact = null;
			if (addressContactId != null) {
				try {
					Optional<AddrContact> existingAddrContact = Optional
							.ofNullable(addrContactClient.getAddressContact(addressContactId));

					if (existingAddrContact.isPresent()) {
						addrContact = existingAddrContact.get();
					}
				} catch (Exception exception) {
					throw exception;
				}
			}
			if (driver.getPartners() != null) {
				driverDto.setTransporterId(driver.getPartners().getId());
			} else {
				driverDto.setTransporterId(null);
			}
			driverDto.setAddrContact(addrContact);

		} else {
			throw new TransactionException("exception.driver.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(driverDto);
	}

	@PostMapping(value = "/query")
	public @ResponseBody ResponseEntity loadByAjax(@RequestBody DataTablesInput input) {
		SortingUtils.addDefaultSortExtra(input);
		//If sort by mobileNumber which from another database or licenceType which display as lookup instead then findAll without paging and order and do it later after mapped mobileNumber field
		int length = input.getLength();
		int start = input.getStart();
		List<Order> tempOrders = input.getOrder();
		List<Order> orders = new ArrayList<>(input.getOrder());
		Boolean specialSort = false;

		for (int i = 0; i < tempOrders.size(); i++){
			Order order = tempOrders.get(i);
			if (order.getColumn().equals(3)||order.getColumn().equals(4)){//3 is the index of mobileNumber1 column, 4 is the index of licenceType column
				input.setLength(-1); //set length = -1 to get all data
				tempOrders.remove(order);
				specialSort = true;
			}
		}
		DataTablesOutput<DriverTableDto> tableDtos = getDriverTableDto(input, getCurrentTenantUserId());
		List<DriverTableDto> driverTableDtoList = tableDtos.getData();
		List<String> licenceTypes = driverTableDtoList.stream().map(DriverTableDto::getLicenceType).distinct().collect(Collectors.toList());
		List<String> lookUpTypes = Collections.singletonList(AppConstants.LookupType.DRIVER_LICENSE_TYPE);
		Map<String, Map<String, String>> mapLookup = lookupService.findLookupByTypeAndCode(lookUpTypes, licenceTypes);
		driverTableDtoList.forEach(el -> el.setLicenceType(lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.DRIVER_LICENSE_TYPE, el.getLicenceType(), mapLookup)));
		logger.info(String.format("license type %s",driverTableDtoList.get(0).getLicenceType()));
		tableDtos.setData(driverTableDtoList);
		if (specialSort) {
			driverTableDtoList = tableDtos.getData();
			customSort(orders, driverTableDtoList);
			List<DriverTableDto> sortedDriverTableDtoList = driverTableDtoList.subList(start, Math.min(length + start, driverTableDtoList.size()));
			tableDtos.setData(sortedDriverTableDtoList);
			tableDtos.setRecordsFiltered(CollectionUtils.size(driverTableDtoList));
		}
		return ResponseEntity.status(HttpStatus.OK).body(tableDtos);
	}

	private DataTablesOutput<DriverTableDto> getDriverTableDto(DataTablesInput input, Long tenantUserId){
		Column mobileNumber = input.getColumn("mobileNumber1");
		Boolean mobileSearchAble = false;
		if (mobileNumber != null) {
			mobileSearchAble = mobileNumber.getSearchable();
			input.getColumn("mobileNumber1").setSearchable(false);
		}

		DataTablesOutput<Driver> table = driverService.query(input, tenantUserId);
		if (!CollectionUtils.isEmpty(table.getData())) {
			List<Long> addContactIds = table.getData().stream().map(Driver::getAddressContactId)
					.collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(addContactIds)) {
				try {
					Optional<List<AddrContact>> existingAddrContacts = Optional
							.ofNullable(addrContactClient.getAddressContactList(addContactIds));
					if (existingAddrContacts.isPresent()) {
						addrContactMap = existingAddrContacts.get().stream()
								.collect(Collectors.toMap(AddrContact::getId, a -> a, (k1, k2) -> k1));
					}
				} catch (Exception exception) {
					throw exception;
				}
			}
		}

		DataTablesOutput<DriverTableDto> tableDtos = convertToTableDto(table);
		if (mobileNumber != null && StringUtils.isNotEmpty(mobileNumber.getSearch().getValue()) && mobileSearchAble) {
			String search = mobileNumber.getSearch().getValue();
			if (!CollectionUtils.isEmpty(tableDtos.getData())) {
				List<DriverTableDto> filterByMobile = tableDtos.getData().stream().filter(dto -> {
					if (dto.getMobileNumber1().contains(search)) {
						return true;
					}
					return false;
				}).collect(Collectors.toList());
				tableDtos.setData(filterByMobile);
				tableDtos.setRecordsFiltered(CollectionUtils.size(filterByMobile));
			}
		}
		return tableDtos;
	}

	public void customSort(List<Order> orders, List<DriverTableDto> driverTableDtos) {
		for (Order order: orders){
			Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(order.getDir().equals("asc"));
			//3 is the index of mobileNumber1 column
			if (order.getColumn().equals(3)){
				driverTableDtos.sort(SortingUtils.addDefaultSortExtraBaseTableDto(
						Comparator.comparing(DriverTableDto::getMobileNumber1, sortOrder)
				));
			}
			//4 is the index of licenseType column but display the lookup result
			if (order.getColumn().equals(4)){
				driverTableDtos.sort(SortingUtils.addDefaultSortExtraBaseTableDto(
						Comparator.comparing(DriverTableDto::getLicenceType, sortOrder)
				));
			}
		}
	}

	@GetMapping(value = "/updateStatus")
	public @ResponseBody ResponseEntity updateStatus(@RequestParam("id") Long id,
			@RequestParam("status") Boolean status) {
		try {
			driverService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			throw new TransactionException("exception.driver.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	private AddrContactDTO mapAddrContactDto(DriverDto driverDto) {
		AddrContactDTO addrContactDTO = new AddrContactDTO();

		BeanCopier copier = BeanCopier.create(DriverDto.class, AddrContactDTO.class, false);
		copier.copy(driverDto, addrContactDTO, null);
		return addrContactDTO;
	}

	@Override
	public DriverTableDto mapTableDto(Driver data) {
		DriverTableDto tableDto = new DriverTableDto();
		Long addressContactId = data.getAddressContactId();
		if (addressContactId != null) {
			AddrContact addrContact = addrContactMap.get(addressContactId);
			if (addrContact!= null){
				tableDto.setMobileNumber1(addrContact.getMobileNumber1()!=null ? addrContact.getMobileNumber1() : "");
				tableDto.setMobileNumber1CountryShortName(addrContact.getMobileNumber1CountryShortName()!=null ? addrContact.getMobileNumber1CountryShortName() : "");
			}
		}
		tableDto.setName(data.getName());
		BeanCopier copier = BeanCopier.create(Driver.class, DriverTableDto.class, false);
		copier.copy(data, tableDto, null);
		tableDto.setTransporterId(data.getPartners() == null ? null : data.getPartners().getId());
		tableDto.setTransporterName(data.getPartners() == null ? null : data.getPartners().getPartnerName());
		return tableDto;
	}

	@GetMapping(value = "/findAllDriverByTenantId")
	public @ResponseBody ResponseEntity<List<DriverDto>> findAllDriverByTenantId(@RequestBody(required = false) DriverFilter filter) {
		if (Objects.isNull(filter)) {
			filter = new DriverFilter();
		}
		List<DriverDto> driverDtoList = driverService.findByTenantId(getCurrentTenantId(), getCurrentTenantUserId(), filter);
		return ResponseEntity.status(HttpStatus.OK).body(driverDtoList);
	}

	@GetMapping("/findByFilter")
	public ResponseEntity<List<DriverDto>> findByFilter(DriverFilter filter) {
		if (filter.getTransporterId() != null) {
			Partners partners = partnersService.findById(filter.getTransporterId());
			if (partners == null)
				throw new TransactionException("exception.transporter.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(driverService.findByFilter(filter, getCurrentTenantUserId(), true));
	}

	@PostMapping(value = "/uploadFiles")
	public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
		ExcelResponseMessage excelResponseMessage = driverService.uploadExcel(file, getCurrentTenantId(), getCurrentTenantUserId());
		return ResponseEntity.status(HttpStatus.OK).body(excelResponseMessage);
	}

	@PostMapping(value = "/downloadExcel")
	public void downloadExcel(HttpServletResponse response, @RequestBody DriverFilter filter) {
		List<DriverDto> driverDtoList = driverService.findByTenantId(getCurrentTenantId(), getCurrentTenantUserId(), filter);
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_EXPORT);
		List<String> licenceTypes = driverDtoList.stream().map(DriverDto::getLicenceType).distinct().collect(Collectors.toList());
		List<String> lookUpTypes = Collections.singletonList(AppConstants.LookupType.DRIVER_LICENSE_TYPE);
		Map<String, Map<String, String>> mapLookup = lookupService.findLookupByTypeAndCode(lookUpTypes, licenceTypes);
		driverDtoList.forEach(driverDto -> driverDto.setLicenceType(
				lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.DRIVER_LICENSE_TYPE, driverDto.getLicenceType(), mapLookup)
		));
		this.sortByMultiFields(filter.getSort(), driverDtoList);
		excelUtils.exportExcel(response, driverDtoList, DriverDto.class, template);
	}

	private void sortByMultiFields(List<Sort.Order> lstSort, List<DriverDto> driverDtoList) {
		Comparator<DriverDto> comparator = Comparator.comparing(DriverDto::getName, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
		if (lstSort.isEmpty()) {
			driverDtoList.sort(comparator);
			return;
		}
		Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(0).getDirection().isAscending());
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.DRIVER_NAME)) {
			comparator = Comparator.comparing(DriverDto::getName, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.DRIVER_TRANSPORTER_NAME)) {
			comparator = Comparator.comparing(DriverDto::getTransporterName, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.DRIVER_MOBILE_NUMBER)) {
			comparator = Comparator.comparing(DriverDto::getMobileNumber1, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.DRIVER_LICENCE_TYPE)) {
			comparator = Comparator.comparing(DriverDto::getLicenceType, sortOrder);
		}
		if (Objects.equals(1, lstSort.size())) {
			comparator = SortingUtils.addDefaultSortExtraBaseDto(comparator);
			driverDtoList.sort(comparator);
			return;
		}
		for (int i = 1; i < lstSort.size(); i++) {
			sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(i).getDirection().isAscending());
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.DRIVER_NAME)) {
				comparator = comparator.thenComparing(DriverDto::getName, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.DRIVER_TRANSPORTER_NAME)) {
				comparator = comparator.thenComparing(DriverDto::getTransporterName, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.DRIVER_MOBILE_NUMBER)) {
				comparator = comparator.thenComparing(DriverDto::getMobileNumber1, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.DRIVER_LICENCE_TYPE)) {
				comparator = comparator.thenComparing(DriverDto::getLicenceType, sortOrder);
			}
		}
		comparator = SortingUtils.addDefaultSortExtraBaseDto(comparator);
		driverDtoList.sort(comparator);
	}

	@GetMapping("/getDriverIdsByDriverName")
	public ResponseEntity<List<Long>> findDriverIdsByDriverName(TptRequestReportFilter tptRequestReportFilter) {
		return ResponseEntity.status(HttpStatus.OK).body(
				driverService.findDriverIdsByDriverName(
						tptRequestReportFilter.getTenantId(),
						tptRequestReportFilter.getDriverName()
				)
		);
	}

	@GetMapping("/findByEmail")
	public DriverDto getDriverByEmail(@RequestParam("tenantId") Long tenantId, @RequestParam("email") String email) {
		Driver driver = driverService.findByUserEmailAndTenantId(email, tenantId);

		if (ObjectUtils.isEmpty(driver)) {
			return null;
		}

		DriverDto driverDto = new DriverDto();
		final BeanCopier copier = BeanCopier.create(Driver.class, DriverDto.class, false);
		copier.copy(driver, driverDto, null);
		if (ObjectUtils.isNotEmpty(driver.getPartners())) {
			driverDto.setTransporterId(driver.getPartners().getId());
		}
		return driverDto;
	}

	@PostMapping("/getNoDriverByTenantId")
	public ResponseEntity<Long> getNoDriverByTenantId(@RequestBody Long tenantId) {
		Long noOfDriver = driverService.getNoDriverByTenantId(tenantId);
		return ResponseEntity.ok().body(noOfDriver);
	}

	@PostMapping("/getTenantDetails")
	public ResponseEntity<List<TenantDetail>> getTenantDetails(@RequestParam("sortParam") String sortParam) {
		List<TenantDetail> dtoList = driverRepositoryCustom.getListNoForTenantDTO(sortParam);
		return ResponseEntity.ok().body(dtoList);
	}

	@GetMapping("/getTransporterIdByDriver")
	public ResponseEntity<Long> getTransporterIdByDriver(@RequestParam("driverId") Long driverId) {
		Long transporterId = driverService.getTransporterIdByDriver(driverId);
		return ResponseEntity.status(HttpStatus.OK).body(transporterId);
	}
}
