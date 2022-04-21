package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.ConfigCodeDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.ConfigCodeFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.ConfigCode;
import com.y3technologies.masters.model.OperationLog;
import com.y3technologies.masters.model.QConfigCode;
import com.y3technologies.masters.repository.ConfigCodeRepository;
import com.y3technologies.masters.service.ConfigCodeService;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.EntitySpecificationBuilder;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ConfigCodeServiceImpl implements ConfigCodeService {

	@Autowired
	MessagesUtilities messagesUtilities;

	@Autowired
	private ConfigCodeRepository configCodeRepository;

	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private ExcelClient excelClient;

	@Autowired
	private MastersApplicationPropertiesConfig propertiesConfig;

	@Autowired
	private ThreadPoolTaskExecutor executor;

	@Autowired
	private AasClient aasClient;

	@Override
	@Transactional
	public ConfigCode save(ConfigCode model) {
		Long id = model.getId();
		boolean updateFlag = Boolean.TRUE;
		if(model.getId() != null) {
			ConfigCode oldModel = getById(model.getId()).get();
			updateFlag = oldModel.hashCode() != model.hashCode();
		}
		model.setHashcode(model.hashCode());
		ConfigCode savedConfigCode = configCodeRepository.save(model);
		if(updateFlag) {
			operationLogService.log(id == null, savedConfigCode, savedConfigCode.getClass().getSimpleName(), savedConfigCode.getId().toString());
		}
		return savedConfigCode;
	}

	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		Optional<ConfigCode> model = getById(id);
		if(model.get().getActiveInd() != status) {
			configCodeRepository.updateStatus(id, status);
			operationLogService.log(OperationLog.Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	@Override
	public Optional<ConfigCode> getById(Long id) {
		return configCodeRepository.findById(id);
	}

	@Override
	public boolean existsById(Long id) {
		return configCodeRepository.existsById(id);
	}
	
	@Override
	public List<ConfigCode> findAll() {
		EntitySpecificationBuilder<ConfigCode> es = new EntitySpecificationBuilder<ConfigCode>();
		es.with("activeInd", ":", true, "and");
		List<ConfigCode> list = configCodeRepository.findAll(es.build());
		return list;
	}

	@Override
	public List<ConfigCode> findByUsageLevel(String usageLevel) {
		List<ConfigCode> list = configCodeRepository.findByUsageLevel(usageLevel);
		return list;
	}

	@Override
	public void delete(ConfigCode configCode) {
		configCodeRepository.delete(configCode);
	}

    @Override
    public Page<ConfigCode> findAllConfigCodeWithPagination(Pageable pageable, Map<String, String> map) {
		return configCodeRepository.findAllConfigCodeWithPagination(pageable, map);
    }

	@Override
	public ConfigCodeDto getByCode(String code) {
		ConfigCode configCode = configCodeRepository.findByCode(code).orElse(null);
		if(null != configCode) return mapModelToDto(configCode);
		return null;
	}

	@Override
	public ConfigCodeDto updateTptRequestSeqGenConfigValue(String code, String incrementBy) {
		ConfigCode configCode = configCodeRepository.findByCode(code).orElseThrow(() -> new TransactionException("exception.configCode.invalid"));
		int newConfigValue = Integer.parseInt(configCode.getConfigValue()) + 1;
		configCode.setConfigValue(String.valueOf(newConfigValue));
		configCodeRepository.save(configCode);
		return mapModelToDto(configCode);		
	}
	
	private ConfigCodeDto mapModelToDto(ConfigCode configCode) {
		ConfigCodeDto dto = new ConfigCodeDto();
		dto.setId(configCode.getId());
		dto.setConfigValue(configCode.getConfigValue());
		dto.setDescription(configCode.getDescription());
		dto.setUsageLevel(configCode.getUsageLevel());
		dto.setCode(configCode.getCode());
		return dto;
	}

	@Override
	public List<ConfigCode> getAll() {
		return configCodeRepository.findAll();
	}

	@Override
	public int countByCondition(String configCode){
		return configCodeRepository.countByCode(configCode);
	}

	public ExcelResponseMessage uploadFile(MultipartFile file){
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(ConfigCodeDto.class, workbook, sheet, fileName
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
				validateAndSaveExcelData(workbook, sheet, fileName, mapExcelError, excelEmailErrors, uploadTemplateHdrIdDto, timeStartReadingFile, userInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return excelResponseMessage;
	}

	public void downloadExcel(HttpServletResponse response, ConfigCodeFilter filter) {
		QConfigCode configCode = QConfigCode.configCode;
		BooleanExpression query = configCode.isNotNull();

		if (StringUtils.isNotBlank(filter.getCode())) {
			String keyword = filter.getCode().trim();
			query = query.and(configCode.code.like("%" + keyword + "%"));
		}

		if (StringUtils.isNotBlank(filter.getUsageLevel())) {
			String keyword = filter.getUsageLevel().trim();
			query = query.and(configCode.usageLevel.like("%" + keyword + "%"));
		}

		if (StringUtils.isNotBlank(filter.getConfigValue())) {
			String keyword = filter.getConfigValue().trim();
			query = query.and(configCode.configValue.like("%" + keyword + "%"));
		}

		List<ConfigCode> dataList = configCodeRepository.findAll(query, Sort.by(filter.getSort()));
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_EXPORT);
		excelUtils.exportExcel(response, dataList, ConfigCode.class, template);
	}

	public void checkDuplicatedEntries(List<ConfigCodeDto> configCodeList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (ConfigCodeDto dto : configCodeList) {
			if (StringUtils.isEmpty(dto.getCode())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getCode().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getCode().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < configCodeList.size(); itemIndex++) {
			ConfigCodeDto dto = configCodeList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getCode())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getCode().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("config.code.excel.duplicate.line",
					new String[] { mapCheckDup.get(dto.getCode().trim().toLowerCase()), dto.getCode() }));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	public void checkExistedEntries(List<ConfigCodeDto> configCodeList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < configCodeList.size(); itemIndex++) {
			ConfigCodeDto dto = configCodeList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getCode())) {
				continue;
			}

			int existedConfigCodeAmount = countByCondition(dto.getCode());
			if (existedConfigCodeAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Config Code"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries",null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void checkValidUsageLevel (List<ConfigCodeDto> configCodeList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors){
		for (int itemIndex = 0; itemIndex < configCodeList.size(); itemIndex++) {
			ConfigCodeDto configCodeDto = configCodeList.get(itemIndex);
			if (!StringUtils.isEmpty(configCodeDto.getUsageLevel()) && !AppConstants.usageLevel.mapUsageLevel.containsKey(configCodeDto.getUsageLevel().trim().toUpperCase())){
				StringBuilder cellErrors = mapExcelError.get(itemIndex);
				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}
				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.invalid", new String[]{"Usage Level"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries",null));
				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void saveExcelData(List<ConfigCodeDto> configCodeList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < configCodeList.size(); itemIndex++) {
			ConfigCodeDto configCodeDto = configCodeList.get(itemIndex);
			try {
				ConfigCode configCode = new ConfigCode();
				BeanCopier copier = BeanCopier.create(ConfigCodeDto.class, ConfigCode.class, false);
				copier.copy(configCodeDto, configCode, null);
				configCodeRepository.save(configCode);
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

	public void validateAndSaveExcelData(Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo) {
		List<ConfigCodeDto> listConfigCodeAfterReadExcel = excelUtils.parseExcelToDto(ConfigCodeDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		String emailTemplateName = StringUtils.EMPTY;

		UploadTemplateHdrIdDto test = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_UPLOAD);


		checkDuplicatedEntries(listConfigCodeAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listConfigCodeAfterReadExcel, mapExcelError, excelEmailErrors);
		checkValidUsageLevel(listConfigCodeAfterReadExcel, mapExcelError, excelEmailErrors);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listConfigCodeAfterReadExcel, mapExcelError, excelEmailErrors);

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

		excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.configuration.settings", null), excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);

	}
}