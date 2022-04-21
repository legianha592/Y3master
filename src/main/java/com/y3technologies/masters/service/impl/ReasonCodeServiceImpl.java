package com.y3technologies.masters.service.impl;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.ReasonCodeExcelDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.ReasonCodeFilter;
import com.y3technologies.masters.dto.table.ReasonCodeTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.QReasonCode;
import com.y3technologies.masters.model.ReasonCode;
import com.y3technologies.masters.repository.ReasonCodeRepository;
import com.y3technologies.masters.service.FtpJSchService;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.ReasonCodeService;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.EntitySpecificationBuilder;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import com.y3technologies.masters.util.SortingUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ReasonCodeServiceImpl implements ReasonCodeService {

	@Autowired
	private ReasonCodeRepository reasonCodeRepository;

	@Autowired
	private OperationLogService operationLogService;

	private final String FILE_NAME = "default_reason_code";
	private final String FILE_EXTENSION = "xlsx";
	private static final Logger logger = LoggerFactory.getLogger(ReasonCodeServiceImpl.class);
	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private MastersApplicationPropertiesConfig mastersApplicationPropertiesConfig;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private ExcelClient excelClient;

	@Autowired
	private MessagesUtilities messagesUtilities;

	@Autowired
	private MastersApplicationPropertiesConfig propertiesConfig;

	@Autowired
	private ThreadPoolTaskExecutor executor;

	@Autowired
	private AasClient aasClient;

	@Autowired
	private FtpJSchService ftpJSchService;

	@Override
	@Transactional
	public void save(ReasonCode model) {
		Long id = model.getId();
		Boolean updateFlag = Boolean.TRUE;
		if (model.getId() != null) {
			ReasonCode oldModel = getById(model.getId());
			updateFlag = oldModel.getHashcode() != model.hashCode();
		}
		model.setHashcode(model.hashCode());
		reasonCodeRepository.save(model);
		if (updateFlag) {
			operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
		}
	}

	@Override
	public boolean existsById(Long id) {
		return reasonCodeRepository.existsById(id);
	}

	@Override
	public List<ReasonCode> listByParam(ReasonCode model) {
		EntitySpecificationBuilder<ReasonCode> es = new EntitySpecificationBuilder<ReasonCode>();
		es.with("tenantId", ":", model.getTenantId(), "and");
		List<ReasonCode> list = reasonCodeRepository.findAll(es.build());
		return list;
	}

	@Override
	public List<ReasonCode> findBySearch(ReasonCodeFilter filter) {

		if (filter == null)
			return new ArrayList<>();

		QReasonCode reasonCode = QReasonCode.reasonCode1;
		BooleanExpression query = reasonCode.isNotNull();

		if (filter.getTenantId() == null) {
			// If Tenant ID is NULL then get only default data;
			query = query.and(reasonCode.tenantId.eq(0L));
		} else {
			query = query.and(reasonCode.tenantId.eq(filter.getTenantId()));
		}

		query = addQuery(query, reasonCode, filter);

		List<ReasonCode> list = reasonCodeRepository.findAll(query);

		if (list == null || list.size() == 0) {
			BooleanExpression queryWithDefaultTenant = reasonCode.isNotNull();
			queryWithDefaultTenant = queryWithDefaultTenant.and(reasonCode.tenantId.eq(0L));
			queryWithDefaultTenant = addQuery(queryWithDefaultTenant, reasonCode, filter);
			return reasonCodeRepository.findAll(queryWithDefaultTenant);
		} else {
			return list;

		}
	}

	private BooleanExpression addQuery(BooleanExpression query, QReasonCode reasonCode, ReasonCodeFilter filter) {
		if (StringUtils.isNoneBlank(filter.getCategory())) {
			query = query.and(reasonCode.category.like("%" + filter.getCategory() + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getReasonCode())) {
			query = query.and(reasonCode.reasonCode.like("%" + filter.getReasonCode() + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getUsage())) {
			query = query.and(reasonCode.usage.like("%" + filter.getUsage() + "%"));
		}

		if (filter.getActiveInd() != null) {
			query = query.and(reasonCode.activeInd.eq(filter.getActiveInd()));
		}

		if (CollectionUtils.isNotEmpty(filter.getReasonCodeIdList())) {
			query = query.and(reasonCode.id.in(filter.getReasonCodeIdList()));
		}

		return query;
	}

	@Override
	public DataTablesOutput<ReasonCode> query(DataTablesInput input) {
		if(input.getColumn("tenantId") != null
				&& StringUtils.isNotEmpty(input.getColumn("tenantId").getSearch().getValue())
				&& input.getColumn("tenantId").getSearchable()) {
			Specification<ReasonCode> sTenantId = new Specification<ReasonCode>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Predicate toPredicate(Root<ReasonCode> root, CriteriaQuery<?> query,
											 CriteriaBuilder criteriaBuilder) {
					return criteriaBuilder.equal(root.get("tenantId"), input.getColumn("tenantId").getSearch().getValue());
				}
			};
			input.getColumn("tenantId").setSearchable(false);

			return reasonCodeRepository.findAll(input, sTenantId);
		}
		return reasonCodeRepository.findAll(input);
	}

	@Override
	public ReasonCode getById(Long id) {
		return reasonCodeRepository.findById(id).get();
	}

	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		ReasonCode model = getById(id);
		if (model.getActiveInd() != status) {
			reasonCodeRepository.updateStatus(id, status);
			operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	@Override
	@Transactional
	public void populateSystemReasonCodeForUser(Long tenantId) {
		List<ReasonCodeExcelDto> reasonCodeExcelDtoList = getReasonCodesFromExcel();
		List<ReasonCode> reasonCodeList = new ArrayList<>();
		reasonCodeExcelDtoList.forEach(reasonCode -> {
			reasonCodeList.add(mapReasonCodeFromExcel(reasonCode, tenantId));
		});
		reasonCodeRepository.saveAll(reasonCodeList);

	}

	private List<ReasonCodeExcelDto> getReasonCodesFromExcel() {
		List<ReasonCodeExcelDto> reasonCodeDTOList = new ArrayList<>();
		try {
			StringBuilder filePath = new StringBuilder().append(mastersApplicationPropertiesConfig.getFileServerBasePath());
            filePath.append("/").append(AppConstants.FTP_DEFAULT_FOLDER_NAME).append("/").append(FILE_NAME).append(".").append(FILE_EXTENSION);
            InputStream in = ftpJSchService.readFile(filePath.toString());
            StringBuilder fileName = new StringBuilder().append(FILE_NAME).append(".").append(FILE_EXTENSION);
            MultipartFile file = new MockMultipartFile(fileName.toString(), fileName.toString(), ContentType.APPLICATION_OCTET_STREAM.toString(), in);
			reasonCodeDTOList = excelUtils.readExcel(ReasonCodeExcelDto.class, file);
		} catch (Exception e) {
			logger.debug(e.getMessage());
		}
		return reasonCodeDTOList;
	}

	private ReasonCode mapReasonCodeFromExcel(ReasonCodeExcelDto reasonCodeExcelDto, Long tenantId) {
		ReasonCode reasonCode = modelMapper.map(reasonCodeExcelDto, ReasonCode.class);
		reasonCode.setTenantId(tenantId);
		reasonCode.setUsage(reasonCodeExcelDto.getReasonUsage());
		reasonCode.setIsDefault(Boolean.TRUE);
//		List<String> reasonUsage = Arrays.asList(reasonCodeExcelDto.getReasonUsage().split(","));
		return reasonCode;
	}

	@Override
	public List<ReasonCode> findByTenantId(Long tenantId, ReasonCodeFilter filter) {
		QReasonCode qReasonCode = QReasonCode.reasonCode1;
		BooleanExpression query = qReasonCode.isNotNull();
		query = query.and(qReasonCode.tenantId.eq(tenantId));

		if (!StringUtils.isBlank(filter.getReasonCode())) {
			query = query.and(qReasonCode.reasonCode.like("%" + filter.getReasonCode() + "%"));
		}

		if (!StringUtils.isBlank(filter.getReasonDescription())) {
			query = query.and(qReasonCode.reasonDescription.like("%" + filter.getReasonDescription() + "%"));
		}

		if (!StringUtils.isBlank(filter.getCategory())) {
			query = query.and(qReasonCode.category.like("%" + filter.getCategory() + "%"));
		}

		if (!StringUtils.isBlank(filter.getUsage())) {
			query = query.and(qReasonCode.usage.like("%" + filter.getUsage() + "%"));
		}

		if (!StringUtils.isBlank(filter.getInducedBy())) {
			query = query.and(qReasonCode.inducedBy.like("%" + filter.getInducedBy() + "%"));
		}

		List<ReasonCode> reasonCodeList = reasonCodeRepository.findAll(query);
		this.sortByMultiFields(filter.getSort(), reasonCodeList);
		return reasonCodeList;
	}

	private void sortByMultiFields(List<Sort.Order> lstSort, List<ReasonCode> reasonCodeList) {
		Comparator<ReasonCode> comparator = Comparator.comparing(ReasonCode::getReasonCode, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
		if (lstSort.isEmpty()) {
			reasonCodeList.sort(comparator);
			return;
		}
		Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(0).getDirection().isAscending());
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_CODE)) {
			comparator = Comparator.comparing(ReasonCode::getReasonCode, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_DESCRIPTION)) {
			comparator = Comparator.comparing(ReasonCode::getReasonDescription, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_CATEGORY)) {
			comparator = Comparator.comparing(ReasonCode::getCategory, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_USAGE_LEVEL)) {
			comparator = Comparator.comparing(ReasonCode::getUsage, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_INDUCED_BY)) {
			comparator = Comparator.comparing(ReasonCode::getInducedBy, sortOrder);
		}
		if (Objects.equals(1, lstSort.size())) {
			comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
			reasonCodeList.sort(comparator);
			return;
		}
		for (int i = 1; i < lstSort.size(); i++) {
			sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(i).getDirection().isAscending());
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_CODE)) {
				comparator = comparator.thenComparing(ReasonCode::getReasonCode, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_DESCRIPTION)) {
				comparator = comparator.thenComparing(ReasonCode::getReasonDescription, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_CATEGORY)) {
				comparator = comparator.thenComparing(ReasonCode::getCategory, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_USAGE_LEVEL)) {
				comparator = comparator.thenComparing(ReasonCode::getUsage, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.REASON_CODES_INDUCED_BY)) {
				comparator = comparator.thenComparing(ReasonCode::getInducedBy, sortOrder);
			}
		}
		comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
		reasonCodeList.sort(comparator);
	}

	@Override
	public int countByCondition(String reasonCode, String reasonDescription, Long tenantId){
		return reasonCodeRepository.countByReasonCodeAndReasonDescriptionAndTenantId(reasonCode, reasonDescription, tenantId);
	}

	@Override
	public ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId){
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.REASONCODE_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(ReasonCodeTableDto.class, workbook, sheet, fileName
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
				validateAndSaveExcelData(currentTenantId, workbook, sheet, fileName, mapExcelError, excelEmailErrors, uploadTemplateHdrIdDto, timeStartReadingFile, userInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return excelResponseMessage;
	}

	public void checkDuplicatedEntries(List<ReasonCodeTableDto> listReasonCode, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (ReasonCodeTableDto dto : listReasonCode) {
			if (dto.getReasonCode() == null || dto.getReasonDescription() == null) {
				continue;
			}
			String listPositionDup = mapCheckDup.get((dto.getReasonCode().trim() + "||" + dto.getReasonDescription().trim()).toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put((dto.getReasonCode().trim() + "||" + dto.getReasonDescription().trim()).toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < listReasonCode.size(); itemIndex++) {
			ReasonCodeTableDto dto = listReasonCode.get(itemIndex);
			if (dto.getReasonCode() == null || dto.getReasonDescription() == null) {
				continue;
			}
			String listPositionDup = mapCheckDup.get((dto.getReasonCode().trim() + "||" + dto.getReasonDescription().trim()).toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("reason.code.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getReasonCode().trim()), dto.getReasonCode(), dto.getReasonDescription() }));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	public void checkExistedEntries(List<ReasonCodeTableDto> listReasonCode, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId)
	{
		for (int itemIndex = 0; itemIndex < listReasonCode.size(); itemIndex++) {
			ReasonCodeTableDto reasonCodeTableDto = listReasonCode.get(itemIndex);
			if (reasonCodeTableDto.getReasonCode() == null || reasonCodeTableDto.getReasonDescription() == null){
				continue;
			}

			int existedTransporterAmount = countByCondition(
					reasonCodeTableDto.getReasonCode(), reasonCodeTableDto.getReasonDescription(), currentTenantId);
			if (existedTransporterAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.reasoncode", null));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void checkValidateFieldsOption(List<ReasonCodeTableDto> listReasonCode, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors){
		String category = "SOURCE|DESTINATION|TRANSIT|OTHERS";
		String usageLevel = "PARTIAL|FAILED|COMPLETE";

		for (int itemIndex = 0; itemIndex < listReasonCode.size(); itemIndex++){
			ReasonCodeTableDto reasonCodeTableDto = listReasonCode.get(itemIndex);

			List<String> listCategories = new ArrayList<>();
			if(reasonCodeTableDto.getCategory() != null){
				listCategories = Arrays.asList(reasonCodeTableDto.getCategory().split("\\|"));
			}

			List<String> listUsages = new ArrayList<>();
			if(reasonCodeTableDto.getUsage() != null){
				listUsages = Arrays.asList(reasonCodeTableDto.getUsage().split("\\|"));
			}

			for (int index = 0; index < listCategories.size(); index++){
				if (category.indexOf(listCategories.get(index).trim().toUpperCase()) == -1){
					StringBuilder cellErrors = mapExcelError.get(itemIndex);

					if (cellErrors == null){
						cellErrors = new StringBuilder();
					}

					excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.invalid.option", new String[] {"Category","SOURCE, DESTINATION, TRANSIT, OTHERS"}));
					excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));

					mapExcelError.put(itemIndex, cellErrors);
					break;
				}
			}

			for (int index = 0; index < listUsages.size(); index++){
				if (usageLevel.indexOf(listUsages.get(index).trim().toUpperCase()) == -1){
					StringBuilder cellErrors = mapExcelError.get(itemIndex);

					if (cellErrors == null){
						cellErrors = new StringBuilder();
					}

					excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.invalid.option", new String[] {"Usage level","PARTIAL, FAILED, COMPLETE"}));
					excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));

					mapExcelError.put(itemIndex, cellErrors);
					break;
				}
			}
		}
	}

	public void saveExcelData(List<ReasonCodeTableDto> listReasonCode, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < listReasonCode.size(); itemIndex++) {
			ReasonCodeTableDto reasonCodeTableDto = listReasonCode.get(itemIndex);
			try {
				ReasonCode reasonCode = new ReasonCode();
				BeanCopier copier = BeanCopier.create(ReasonCodeTableDto.class, ReasonCode.class, false);
				copier.copy(reasonCodeTableDto, reasonCode, null);
				save(reasonCode);
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
	public void downloadExcel(HttpServletResponse response, Long currentTenantId, ReasonCodeFilter filter){
		List<ReasonCode> dataList = findByTenantId(currentTenantId, filter);
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.REASONCODE_SETTING_EXPORT);
		excelUtils.exportExcel(response, dataList, ReasonCode.class, template);
	}

	public void validateAndSaveExcelData(Long currentTenantId, Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo) {
		List<ReasonCodeTableDto> listReasonCodeAfterReadExcel = excelUtils.parseExcelToDto(ReasonCodeTableDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);

		for (int i=0; i<listReasonCodeAfterReadExcel.size(); i++){
			listReasonCodeAfterReadExcel.get(i).setTenantId(currentTenantId);
		}

		String emailTemplateName = StringUtils.EMPTY;

		checkDuplicatedEntries(listReasonCodeAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listReasonCodeAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId);
		checkValidateFieldsOption(listReasonCodeAfterReadExcel, mapExcelError, excelEmailErrors);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listReasonCodeAfterReadExcel, mapExcelError, excelEmailErrors);

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

		excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.reasoncodes", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);
	}
}
