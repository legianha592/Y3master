package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CustomerDto;
import com.y3technologies.masters.dto.DropdownPartnerDto;
import com.y3technologies.masters.dto.PartnerTypesDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.PartnerTypes;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.QPartnerTypes;
import com.y3technologies.masters.model.QPartners;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.repository.PartnerLocationRepository;
import com.y3technologies.masters.repository.PartnerTypesRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import com.y3technologies.masters.service.CustomerService;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.PartnerLocationService;
import com.y3technologies.masters.service.PartnerTypesService;
import com.y3technologies.masters.service.PartnersService;
import com.y3technologies.masters.service.ProfileScopeService;
import com.y3technologies.masters.util.BeanCopierFactory;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends BaseServiceImpl implements CustomerService {
	private final PartnersRepository partnersRepository;
	private final OperationLogService operationLogService;
	private final PartnerTypesService partnerTypesService;
	private final PartnerLocationService partnerLocationService;
	private final LookupRepository lookupRepository;
	private final PartnerLocationRepository partnerLocationRepository;
	private final PartnerTypesRepository partnerTypesRepository;
	private final AddrContactClient addrContactClient;
	private final ExcelClient excelClient;

    private final MessagesUtilities messagesUtilities;
	private final MastersApplicationPropertiesConfig propertiesConfig;
	private final PartnersService partnersService;
	private final ExcelUtils excelUtils;
	private final ProfileScopeService profileScopeService;
	private final ThreadPoolTaskExecutor executor;
	private final AasClient aasClient;

	@Override
	@Transactional
	public Partners save(CustomerDto dto) {
		Partners model = new Partners();

		Boolean updateFlag = Boolean.TRUE;
		Boolean newPartner = Boolean.TRUE;

		if (dto.getId() != null) {
			Partners oldModel = partnersRepository.findById(dto.getId()).get();
			updateFlag = oldModel.getHashcode() != model.hashCode();
			newPartner = Boolean.FALSE;
			model = oldModel;
			model.setActiveInd(dto.getActiveInd());
		}else{
			BeanCopierFactory.CustomerDto_partners_copier.copy(dto,  model, null);
		}
		model.setHashcode(model.hashCode());
		model.setPartnerCode(dto.getPartnerCode());

		AddrContactDTO addrContactDto = new AddrContactDTO();
		// save address contact or reuse addressContactId if exist
		BeanCopierFactory.CustomerDto_addressContactDto_copier.copy(dto, addrContactDto, null);
		Optional<AddrContact> savedAddrContact = Optional
				.ofNullable(addrContactClient.createOrUpdateAddressContact(addrContactDto));

		partnersRepository.save(model);

		if (updateFlag) {
			operationLogService.log(dto.getId() == null, model, model.getClass().getSimpleName(), model.getId().toString());
		}

		if (newPartner) {
			Lookup lookup = lookupRepository.findByLookupTypeAndLookupCode(AppConstants.LookupType.PARTNER_TYPES, AppConstants.PartnerType.CUSTOMER);
			PartnerTypes partnerTypes = new PartnerTypes();
			partnerTypes.setPartners(model);
			partnerTypes.setLookup(lookup);
			partnerTypesService.save(partnerTypes);
		}

		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(model.getId());
		PartnerLocation partnerLocation = new PartnerLocation();
		if(partnerLocationList.isEmpty()){
			partnerLocation.setPartners(model);
		}else{
			//CUSTOMER HAVE ONLY ONE PARTNER LOCATION
			partnerLocation = partnerLocationList.get(0);
		}
		partnerLocation.setAddressContactId(savedAddrContact.get().getId());
		partnerLocationService.save(partnerLocation);
		return model;
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerDto getById(Long id) {
		Partners partners = partnersRepository.findById(id).get();
		CustomerDto dto = null;
		if (partners != null) {
			dto = new CustomerDto();
			BeanCopierFactory.Partners_customerDto_copier.copy(partners,  dto, null);

			List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(id);
			if(!partnerLocationList.isEmpty()) {
				PartnerLocation partnerLocation = partnerLocationList.get(0);
				Long addressContactId = partnerLocation.getAddressContactId();
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
				dto.setAddrContact(addrContact);
			}
		}
		return dto;
	}

	@Override
	public Partners findById(Long id) {
		Partners partners = partnersRepository.findById(id).orElse(null);
		if(partners != null) {
			List<PartnerTypesDto> partnerTypesList = partnerTypesRepository.findByPartnerId(partners.getId());
			boolean customerPartner = partnerTypesList.stream().anyMatch(partnerTypesDto -> partnerTypesDto.getPartnerType().equals(AppConstants.PartnerType.CUSTOMER));
			if(customerPartner){
				return partners;
			}else{
				return null;
			}
		}
		return null;
	}

	@Override
	public Page<Partners> findBySearch(PartnersFilter filter, Long tenantUserId, String token) {

		if (filter == null || filter.getPageNo() < 0 || filter.getPageSize() < 1)
			return Page.empty();

		QPartners partners = QPartners.partners;
		QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
		BooleanExpression query = partners.isNotNull();

		query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.CUSTOMER, token);

		if (filter.getTenantId() != null) {
			query = query.and(partners.tenantId.eq(filter.getTenantId()));
		}

		if (StringUtils.isNoneBlank(filter.getName())) {
			String keyword = filter.getName().trim();
			query = query.and(partners.partnerName.like("%" + keyword + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getPartnerCode())) {
			String keyword = filter.getPartnerCode().trim();
			query = query.and(partners.partnerCode.like("%" + keyword + "%"));
		}

		if(filter.getActiveInd() != null){
			query = query.and(partners.activeInd.eq(filter.getActiveInd()));
		}

		query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
				.where(partnerTypes.lookup.lookupCode.eq(AppConstants.PartnerType.CUSTOMER))));

		Page<Partners> partnersPage;
		if (!StringUtils.isEmpty(filter.getSortBy())) {
			partnersPage = partnersRepository.findAll(query, PageRequest.of(filter.getPageNo(), filter.getPageSize(), Sort.by(filter.getSort())));
		} else {
			partnersPage = partnersRepository.findAll(query, PageRequest.of(filter.getPageNo(), filter.getPageSize(), Sort.by("activeInd").descending().and(Sort.by("partnerCode").ascending())));
		}
		return partnersPage;
	}

	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		Partners model = partnersRepository.findById(id).get();
		if (model.getActiveInd() != status) {
			partnersRepository.updateStatus(id, status);
			operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	@Override
	public List<Partners> findByFilter(PartnersFilter filter, Long tenantUserId, boolean isCallFromFindByFilterController, String token) {

		QPartners partners = QPartners.partners;
		QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
		BooleanExpression query = partners.isNotNull();

		if (isCallFromFindByFilterController) {
			query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.CUSTOMER, token);
		}

		if (filter.getTenantId() != null) {
			query = query.and(partners.tenantId.eq(filter.getTenantId()));
		}

		if (StringUtils.isNoneBlank(filter.getName())) {
			String keyword = filter.getName().trim();

			if(filter.getIsCreateCustomer()){
				query = query.and(partners.partnerName.eq(keyword));
			}else{
				query = query.and(partners.partnerName.like("%" + keyword + "%"));
			}
		}

		if (StringUtils.isNoneBlank(filter.getPartnerCode())) {
			String keyword = filter.getPartnerCode().trim();
			if(filter.getIsCreateCustomer()){
				query = query.and(partners.partnerCode.eq(keyword));
			}else{
				query = query.and(partners.partnerCode.like("%" + keyword + "%"));
			}
		}

		query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
				.where(partnerTypes.lookup.lookupCode.eq(AppConstants.PartnerType.CUSTOMER))));

		if (CollectionUtils.isNotEmpty(filter.getPartnerIdList())) {
			query = query.and(partners.id.in(filter.getPartnerIdList()));
		}

		if(filter.getActiveInd() != null){
			query = query.and(partners.activeInd.eq(filter.getActiveInd()));
		}

		return partnersRepository.findAll(query);
	}

	@Override
	public List<Partners> getAllByTenantId(Long tenantId) {

		QPartners partners = QPartners.partners;
		QPartnerTypes partnerType = QPartnerTypes.partnerTypes;
		BooleanExpression query = partners.isNotNull();

		if (tenantId!=null){
			query = query.and(partners.tenantId.eq(tenantId));
		}

		query = query.and(partners.id.in(JPAExpressions.select(partnerType.partners.id).from(partnerType)
				.where(partnerType.lookup.lookupCode.eq(AppConstants.PartnerType.CUSTOMER))));

		List<Partners> customerList = partnersRepository.findAll(query);
		setAddrContactToExport(customerList);

		return customerList;
	}

	private void setAddrContactToExport(List<Partners> partnersList) {

		List<Long> listPartnerIds = new ArrayList<>();
		partnersList.stream().forEach(partners -> {
			listPartnerIds.add(partners.getId());
		});
		if (listPartnerIds.isEmpty()) {
			return;
		}
		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnersIds(listPartnerIds);
		List<Long> listAddrContactId = new ArrayList<>();
		Map<Long, Partners> getPartnerByContactId = new HashMap<>();
		partnerLocationList.stream().forEach(partnerLocation -> {
			getPartnerByContactId.put(partnerLocation.getAddressContactId(), partnerLocation.getPartners());
			listAddrContactId.add(partnerLocation.getAddressContactId());
		});

		if (!CollectionUtils.isEmpty(listAddrContactId)) {
			List<AddrContact> listAddrContact = addrContactClient.getAddressContactList(listAddrContactId);

			Map<Long, AddrContact> getPartnerByAddrContact = new HashMap<>();
			if (!listAddrContact.isEmpty()) {
				listAddrContact.stream().forEach(addrContact -> {
					if (addrContact.getId() != null) {
						Partners partner = getPartnerByContactId.get(addrContact.getId());
						getPartnerByAddrContact.put(partner.getId(), addrContact);
					}
				});
			}
			partnersList.stream().forEach(partner -> {
				AddrContact addrContact = getPartnerByAddrContact.get(partner.getId());
				if (addrContact != null && !StringUtils.isEmpty(addrContact.getMobileNumber1())) {
					partner.setPhone(addrContact.getMobileNumber1());
					partner.setPhoneCountryShortName(addrContact.getMobileNumber1CountryShortName());
					partner.setPhoneAreaCode(addrContact.getTelephoneAreaCode());
				}
			});
		}
	}

	@Override
	public int countByCustomerName(String customerName, Long currentTenantId) {
		return partnersRepository.countByPartnerNameAndTenantId(customerName, AppConstants.PartnerType.CUSTOMER, currentTenantId);
	}

	public ExcelResponseMessage uploadExcel(MultipartFile file, Long tenantId) {
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(CustomerDto.class, workbook, sheet, fileName
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
		executor.execute(() -> {
			try {
				validateAndSaveExcelData(tenantId, workbook, sheet, fileName, mapExcelError, excelEmailErrors, uploadTemplateHdrIdDto, timeStartReadingFile, userInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return excelResponseMessage;
	}

	public void checkDuplicatedEntries(List<CustomerDto> customerDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (CustomerDto dto : customerDtoList) {
			if (StringUtils.isEmpty(dto.getPartnerName())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getPartnerName().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getPartnerName().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < customerDtoList.size(); itemIndex++) {
			CustomerDto dto = customerDtoList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getPartnerName())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getPartnerName().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("customer.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getPartnerName().trim().toLowerCase()), dto.getPartnerName()}));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	private void checkExistedEntries(List<CustomerDto> customerDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long tenantId) {
		for (int itemIndex = 0; itemIndex < customerDtoList.size(); itemIndex++) {
			CustomerDto dto = customerDtoList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getPartnerName())) {
				continue;
			}

			int existedCustomerAmount = countByCustomerName(dto.getPartnerName(), tenantId);
			if (existedCustomerAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Customer Name"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	private void validCountryMobileCode(List<CustomerDto> customerDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		for (int itemIndex = 0; itemIndex < customerDtoList.size(); itemIndex++) {
			CustomerDto customerDto = customerDtoList.get(itemIndex);
			if (StringUtils.isEmpty(customerDto.getMobileNumber1CountryShortName())) {
				continue;
			}
			List<CountryDTO> countryDTOList = addrContactClient.findByCountryIsdCode(customerDto.getMobileNumber1CountryShortName());
			if (countryDTOList.size() == 1) {
				customerDto.setMobileNumber1CountryShortName(countryDTOList.get(0).getCountryShortName());
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
				excelUtils.buildCellErrors(cellErrors,   messagesUtilities.getMessageWithParam("upload.excel.error.data.more.than.one", new String[]{"Country", "this Country Mobile Code"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	private void saveExcelData(List<CustomerDto> listCustomerDto, Map<Integer, StringBuilder> mapExcelError, Long tenantId, Set<String> excelEmailErrors) {
		for (int itemIndex = 0; itemIndex < listCustomerDto.size(); itemIndex++) {
			CustomerDto customerDto = listCustomerDto.get(itemIndex);
			customerDto.setTenantId(tenantId);
			try {
				save(customerDto);
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

	public void downloadExcel(PartnersFilter filter, HttpServletResponse response, Long tenantUserId) {
		Page<Partners> dataPage = findBySearch(filter, tenantUserId, null);
		List<Partners> dataList = dataPage.toList();
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_EXPORT);
		excelUtils.exportExcel(response, dataList, Partners.class, template);
	}

	@Override
	public Partners findByName(Long tenantId, String customerName) {
		List<Partners> customers = partnersService.findByTenantIdAndPartnerNameAndLookupCode(tenantId, customerName, AppConstants.PartnerType.CUSTOMER);

		if (customers.isEmpty()) {
			return null;
		}
		return customers.get(0);
	}

	public void validateAndSaveExcelData(Long currentTenantId, Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo) {
		List<CustomerDto> listCustomerAfterReadExcel = excelUtils.parseExcelToDto(CustomerDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		String emailTemplateName = StringUtils.EMPTY;

		checkDuplicatedEntries(listCustomerAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listCustomerAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId);
		validCountryMobileCode(listCustomerAfterReadExcel, mapExcelError, excelEmailErrors);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listCustomerAfterReadExcel, mapExcelError, currentTenantId, excelEmailErrors);

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

		excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.customer", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);
	}

	@Override
	public Page<DropdownPartnerDto> findByCustomerName(Long tenantId, Long tenantUserId, String term, Pageable pageable) {
		QPartners partners = QPartners.partners;
		BooleanExpression query = partners.isNotNull();
		query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.CUSTOMER, null);
		return partnersRepository.findByNameAndTypeSelectIdAndName(tenantId, term, AppConstants.PartnerType.CUSTOMER, pageable, query);
	}

    @Override
    public boolean existById(Long customerId) {
        return partnersRepository.existsById(customerId);
    }
}
