package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.EquipmentFilter;
import com.y3technologies.masters.dto.table.EquipmentTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Equipment;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.QEquipment;
import com.y3technologies.masters.repository.EquipmentRepository;
import com.y3technologies.masters.service.EquipmentService;
import com.y3technologies.masters.service.LookupService;
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
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EquipmentServiceImpl implements EquipmentService {

	@Autowired
	private EquipmentRepository equipmentRepository;
	
	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private MastersApplicationPropertiesConfig propertiesConfig;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private MessagesUtilities messagesUtilities;

	@Autowired
	private LookupService lookupService;

	@Autowired
	private ThreadPoolTaskExecutor executor;

	@Autowired
	private AasClient aasClient;

	@Override
	@Transactional
	public void save(Equipment model) {
		Long id = model.getId();
		Boolean updateFlag = Boolean.FALSE;
		if(model.getId() != null) {
			Equipment oldModel = getById(model.getId());
			updateFlag = oldModel.getHashcode() != model.hashCode();
		}
		model.setHashcode(model.hashCode());
		equipmentRepository.save(model);
		if(updateFlag) {
			operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
		}
	}
	
	@Override
	public boolean existsById(Long id) {
		return equipmentRepository.existsById(id);
	}
	
	@Override
	public List<Equipment> listByParam(Equipment equipment) {
		EntitySpecificationBuilder<Equipment> es = new EntitySpecificationBuilder<Equipment>();
    	es.with("tenantId", ":", equipment.getTenantId(), "and");
    	List<Equipment> list = equipmentRepository.findAll(es.build());
		return list;
	}

	@Override
	public DataTablesOutput<Equipment> query(DataTablesInput input) {
		if(input.getColumn("tenantId") != null
				&& StringUtils.isNotEmpty(input.getColumn("tenantId").getSearch().getValue())
				&& input.getColumn("tenantId").getSearchable()) {
			Specification<Equipment> sTenantId = new Specification<Equipment>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Predicate toPredicate(Root<Equipment> root, CriteriaQuery<?> query,
											 CriteriaBuilder criteriaBuilder) {
					return criteriaBuilder.equal(root.get("tenantId"), input.getColumn("tenantId").getSearch().getValue());
				}
			};
			input.getColumn("tenantId").setSearchable(false);

			return equipmentRepository.findAll(input, sTenantId);
		}
		return equipmentRepository.findAll(input);
	}

	@Override
	public Equipment getById(Long id) {
		return equipmentRepository.findById(id).get();
	}
	
	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		Equipment model = getById(id);
		if(model.getActiveInd() != status) {
			equipmentRepository.updateStatus(id, status);
			operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	@Override
	public List<Equipment> findByTenantId(Long tenantId, EquipmentFilter filter) {
		QEquipment qEquipment = QEquipment.equipment;
		BooleanExpression query = qEquipment.isNotNull();
		query = query.and(qEquipment.tenantId.eq(tenantId));

		if (!StringUtils.isBlank(filter.getUnitAidc1())) {
			query = query.and(qEquipment.unitAidc1.like("%" + filter.getUnitAidc1() + "%"));
		}

		if (!StringUtils.isBlank(filter.getUnitAidc2())) {
			query = query.and(qEquipment.unitAidc2.like("%" + filter.getUnitAidc2() + "%"));
		}

		if (!StringUtils.isBlank(filter.getEquipmentType())) {
			query = query.and(qEquipment.equipmentType.like("%" + filter.getEquipmentType() + "%"));
		}

		if (!StringUtils.isBlank(filter.getUnitType())) {
			query = query.and(qEquipment.unitType.like("%" + filter.getUnitType() + "%"));
		}

		return equipmentRepository.findAll(query);
	}

	@Override
	public int countByUnitAidc1(String unitAidc1, Long currentTenantId) {
		return equipmentRepository.countByTenantIdAndUnitAidc1(currentTenantId,unitAidc1);
	}

	@Override
	public ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId) {
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.EQUIPMENT_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(EquipmentTableDto.class, workbook, sheet, fileName
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

	public void checkDuplicatedEntries(List<EquipmentTableDto> equipmentTableDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (EquipmentTableDto dto : equipmentTableDtoList) {
			if (StringUtils.isEmpty(dto.getUnitAidc1())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getUnitAidc1().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getUnitAidc1().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < equipmentTableDtoList.size(); itemIndex++) {
			EquipmentTableDto dto = equipmentTableDtoList.get(itemIndex);
			if (StringUtils.isEmpty(dto.getUnitAidc1())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getUnitAidc1().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("equipment.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getUnitAidc1().trim().toLowerCase()), dto.getUnitAidc1()}));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	private void checkExistedEntries(List<EquipmentTableDto> equipmentList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId) {
		for (int itemIndex = 0; itemIndex < equipmentList.size(); itemIndex++) {
			EquipmentTableDto equipmentTableDto = equipmentList.get(itemIndex);
			if (StringUtils.isEmpty(equipmentTableDto.getUnitAidc1())) {
				continue;
			}

			int existedEquipmentAmount = countByUnitAidc1(equipmentTableDto.getUnitAidc1(), currentTenantId);
			if (existedEquipmentAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Equipment Name"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	private void checkLookupListValid(List<EquipmentTableDto> equipmentList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId) {
		List<Lookup> listEquipmentType = lookupService.findByLookupTypeTenantId(AppConstants.LookupType.EQUIPMENT_TYPE, currentTenantId);
		List<Lookup> listEquipmentUnitType = lookupService.findByLookupTypeTenantId(AppConstants.LookupType.EQUIPMENT_UNIT_TYPE, currentTenantId);
		Map<String, Lookup> mapLookupTypeByLookup = new HashMap<>();
		listEquipmentType.stream().forEach(lookup -> {
			if (!StringUtils.isEmpty(lookup.getLookupDescription())){
				mapLookupTypeByLookup.put(lookup.getLookupDescription().trim().toLowerCase(), lookup);
			}
		});
		for (int itemIndex = 0; itemIndex < equipmentList.size(); itemIndex++) {
			EquipmentTableDto equipmentTableDto = equipmentList.get(itemIndex);
			if (equipmentTableDto.getEquipmentType() == null || mapLookupTypeByLookup.get(equipmentTableDto.getEquipmentType().trim().toLowerCase()) == null) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}
				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.invalid", new String[]{"Equipment Type"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);
			}
			if (equipmentTableDto.getEquipmentType() != null && mapLookupTypeByLookup.get(equipmentTableDto.getEquipmentType().trim().toLowerCase()) != null) {
				equipmentTableDto.setEquipmentType(mapLookupTypeByLookup.get(equipmentTableDto.getEquipmentType().trim().toLowerCase()).getLookupCode());
			}
		}
		Map<String, Lookup> mapLookupUnitTypeByLookup = new HashMap<>();
		listEquipmentUnitType.stream().forEach(lookup -> {
			if (!StringUtils.isEmpty(lookup.getLookupDescription())){
				mapLookupUnitTypeByLookup.put(lookup.getLookupDescription().trim().toLowerCase(), lookup);
			}
		});
		for (int itemIndex = 0; itemIndex < equipmentList.size(); itemIndex++) {
			EquipmentTableDto equipmentTableDto = equipmentList.get(itemIndex);
			if (equipmentTableDto.getUnitType() == null || mapLookupUnitTypeByLookup.get(equipmentTableDto.getUnitType().trim().toLowerCase()) == null){
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors,  messagesUtilities.getMessageWithParam("upload.excel.error.data.invalid", new String[]{"Equipment Unit Type"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
			if (equipmentTableDto.getUnitType() != null && mapLookupTypeByLookup.get(equipmentTableDto.getUnitType().trim().toLowerCase()) != null) {
				equipmentTableDto.setUnitType(mapLookupUnitTypeByLookup.get(equipmentTableDto.getUnitType().trim().toLowerCase()).getLookupCode());
			}
		}
	}

	private void saveExcelData(List<EquipmentTableDto> equipmentList, Map<Integer, StringBuilder> mapExcelError, Long currentTenantId, Set<String> excelEmailErrors) {
		for (int itemIndex = 0; itemIndex < equipmentList.size(); itemIndex++) {
			EquipmentTableDto equipmentTableDto = equipmentList.get(itemIndex);
			equipmentTableDto.setTenantId(currentTenantId);

			try {
				Equipment equipment = new Equipment();
				BeanCopier copier = BeanCopier.create(EquipmentTableDto.class, Equipment.class, false);
				copier.copy(equipmentTableDto, equipment, null);
				save(equipment);
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

	public void validateAndSaveExcelData(Long currentTenantId, Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo) {
		List<EquipmentTableDto> listEquipmentAfterReadExcel = excelUtils.parseExcelToDto(EquipmentTableDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		String emailTemplateName = StringUtils.EMPTY;

		checkDuplicatedEntries(listEquipmentAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listEquipmentAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId);
		checkLookupListValid(listEquipmentAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listEquipmentAfterReadExcel, mapExcelError, currentTenantId, excelEmailErrors);

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

		excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.equipment", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);
	}
}
