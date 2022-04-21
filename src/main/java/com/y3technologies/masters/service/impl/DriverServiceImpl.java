package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.DriverDto;
import com.y3technologies.masters.dto.TransporterDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.TenantUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.AddrContactFilter;
import com.y3technologies.masters.dto.filter.DriverFilter;
import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Driver;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.QDriver;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.repository.DriverRepository;
import com.y3technologies.masters.repository.impl.DriverRepositoryImpl;
import com.y3technologies.masters.service.DriverService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.PartnersService;
import com.y3technologies.masters.service.ProfileScopeService;
import com.y3technologies.masters.service.TransporterService;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

	public final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final DriverRepository driverRepository;
	private final OperationLogService operationLogService;
	private final AasClient aasClient;
	private final AddrContactClient addrContactClient;
	private final DriverRepositoryImpl driverRepositoryCustom;
	private final PartnersService partnersService;
	private final MessagesUtilities messagesUtilities;
	private final MastersApplicationPropertiesConfig propertiesConfig;

	private final LookupService lookupService;
	private final ExcelUtils excelUtils;
	private final ProfileScopeService profileScopeService;
	private final TransporterService transporterService;
	private final ThreadPoolTaskExecutor executor;

	@Override
	@Transactional
	public void save(Driver model) {
		Long id = model.getId();
		Boolean updateFlag = Boolean.TRUE;
		if (model.getId() != null) {
			Driver oldModel = driverRepository.findById(model.getId()).get();
			updateFlag = oldModel.getHashcode() != model.hashCode();
		}
		model.setHashcode(model.hashCode());
		driverRepository.save(model);
		if (updateFlag) {
			operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
		}
	}

	@Override
	public Driver getById(Long id) {
		return driverRepository.findById(id).orElse(null);
	}

	@Override
	public DataTablesOutput<Driver> query(DataTablesInput input, Long tenantUserId) {
		Specification<Driver> sQuery = (root, query, criteriaBuilder) -> criteriaBuilder.and();
		sQuery = profileScopeService.getQueryFromProfileScope(tenantUserId, sQuery, AppConstants.ProfileScopeSettings.DRIVER);
		if (input.getColumn("tenantId") != null
				&& StringUtils.isNotEmpty(input.getColumn("tenantId").getSearch().getValue())
				&& input.getColumn("tenantId").getSearchable()
		) {
			Specification<Driver> specification = new Specification<Driver>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Predicate toPredicate(Root<Driver> root, CriteriaQuery<?> query,
											 CriteriaBuilder criteriaBuilder) {
					List<Predicate> listQuery = new ArrayList<>();
					Predicate queryTenantId = criteriaBuilder.equal(root.get("tenantId"), input.getColumn("tenantId").getSearch().getValue());
					listQuery.add(queryTenantId);
					if (!Objects.isNull(input.getColumn("transporterId")) && StringUtils.isNotEmpty(input.getColumn("transporterId").getSearch().getValue())
							&& input.getColumn("transporterId").getSearchable()) {
						Path<Partners> partners = root.get("partners");
						Predicate queryTransporterId = criteriaBuilder.equal(partners.get("id"),input.getColumn("transporterId").getSearch().getValue());
						listQuery.add(queryTransporterId);
					}
					return criteriaBuilder.and(listQuery.toArray(new Predicate[0]));
				}
			};
			input.getColumn("tenantId").setSearchable(false);
			sQuery = sQuery.and(specification);
		}
		return driverRepository.findAll(input, sQuery);
	}

	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		Driver model = driverRepository.findById(id).get();
		if (model.getActiveInd() != status) {
			driverRepository.updateStatus(id, status);
			operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	@Override
	@Transactional
	public void saveAndCreateTenantAccount(Driver driver, AddrContact addrContact, Long userId) {

		this.save(driver);

		if (driver == null)
			throw new NullPointerException();

		/**
		 * Driver's Register Email not found in tenant_user Auto create tenant user
		 * account;
		 */
		if (!aasClient.proceedCreateTenantUserAccount(driver.getEmail(), driver.getTenantId()))
			return;

		TenantUserInfoDTO tenantInfo = new TenantUserInfoDTO();
		tenantInfo.setEmail(driver.getEmail());
		tenantInfo.setFirstName(driver.getName());
		tenantInfo.setLastName(driver.getName());
		tenantInfo.setEmail(driver.getEmail());
		tenantInfo.setUserLevel("TENANT USER");
		tenantInfo.setPhone(addrContact.getMobileNumber1());
		tenantInfo.setPhoneCountryShortName(addrContact.getMobileNumber1CountryShortName());
		tenantInfo.setTenantId(driver.getTenantId());
		tenantInfo.setAasUserId(userId);
		tenantInfo.addTenantUserRole(AppConstants.Role.DRIVER);
		aasClient.createTenantUser(tenantInfo);

	}

	@Transactional
	@Override
	public List<DriverDto> findByTenantId(Long tenantId, Long tenantUserId, DriverFilter filter) {
		QDriver qDriver = QDriver.driver;
		BooleanExpression query = qDriver.isNotNull();

		query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.DRIVER, null);
		query = query.and(qDriver.tenantId.eq(tenantId));

		List<AddrContact> addrContacts = new ArrayList<>();

		if (!StringUtils.isBlank(filter.getName())) {
			query = query.and(qDriver.name.like("%" + filter.getName() + "%"));
		}

		if (!Objects.isNull(filter.getPartnerId())) {
			query = query.and(qDriver.partners.id.eq(filter.getPartnerId()));
		}

		if (!StringUtils.isBlank(filter.getMobileNumber1())) {
			AddrContactFilter addrContactFilter = new AddrContactFilter();
			addrContactFilter.setContactNo(filter.getMobileNumber1());
			addrContacts = addrContactClient.findByFilter(addrContactFilter);
			if (addrContacts.isEmpty()) {
				return Collections.emptyList();
			}
			List<Long> addrContactIds = addrContacts.stream().map(AddrContact::getId).collect(Collectors.toList());
			query = query.and(qDriver.addressContactId.in(addrContactIds));
		}

		if (!StringUtils.isBlank(filter.getLicenceType())) {
			query = query.and(qDriver.licenceType.like("%" + filter.getLicenceType() + "%"));
		}

		List<Driver> driverList = driverRepository.findAll(query);

		if (driverList.isEmpty()) {
			return Collections.emptyList();
		}

		if (addrContacts.isEmpty()) {
			List<Long> contactIds = driverList.stream().map(Driver::getAddressContactId).collect(Collectors.toList());
			if (!contactIds.isEmpty()) {
				addrContacts = addrContactClient.getAddressContactList(contactIds);
			}
		}

		Map<Long, AddrContact> mapAddrContact = addrContacts.stream().collect(Collectors.toMap(AddrContact::getId, Function.identity()));

		return driverList.stream().map(driver -> {
			DriverDto driverDto = new DriverDto();
			final BeanCopier copier = BeanCopier.create(Driver.class, DriverDto.class, false);
			copier.copy(driver, driverDto, null);
			if (!Objects.isNull(driver.getAddressContactId())) {
				AddrContact addrContact = mapAddrContact.get(driver.getAddressContactId());
				if (!Objects.isNull(addrContact)) {
					driverDto.setAddrContact(addrContact);
					driverDto.setMobileNumber1(addrContact.getMobileNumber1());
					driverDto.setMobileNumber1CountryShortName(addrContact.getMobileNumber1CountryShortName());
				}
				driverDto.setAddrContactId(driver.getAddressContactId());
			}
			if (!Objects.isNull(driver.getPartners())) {
				driverDto.setTransporterId(driver.getPartners().getId());
				driverDto.setTransporterName(driver.getPartners().getPartnerName());
			}
			driverDto.setHashcode(driver.getHashcode());
			return driverDto;
		}).collect(Collectors.toList());
	}

	@Override
	public Driver findByUserEmailAndTenantId(String email, Long tenantId) {
		return driverRepository.findByUserEmailAndTenantId(email, tenantId);
	}

	@Override
	public List<DriverDto> findByFilter(DriverFilter filter, Long tenantUserId, boolean isCallFromDriverController) {

		QDriver qDriver = QDriver.driver;
		BooleanExpression query = qDriver.isNotNull();

		if (isCallFromDriverController) {
			query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.DRIVER, null);
		}

		Map<Long, AddrContact> addrContactDtoMap = new HashedMap<>();

		if (filter.getTenantId() != null) {
			query = query.and(qDriver.tenantId.eq(filter.getTenantId()));
		}

		if (filter.getEmail() != null) {
			query = query.and(qDriver.email.eq(filter.getEmail()));
		}

		if (StringUtils.isNoneBlank(filter.getName())) {
			String keyword = filter.getName().trim();
			query = query.and(qDriver.name.like("%" + keyword + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getContactNo())) {
			List<AddrContact> addrContact = addrContactClient
					.findByFilter(new AddrContactFilter(filter.getContactNo().trim()));

			if (CollectionUtils.isNotEmpty(addrContact)) {
				query = query.and(qDriver.addressContactId
						.in(addrContact.stream().map(c -> c.getId()).collect(Collectors.toList())));
			}

		}

		if (CollectionUtils.isNotEmpty(filter.getDriverIdList())) {
			query = query.and(qDriver.id.in(filter.getDriverIdList()));
		}

		if (CollectionUtils.isNotEmpty(filter.getDriverEmailList())) {
			query = query.and(qDriver.email.in(filter.getDriverEmailList()));
		}

		if (CollectionUtils.isNotEmpty(filter.getTransporterIdList())) {
			query = query.and(qDriver.partners.id.in(filter.getTransporterIdList()));
		}

		if (filter.getTransporterId() != null) {
			query = query.and(qDriver.partners.id.in(filter.getTransporterId()));
		}

		if(filter.getAssignedTransporter() != null){
			if(filter.getAssignedTransporter() == Boolean.FALSE){
				query = query.and(qDriver.partners.id.isNull());
			}else{
				query = query.and(qDriver.partners.id.isNotNull());
			}
		}

		if(filter.getActiveInd() != null){
			query = query.and(qDriver.activeInd.eq(filter.getActiveInd()));
		}

		List<Driver> driverList = driverRepository.findAll(query);

		if (CollectionUtils.isEmpty(driverList))
			return Collections.emptyList();

		List<Long> addressContactIds = driverList.stream().map(a -> a.getAddressContactId())
				.collect(Collectors.toList());

		List<AddrContact> addrContactList = addrContactClient.postFindByFilter(new AddrContactFilter(addressContactIds));

		addrContactDtoMap = addrContactList.stream().collect(Collectors.toMap(a -> a.getId(), a -> a));

		List<DriverDto> driverDtoList = new ArrayList<>();
		for (Driver driver : driverList) {
			DriverDto driverDto = new DriverDto();
			driverDto.setId(driver.getId());
			driverDto.setActiveInd(driver.getActiveInd());
			driverDto.setName(driver.getName());
			driverDto.setName(driver.getName());
			driverDto.setEmail(driver.getEmail());
			if(driver.getAddressContactId() != null && addrContactDtoMap.get(driver.getAddressContactId()) != null) {
				driverDto.setMobileNumber1(addrContactDtoMap.get(driver.getAddressContactId()).getMobileNumber1());
			}
			driverDto.setTransporterId(driver.getPartners() != null ? driver.getPartners().getId() : null);

			driverDtoList.add(driverDto);
		}

		return driverDtoList;
	}

	public int countByCondition (DriverDto driverDto){
		return driverRepositoryCustom.countByCondition(driverDto);
	}

	@Override
	public ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId, Long currentTenantUserId) {
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_UPLOAD);
		byte[] byteArr = excelUtils.getByteArrOfUploadedFile(file);
		ExcelResponseMessage excelResponseMessage = new ExcelResponseMessage();
		String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";

		if (!excelUtils.validFileType(fileName, excelResponseMessage)){
			return excelResponseMessage;
		}

		Workbook workbook = excelUtils.initWorkbook(byteArr, fileName);

		if (!excelUtils.validSheetNumber(workbook, uploadTemplateHdrIdDto.getSheetSeqNo(), excelResponseMessage)){
			return excelResponseMessage;
		}

		Sheet sheet = workbook.getSheetAt(uploadTemplateHdrIdDto.getSheetSeqNo());

		boolean validHeader = excelUtils.validateExcelHeader(DriverDto.class, workbook, sheet, fileName
				, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors, excelResponseMessage);

		if (!validHeader){
			return excelResponseMessage;
		}

		if (excelUtils.checkEmptyFile(sheet, uploadTemplateHdrIdDto.getStartRow(), excelResponseMessage)){
			return excelResponseMessage;
		}

		excelUtils.calculateTimeForPopup(sheet, excelResponseMessage);

		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO)
		RequestContextHolder.currentRequestAttributes().getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (org.springframework.util.ObjectUtils.isEmpty(sessionUserInfoDTO)){
			throw new TransactionException("exception.tenant.id.invalid");
		}
		UpdateUserProfileDTO userInfo = aasClient.retrieveUserProfile(sessionUserInfoDTO.getAasUserId());
		String authToken = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();

		executor.execute(() -> {
			try {
				validateAndSaveExcelData(currentTenantId, currentTenantUserId, workbook, sheet, fileName, mapExcelError, excelEmailErrors, uploadTemplateHdrIdDto, timeStartReadingFile, userInfo, authToken);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return excelResponseMessage;
	}

	@Override
	public List<Long> findDriverIdsByDriverName(Long tenantId, String driverName) {
		return driverRepository.findDriverIdsByDriverName(tenantId, driverName);
	}

	@Override
	public List<Driver> findByName(String name){
		return driverRepository.findByName(name);
	}


	public void checkDuplicatedItemByName(List<DriverDto> driverDtoList,
										  Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (DriverDto dto : driverDtoList) {
			if (StringUtils.isEmpty(dto.getName())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getName().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getName().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < driverDtoList.size(); itemIndex++) {
			DriverDto dto = driverDtoList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getName())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getName().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("driver.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getName().trim().toLowerCase()), dto.getName()}));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	public void checkDuplicatedItemByEmail(List<DriverDto> driverDtoList,
										   Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (DriverDto dto : driverDtoList) {
			if (StringUtils.isEmpty(dto.getEmail())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getEmail().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getEmail().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < driverDtoList.size(); itemIndex++) {
			DriverDto dto = driverDtoList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getEmail())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getEmail().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("driver.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getEmail().trim().toLowerCase()), dto.getEmail()}));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	private void validCountryMobileCode(List<DriverDto> driverDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		for (int itemIndex = 0; itemIndex < driverDtoList.size(); itemIndex++) {
			DriverDto driverDto = driverDtoList.get(itemIndex);
			if (StringUtils.isEmpty(driverDto.getMobileNumber1CountryShortName())) {
				continue;
			}
			List<CountryDTO> countryDTOList = addrContactClient.findByCountryIsdCode(driverDto.getMobileNumber1CountryShortName());
			if (countryDTOList.size() == 1) {
				driverDto.setMobileNumber1CountryShortName(countryDTOList.get(0).getCountryShortName());
			} else if (countryDTOList.size() == 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);
				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}
				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Country Mobile Code"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);
			} else {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);
				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}
				excelUtils.buildCellErrors(cellErrors,  messagesUtilities.getMessageWithParam("upload.excel.error.data.more.than.one", new String[]{"Country", "this Country Mobile Code"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void checkExistingEmail(List<DriverDto> listDriverDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < listDriverDto.size(); itemIndex++) {
			DriverDto dto = listDriverDto.get(itemIndex);
			if (StringUtils.isEmpty(dto.getEmail())) {
				continue;
			}

			DriverDto condition = new DriverDto();
			condition.setEmail(dto.getEmail());
			condition.setTenantId(dto.getTenantId());

			int existingItemAmount = countByCondition(condition);
			if (existingItemAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Email"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void checkExistingName(List<DriverDto> listDriverDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < listDriverDto.size(); itemIndex++) {
			DriverDto dto = listDriverDto.get(itemIndex);
			if (StringUtils.isEmpty(dto.getName())) {
				continue;
			}

			DriverDto condition = new DriverDto();
			condition.setName(dto.getName());
			condition.setTenantId(dto.getTenantId());

			int existingItemAmount = countByCondition(condition);
			if (existingItemAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Driver Name"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void checkValidLicenseType (String licenseType, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, int itemIndex) {
		if (!StringUtils.isEmpty(licenseType) && licenseType.contains("|")){
			String[] listLicenseType = licenseType.split("\\|");

			for (String licenseItem: listLicenseType){
				checkLookUp(licenseItem, mapExcelError, excelEmailErrors, itemIndex);
			}
		} else if (!StringUtils.isEmpty(licenseType)){
			checkLookUp(licenseType, mapExcelError, excelEmailErrors, itemIndex);
		}

	}

	public void checkLookUp (String licenseType, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, int itemIndex){
		LookupFilter lookupFilter = new LookupFilter();
		lookupFilter.setLookupDescription(licenseType);
		lookupFilter.setLookupType("DriverLicenseType");
		lookupFilter.setTenantId(0l);
		Lookup lookup = lookupService.findByLookupDescriptionLookupTypeTenantId(lookupFilter);
		if(lookup == null){
			StringBuilder cellErrors = mapExcelError.get(itemIndex);

			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"License Type"}));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	public void validTransporter (List<DriverDto> driverDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId, Long currentTenantUserId, String token){
		Page<TransporterDto> validTransporters = transporterService.findBySearch(new PartnersFilter(currentTenantId, 0, Integer.MAX_VALUE), currentTenantUserId, token);
		Map<String, Boolean> transporterNameMap = validTransporters.getContent().stream().collect(Collectors.toMap(TransporterDto::getPartnerName, transporter -> true));
		for (int itemIndex = 0; itemIndex < driverDtoList.size(); itemIndex++){
			DriverDto driverDto = driverDtoList.get(itemIndex);

			if (StringUtils.isEmpty(driverDto.getTransporterName())){
				continue;
			}

			driverDto.setTenantId(currentTenantId);
			List<Partners> listTransporter = partnersService.findByTenantIdAndPartnerNameAndLookupCode(currentTenantId, driverDto.getTransporterName(), AppConstants.PartnerType.TRANSPORTER);

			if (listTransporter.size() == 1) {
				Partners partner = listTransporter.get(0);
				driverDto.setTransporterId(partner.getId());

			} else if (listTransporter.isEmpty()) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Transporter"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);

			} else {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.more.than.one", new String[]{"Transporter", "this Transporter Name"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);
			}

			if (Objects.isNull(transporterNameMap.get(driverDto.getTransporterName()))) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.no.access", new String[]{"Driver", "transporter"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);
			}

			checkValidLicenseType(driverDto.getLicenceType(),mapExcelError,excelEmailErrors,itemIndex);
		}
	}

	public void saveExcelData(List<DriverDto> listDriverDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors){
		for (int itemIndex = 0; itemIndex < listDriverDto.size(); itemIndex++){
			DriverDto driverDto = listDriverDto.get(itemIndex);
			try {
				Driver driver = new Driver();
				BeanCopier copier = BeanCopier.create(DriverDto.class, Driver.class, false);
				copier.copy(driverDto, driver, null);
				// save address contact or reuse addressContactId if exist
				Optional<AddrContact> savedAddrContact = Optional
						.ofNullable(addrContactClient.createOrUpdateAddressContact(mapAddrContactDto(driverDto)));
				savedAddrContact.ifPresent(value -> driver.setAddressContactId(value.getId()));
				if (driverDto.getTransporterId() != null) {
					driver.setPartners(partnersService.findById(driverDto.getTransporterId()));
				}
				saveAndCreateTenantAccount(driver, savedAddrContact.get(), driverDto.getUserId());
			} catch (Exception e) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.saving.failed", null));
				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.saving.failed", null));
				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	private AddrContactDTO mapAddrContactDto(DriverDto driverDto) {
		AddrContactDTO addrContactDTO = new AddrContactDTO();

		BeanCopier copier = BeanCopier.create(DriverDto.class, AddrContactDTO.class, false);
		copier.copy(driverDto, addrContactDTO, null);
		return addrContactDTO;
	}

	public void validateAndSaveExcelData(Long currentTenantId, Long currentTenantUserId ,Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo, String token) {
		List<DriverDto> listDriverFromExcel = excelUtils.parseExcelToDto(DriverDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		String emailTemplateName = StringUtils.EMPTY;

		checkDuplicatedItemByName(listDriverFromExcel, mapExcelError, excelEmailErrors);
		checkDuplicatedItemByEmail(listDriverFromExcel, mapExcelError, excelEmailErrors);
		checkExistingEmail(listDriverFromExcel,mapExcelError,excelEmailErrors);
		checkExistingName(listDriverFromExcel,mapExcelError,excelEmailErrors);
		validCountryMobileCode(listDriverFromExcel,mapExcelError,excelEmailErrors);
		validTransporter(listDriverFromExcel,mapExcelError,excelEmailErrors, currentTenantId, currentTenantUserId, token);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listDriverFromExcel, mapExcelError, excelEmailErrors);

			//if there is error while saving
			if (!mapExcelError.isEmpty() || !excelEmailErrors.isEmpty()) {
				emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
			} else {
				emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.success", null);
			}
		}

		byte[] attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError, uploadTemplateHdrIdDto, timeStartReadingFile);

		// Send email after uploading
		StringBuilder excelEmailErrorsVl = new StringBuilder();
		excelEmailErrors.forEach(cellError -> {
			excelEmailErrorsVl.append(cellError);
		});

		excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.drivers", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);

	}

	@Override
	public Long getNoDriverByTenantId(Long tenantId) {
		return driverRepository.countAllByTenantId(tenantId);
	}

	@Override
	public Long getTransporterIdByDriver(Long driverId) throws TransactionException {
		try {
			Driver driver = driverRepository.getById(driverId);
			Partners transporter = driver.getPartners();
			return transporter.getId();
		} catch (Exception ex) {
			throw new TransactionException("exception.driver.no.transporter");
		}
	}
}
