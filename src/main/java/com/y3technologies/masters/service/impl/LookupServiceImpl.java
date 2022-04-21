package com.y3technologies.masters.service.impl;

import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.LookupDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import java.util.Collections;
import java.util.Objects;

import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import com.y3technologies.masters.util.ConstantVar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.QLookup;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.util.EntitySpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class LookupServiceImpl implements LookupService {

	private final LookupRepository lookupRepository;

	@Autowired
	private ExcelClient excelClient;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private MastersApplicationPropertiesConfig propertiesConfig;

	@Autowired
	MessagesUtilities messagesUtilities;

	@Autowired
	private ThreadPoolTaskExecutor executor;

	@Autowired
	private AasClient aasClient;

	private static final Logger logger = LoggerFactory.getLogger(LookupServiceImpl.class);

	@Override
	public List<Lookup> listByParam(Lookup lookup) {
		EntitySpecificationBuilder<Lookup> es = new EntitySpecificationBuilder<Lookup>();
		if (lookup.getTenantId() != null) {
			es.with("tenantId", ":", lookup.getTenantId(), "and");
		} else {
			lookup.setTenantId(0L);
			es.with("tenantId", ":", 0, "and");
		}
		if (lookup.getCustomerId() != null) {
			es.with("customerId", ":", lookup.getCustomerId(), "and");
		}
		if (StringUtils.isNoneEmpty(lookup.getLookupType())) {
			es.with("lookupType", ":", lookup.getLookupType(), "and");
		}
		es.with("activeInd", ":", true, "and");
		Sort sort = Sort.by(Sort.Direction.ASC, "seq");
		List<Lookup> list = lookupRepository.findAll(es.build(), sort);
		if (0 == list.size() && !lookup.getTenantId().equals(0L)) {
			EntitySpecificationBuilder<Lookup> es_tenantId_0 = new EntitySpecificationBuilder<Lookup>();
			es_tenantId_0.with("tenantId", ":", 0, "and");
			if (lookup.getCustomerId() != null) {
				es_tenantId_0.with("customerId", ":", lookup.getCustomerId(), "and");
			}
			if (StringUtils.isNoneEmpty(lookup.getLookupType())) {
				es_tenantId_0.with("lookupType", ":", lookup.getLookupType(), "and");
			}
			es_tenantId_0.with("activeInd", ":", true, "and");
			list = lookupRepository.findAll(es_tenantId_0.build(), sort);
		}
		return list;
	}

	@Override
	public List<Lookup> getAllLookup(Long tenantId) {
		List<Lookup> list = lookupRepository.findAllByTenantId(tenantId);
		return list;
	}

	@Override
	public Lookup findById(Long id) {
		return lookupRepository.findById(id).get();
	}

	@Override
	public Lookup save(Lookup lookup) {
		return lookupRepository.save(lookup);
	}

	@Override
	public Page<Lookup> findBySearch(LookupFilter filter) {

		if (filter == null || filter.getPageNo() < 0 || filter.getPageSize() < 1)
			return Page.empty();

		QLookup lookup = QLookup.lookup;
		BooleanExpression query = lookup.isNotNull();

		query = query.and(lookup.tenantId.eq(0L));

		if (filter.getTenantId() != null && filter.getTenantId() > 0L) {
			// If Tenant ID is NOT NULL then get only lookup created by Tenant itself;
			query = query.or(lookup.tenantId.eq(filter.getTenantId()));
		}

		if (StringUtils.isNoneBlank(filter.getLookupCode())) {
			query = query.and(lookup.lookupCode.like("%" + filter.getLookupCode() + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getLookupType())) {
			query = query.and(lookup.lookupType.like("%" + filter.getLookupType() + "%"));
		}
		Page<Lookup> lookupsPage;
		if (!StringUtils.isEmpty(filter.getSortBy())) {
			lookupsPage = lookupRepository.findAll(query, PageRequest.of(filter.getPageNo(),  filter.getPageSize(), Sort.by(filter.getSort())));
		} else {
			lookupsPage = lookupRepository.findAll(query, PageRequest.of(filter.getPageNo(), filter.getPageSize()));
		}

		return lookupsPage;
	}

	@Override
	public void updateStatus(Long id, Boolean status) {

		Lookup lookup = this.findById(id);

		if (lookup == null)
			return;

		lookup.setActiveInd(status);

		this.save(lookup);
	}

	@Override
	public List<Lookup> findByFilter(LookupFilter filter) {

		BooleanExpression query = this.queryString(filter);

		List<Lookup> lookupList = lookupRepository.findAll(query, Sort.by("seq"));

		if (CollectionUtils.isNotEmpty(lookupList))
			return lookupList;

		if (filter.getTenantId() != null && filter.getTenantId() != 0L) {

			filter.setTenantId(0L);

			return lookupRepository.findAll(this.queryString(filter), Sort.by("seq"));
		}

		return Collections.emptyList();
	}

	private BooleanExpression queryString(LookupFilter filter) {

		QLookup lookup = QLookup.lookup;
		BooleanExpression query = lookup.isNotNull();

		if (filter.getTenantId() != null) {
			// If Tenant ID is NOT NULL then get only lookup created by Tenant itself;
			query = query.and(lookup.tenantId.eq(filter.getTenantId()));
		} else {
			query = query.and(lookup.tenantId.eq(0L));
		}

		if (StringUtils.isNoneBlank(filter.getLookupCode())) {
			String keyword = filter.getLookupCode().trim();
			query = query.and(lookup.lookupCode.like("%" + keyword + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getLookupType())) {
			String keyword = filter.getLookupType().trim();
			query = query.and(lookup.lookupType.eq(keyword));
		}

		if (filter.getActive() != null) {
			query = query.and(lookup.activeInd.eq(filter.getActive()));
		}

		if (CollectionUtils.isNotEmpty(filter.getLookupIdList())) {
			query = query.and(lookup.id.in(filter.getLookupIdList()));
		}

		return query;
	}

	@Override
	public Lookup findByLookupCodeLookupTypeTenantId(LookupFilter filter) {
		return lookupRepository.findByLookupTypeAndLookupCodeAndTenantId(filter.getLookupType(), filter.getLookupCode(), filter.getTenantId());
	}

	@Override
	public List<Lookup> findByLookupType (String lookupType) {
		return lookupRepository.findByLookupType(lookupType);
	}

	@Override
	public Lookup findByLookupDescriptionLookupTypeTenantId(LookupFilter filter) {
		return lookupRepository.findByLookupTypeAndLookupDescriptionAndTenantId(filter.getLookupType(), filter.getLookupDescription(), filter.getTenantId());
	}

	public List<Lookup> findByLookupDescriptionAndTenantId(String lookupDescription, Long tenantId) {
		return lookupRepository.findByLookupDescriptionAndTenantId(lookupDescription, tenantId);
	}

	public List<Lookup> findByLookupTypeTenantId(String lookupCode, Long tenantId) {
		return lookupRepository.findAllByLookupTypeAndTenantId(lookupCode, tenantId);
	}

	@Override
	public ExcelResponseMessage uploadFile(MultipartFile file, Long tenantId) {
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap<>();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.LOOKUP_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(LookupDto.class, workbook, sheet, fileName
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

	private void checkExistedEntries(List<LookupDto> lookupDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long tenantId) {
		for (int itemIndex = 0; itemIndex < lookupDtoList.size(); itemIndex++) {
			LookupDto dto = lookupDtoList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getLookupCode()) || StringUtils.isEmpty(dto.getLookupType())) {
				continue;
			}

			int existedLookUpAmount = countByCondition(tenantId, dto.getLookupCode(), dto.getLookupType());
			if (existedLookUpAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Lookup Code and Lookup Type"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public int countByCondition (Long tenantId, String lookUpCode, String lookUpType){
		return lookupRepository.countByTenantIdAndLookupCodeAndLookupType(tenantId, lookUpCode, lookUpType);
	}

	private void checkDuplicatedEntries(List<LookupDto> lookupDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (LookupDto dto : lookupDtoList) {
			if (StringUtils.isEmpty(dto.getLookupCode()) || StringUtils.isEmpty(dto.getLookupType())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getLookupCode().trim().toLowerCase() + "||" + dto.getLookupType().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getLookupCode().trim().toLowerCase() + "||" + dto.getLookupType().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < lookupDtoList.size(); itemIndex++) {
			LookupDto dto = lookupDtoList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getLookupCode()) || StringUtils.isEmpty(dto.getLookupType())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getLookupCode().trim().toLowerCase() + "||" + dto.getLookupType().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("lookup.excel.duplicate.line",
					new String[]{mapCheckDup.get(dto.getLookupCode().trim().toLowerCase()
							+ "||" + dto.getLookupType().trim().toLowerCase()), dto.getLookupCode(),dto.getLookupType() }));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	private void saveExcelData(List<LookupDto> listLookUpDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long tenantId) {
		for (int itemIndex = 0; itemIndex < listLookUpDto.size(); itemIndex++) {
			LookupDto lookupDto = listLookUpDto.get(itemIndex);
			try {
				Lookup lookup = new Lookup();
				lookupDto.setTenantId(tenantId);
				BeanCopier copier = BeanCopier.create(LookupDto.class, Lookup.class, false);
				copier.copy(lookupDto, lookup, null);
				lookupRepository.save(lookup);
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

	@Override
	public void downloadExcel(HttpServletResponse response, LookupFilter filter) {
		List<Lookup> dataList = listDataToExport(filter);
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOOKUP_SETTING_EXPORT);
		excelUtils.exportExcel(response, dataList, Lookup.class, template);
	}

	public List<Lookup> listDataToExport(LookupFilter filter) {

		QLookup lookup = QLookup.lookup;
		BooleanExpression query = lookup.isNotNull();

		query = query.and(lookup.tenantId.eq(0L));

		if (filter.getTenantId() != null && filter.getTenantId() > 0L) {
			query = query.or(lookup.tenantId.eq(filter.getTenantId()));
		}

		if (!StringUtils.isBlank(filter.getLookupType())) {
			query = query.and(lookup.lookupType.like("%" + filter.getLookupType() + "%"));
		}

		if (!StringUtils.isBlank(filter.getLookupCode())) {
			query = query.and(lookup.lookupCode.like("%" + filter.getLookupCode() + "%"));
		}

		return lookupRepository.findAll(query, Sort.by(filter.getSort()));
	}


	@Override
	public List<Lookup> findLookupByTypeAndCodeAndTenantId(LookupFilter filter) {
		QLookup qLookup = QLookup.lookup;
		BooleanExpression query = qLookup.isNotNull();

		if (!Objects.isNull(filter.getTenantId())) {
			query = query.and(qLookup.tenantId.eq(filter.getTenantId()));
		}

		if (!CollectionUtils.isEmpty(filter.getLookupTypes())) {
			query = query.and(qLookup.lookupType.in(filter.getLookupTypes()));
		}

		if (!CollectionUtils.isEmpty(filter.getLookupCodes())) {
			query = query.and(qLookup.lookupCode.in(filter.getLookupCodes()));
		}

		return lookupRepository.findAll(query);
	}

	private Map<String, Map<String, String>> convertToMapLookupTypeAndCodeAndDescription(List<Lookup> lookups) {
		Map<String, Map<String, String>> mapLookup = new HashMap<>();
		lookups.forEach(lookup -> {
			String lookUpType = lookup.getLookupType();
			String lookUpCode = lookup.getLookupCode();
			String lookUpDescription = lookup.getLookupDescription();
			Map<String, String> mapLookUpCodeAndDescription = mapLookup.get(lookUpType);
			if (Objects.isNull(mapLookUpCodeAndDescription)) {
				mapLookUpCodeAndDescription = new HashMap<>();
			}
			mapLookUpCodeAndDescription.put(lookUpCode, lookUpDescription);
			logger.info(String.format("map lookup code l:"+ lookUpType+" "  + lookUpCode +" "+ lookUpDescription));

			mapLookup.put(lookUpType, mapLookUpCodeAndDescription);
		});
		return mapLookup;
	}

	@Override
	public String getLookupDescriptionFromLookupCode(String lookupType, String lookupCode, Map<String, Map<String, String>> mapLookup) {
		if (StringUtils.isEmpty(lookupType) || StringUtils.isEmpty(lookupCode) || Objects.isNull(mapLookup.get(lookupType))) {
			return null;
		}
		logger.info(String.format("lookupType:" + lookupType +" "+ lookupCode));
		Map<String, String> mapLookUpCodeAndDescription = mapLookup.get(lookupType);
		StringBuilder lookUpDescriptionBuilder = new StringBuilder();
		String[] lookupCodes = lookupCode.split("\\|");
		logger.info(String.format("lookupCodes:" + lookupCodes));
		if (ArrayUtils.isEmpty(lookupCodes)) {
			return lookUpDescriptionBuilder.toString();
		}
		for (String lkCode: lookupCodes) {
			String lkDescription = mapLookUpCodeAndDescription.get(lkCode);
			logger.info(String.format("lkDescription:" + lkDescription));
			if (!Objects.isNull(lkDescription)) {
				lookUpDescriptionBuilder.append(lkDescription);
				lookUpDescriptionBuilder.append(ConstantVar.ORPREFIX);
			}
		}
		String lookUpDescription = lookUpDescriptionBuilder.toString();
		logger.info(String.format("lookUpDescription:" + lookUpDescription));
		if (StringUtils.isBlank(lookUpDescription)) {
			return lookUpDescription;
		}
		return lookUpDescription.substring(0, lookUpDescription.length() - 1);
	}

	@Override
	public Map<String, Map<String, String>> findLookupByTypeAndCode(List<String> lookupTypes, List<String> lookupCodes) {
		LookupFilter lookupFilter = new LookupFilter();
		lookupFilter.setLookupCodes(lookupCodes);
		lookupFilter.setLookupTypes(lookupTypes);
		List<Lookup> lookups = this.findLookupByTypeAndCodeAndTenantId(lookupFilter);
		for(Lookup l :lookups){
			logger.info(String.format("lookup code l:" + l.getLookupCode() +" "+ l.getLookupType()));
		}

		logger.info(String.format("lookup code:" + lookupCodes +" "+ lookupCodes.size()));
		logger.info(String.format("lookup type:" + lookupTypes +" "+ lookupCodes.size()));

		return this.convertToMapLookupTypeAndCodeAndDescription(lookups);
	}

	public void validateAndSaveExcelData(Long currentTenantId, Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo) {
		List<LookupDto> listLookUpAfterReadExcel = excelUtils.parseExcelToDto(LookupDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		String emailTemplateName = StringUtils.EMPTY;

		checkDuplicatedEntries(listLookUpAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listLookUpAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listLookUpAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId);

			if (!mapExcelError.isEmpty() || !excelEmailErrors.isEmpty()) {
				emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
			} else {
				emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.success", null);
			}
		}

		byte[] attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError, uploadTemplateHdrIdDto, timeStartReadingFile);

		StringBuilder excelEmailErrorsVl = new StringBuilder();
		excelEmailErrors.forEach(cellError -> {
			excelEmailErrorsVl.append(cellError);
		});

		excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.lookup", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);

	}
  
  @Override
  public boolean existById(Long lookupId) {
      return lookupRepository.existsById(lookupId);
  }

  @Override
  public List<LookupDto> listLookupDtoByLookupType(String lookupType) {
      BeanCopier beanCopier = BeanCopier.create(Lookup.class, LookupDto.class, false);
      List<Lookup> lookups = lookupRepository.findByLookupType(lookupType);
      List<LookupDto> lookupDtoList = lookups.stream().map(lookup -> {
          LookupDto lookupDto = new LookupDto();
          beanCopier.copy(lookup, lookupDto, null);
          return lookupDto;
      }).collect(Collectors.toList());
      return lookupDtoList;
  }
}
