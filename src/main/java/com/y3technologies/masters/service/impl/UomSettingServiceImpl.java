package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.UomSettingDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.UomSettingFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.QUomSetting;
import com.y3technologies.masters.model.UomSetting;
import com.y3technologies.masters.repository.UomSettingRepository;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.UomSettingService;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UomSettingServiceImpl implements UomSettingService {

	@Autowired
	private UomSettingRepository uomSettingRepository;
	
	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	MessagesUtilities messagesUtilities;

	@Autowired
	LookupServiceImpl lookupService;

	@Autowired
	private MastersApplicationPropertiesConfig propertiesConfig;

	@Autowired
	private ThreadPoolTaskExecutor executor;

	@Autowired
	private AasClient aasClient;

	@Override
	@Transactional
	public void save(UomSetting model) {
		Long id = model.getId();
		Boolean updateFlag = Boolean.TRUE;
		if(model.getId() != null) {
			UomSetting oldModel = uomSettingRepository.findById(id).get();
			updateFlag = oldModel.getHashcode() != model.hashCode();
		}
		model.setHashcode(model.hashCode());
		uomSettingRepository.save(model);
		if(updateFlag) {
			operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
		}
	}

	@Override
	public UomSettingDto getById(Long id) {
		UomSetting uomSetting = uomSettingRepository.findById(id).get();
		UomSettingDto dto = null;
		if(uomSetting != null) {
			dto = new UomSettingDto();
			final BeanCopier copier = BeanCopier.create(UomSetting.class, UomSettingDto.class, false);
			copier.copy(uomSetting, dto, null);
			
			List<String> uomGroupList = new ArrayList<>();  //to set group list
			uomGroupList.add("GP1");
			uomGroupList.add("GP2");
			uomGroupList.add("GP3");
			
			dto.setUomGroupList(uomGroupList);
		}
		return dto;
	}

	@Override
	public List<UomSettingDto> getAllActiveUOM(String uomGroup) {
		List<UomSetting> uomSettingList = new ArrayList<>();
		if (StringUtils.isEmpty(uomGroup)){
			uomSettingList = uomSettingRepository.findAllActiveUOM();
		} else{
			uomSettingList = uomSettingRepository.findAllByUomGroup(uomGroup);
		}
		List<UomSettingDto> uomSettingDtoList = new ArrayList<>();

		if(null != uomSettingList && uomSettingList.size() > 0) {
			uomSettingList.forEach(u -> {
				UomSettingDto dto = new UomSettingDto();
				final BeanCopier copier = BeanCopier.create(UomSetting.class, UomSettingDto.class, false);
				copier.copy(u, dto, null);
				uomSettingDtoList.add(dto);
			});
		}
		return uomSettingDtoList;
	}

	@Override
	public DataTablesOutput<UomSetting> query(DataTablesInput input) {
		return uomSettingRepository.findAll(input);
	}

	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		UomSetting model = uomSettingRepository.findById(id).get();
		if(model.getActiveInd() != status) {
			uomSettingRepository.updateStatus(id, status);
			operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	@Transactional
	public UomSettingDto getByUomCode(String uomCode) {
		UomSetting search = new UomSetting();
		search.setUom(uomCode);
		search.setActiveInd(true);
		UomSetting model = uomSettingRepository.findOne(Example.of(search)).orElse(null);

		if (model != null) {
			UomSettingDto dto = new UomSettingDto();
			final BeanCopier copier = BeanCopier.create(UomSetting.class, UomSettingDto.class, false);
			copier.copy(model, dto, null);
			return dto;
		}
		return null;
	}

	@Transactional
	public List<UomSettingDto> getByUomGroup(String uomGroup) {
		List<UomSetting> list = uomSettingRepository.findAllByUomGroup(uomGroup);

		List<UomSettingDto> uomSettingDtoList = new ArrayList<>();

		list.forEach(u -> {
			UomSettingDto dto = new UomSettingDto();
			final BeanCopier copier = BeanCopier.create(UomSetting.class, UomSettingDto.class, false);
			copier.copy(u, dto, null);
			uomSettingDtoList.add(dto);
		});
		return uomSettingDtoList;
	}

	@Override
	public List<UomSetting> findByFilter(UomSettingFilter filter) {

		QUomSetting uomSetting = QUomSetting.uomSetting;
		BooleanExpression query = uomSetting.isNotNull();

		if(CollectionUtils.isNotEmpty(filter.getUomSettingIdList())) {
			query = query.and(uomSetting.id.in(filter.getUomSettingIdList()));
		}

		if(filter.getActiveInd() != null){
			query = query.and(uomSetting.activeInd.eq(filter.getActiveInd()));
		}

		return uomSettingRepository.findAll(query);
	}

	@Override
	public List<UomSetting> getAll(UomSettingFilter filter) {
		QUomSetting qUomSetting = QUomSetting.uomSetting;
		BooleanExpression query = qUomSetting.isNotNull();

		if (!StringUtils.isBlank(filter.getUom())) {
			query = query.and(qUomSetting.uom.like("%" + filter.getUom() + "%"));
		}

		if (!StringUtils.isBlank(filter.getDescription())) {
			query = query.and(qUomSetting.description.like("%" + filter.getDescription() + "%"));
		}

		if (!StringUtils.isBlank(filter.getUomGroup())) {
			query = query.and(qUomSetting.uomGroup.like("%" + filter.getUomGroup() + "%"));
		}

		return uomSettingRepository.findAll(query);
	}

	@Override
	public List<UomSettingDto> getUomSettingDtoByUom(String uom) {
		UomSettingFilter filter = new UomSettingFilter();
		filter.setUom(uom);
		List<UomSetting> list = getAll(filter);
		List<UomSettingDto> uomSettingDtoList = new ArrayList<>();
		final BeanCopier copier = BeanCopier.create(UomSetting.class, UomSettingDto.class, false);
		list.forEach(u -> {
			UomSettingDto dto = new UomSettingDto();
			copier.copy(u, dto, null);
			uomSettingDtoList.add(dto);
		});
		return uomSettingDtoList;
	}

	public int countByCondition (String uom, String uomGroup){
		return uomSettingRepository.countByCondition(uom, uomGroup);
	}

	@Override
	public List<UomSetting> findByUomAndUomGroup(String uom, String uomGroup) {
		return uomSettingRepository.findByUomAndUomGroup(uom, uomGroup);
	}

	public void checkDuplicatedEntries(List<UomSettingDto> uomSettingDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (UomSettingDto dto : uomSettingDtoList) {
			if (StringUtils.isEmpty(dto.getUom()) || StringUtils.isEmpty(dto.getUomGroup())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getUom().trim().toLowerCase() + "||" + dto.getUomGroup().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getUom().trim().toLowerCase() + "||" + dto.getUomGroup().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < uomSettingDtoList.size(); itemIndex++) {
			UomSettingDto dto = uomSettingDtoList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getUom()) || StringUtils.isEmpty(dto.getUomGroup())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getUom().trim().toLowerCase() + "||" + dto.getUomGroup().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("uom.setting.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getUom().trim().toLowerCase() + "||" + dto.getUomGroup().trim().toLowerCase()), dto.getUom(), dto.getUomGroup() }));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	public void checkExistedEntries(List<UomSettingDto> uomSettingList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < uomSettingList.size(); itemIndex++) {
			UomSettingDto dto = uomSettingList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getUom()) || StringUtils.isEmpty(dto.getUomGroup())){
				continue;
			}

			int existedUomAmount = countByCondition(dto.getUom(),dto.getUomGroup());
			if (existedUomAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Uom"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void checkValidUomGroup(List<UomSettingDto> uomSettingList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors){
		List<Lookup> lookupList = lookupService.findByLookupType(AppConstants.LookupType.UOM_GROUP);
		for (int itemIndex = 0; itemIndex < uomSettingList.size(); itemIndex++) {
			UomSettingDto uomSettingDto = uomSettingList.get(itemIndex);
			boolean isValidUomGroup = false;
			for (Lookup lookup: lookupList){
				if (StringUtils.isNoneEmpty(uomSettingDto.getUomGroup())&&uomSettingDto.getUomGroup().equals(lookup.getLookupDescription().trim())){
					isValidUomGroup = true;
					break;
				}
			}
			if (!isValidUomGroup){
				StringBuilder cellErrors = mapExcelError.get(itemIndex);
				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}
				excelUtils.buildCellErrors(cellErrors,messagesUtilities.getMessageWithParam("upload.excel.error.data.invalid", new String[]{"Uom Group"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void saveExcelData(List<UomSettingDto> uomSettingList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		for (int itemIndex = 0; itemIndex < uomSettingList.size(); itemIndex++) {
			UomSettingDto uomSettingDto = uomSettingList.get(itemIndex);
			try {
				UomSetting uomSetting = new UomSetting();
				BeanCopier copier = BeanCopier.create(UomSettingDto.class, UomSetting.class, false);
				copier.copy(uomSettingDto, uomSetting, null);
				save(uomSetting);

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

	public ExcelResponseMessage uploadExcel(MultipartFile file) {
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.UOM_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(UomSettingDto.class, workbook, sheet, fileName
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

	public void validateAndSaveExcelData(Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo){
		List<UomSettingDto> listUomSettingAfterReadExcel = excelUtils.parseExcelToDto(UomSettingDto.class,  sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		String emailTemplateName = StringUtils.EMPTY;

		checkDuplicatedEntries(listUomSettingAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listUomSettingAfterReadExcel, mapExcelError, excelEmailErrors);
		checkValidUomGroup(listUomSettingAfterReadExcel, mapExcelError, excelEmailErrors);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listUomSettingAfterReadExcel, mapExcelError, excelEmailErrors);

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

		excelUtils.sendNotificationEmail(emailTemplateName,  messagesUtilities.getMessageWithParam("menu.label.uom", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);
	}
}
