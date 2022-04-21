package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.TransporterDto;
import com.y3technologies.masters.dto.VehicleDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.dto.filter.VehicleFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Driver;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.QVehicle;
import com.y3technologies.masters.model.Vehicle;
import com.y3technologies.masters.repository.DriverRepository;
import com.y3technologies.masters.repository.LocationRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import com.y3technologies.masters.repository.VehicleRepository;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.ProfileScopeService;
import com.y3technologies.masters.service.TransporterService;
import com.y3technologies.masters.service.VehicleService;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

	public final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final PartnersRepository partnersRepository;
	private final VehicleRepository vehicleRepository;
	private final DriverRepository driverRepository;
	private final LocationRepository locationRepository;
	private final OperationLogService operationLogService;
	private final LookupRepository lookupRepository;
	private final ExcelClient excelClient;
	private final ExcelUtils excelUtils;
	private final MastersApplicationPropertiesConfig propertiesConfig;
	private final MessagesUtilities messagesUtilities;
	private final ProfileScopeService profileScopeService;
	private final TransporterService transporterService;
	private final ThreadPoolTaskExecutor executor;
	private final AasClient aasClient;

	@Override
	@Transactional
	public Vehicle save(VehicleDto dto) {
		Long id = dto.getId();
		Boolean updateFlag = Boolean.FALSE;

		Vehicle vehicle = new Vehicle();
		final BeanCopier copier = BeanCopier.create(VehicleDto.class, Vehicle.class, false);
		copier.copy(dto, vehicle, null);
		if (dto.getDefaultDriverId() != null) {
			Optional<Driver> driver = driverRepository.findById(dto.getDefaultDriverId());
			driver.ifPresent(value -> vehicle.setDefaultDriver(value));
		}
		if (dto.getAssetDefaultLocId() != null) {
			Optional<Location> location = locationRepository.findById(dto.getAssetDefaultLocId());
			location.ifPresent(value -> vehicle.setAssetDefaultLoc(value));
		}
		if (dto.getId() != null) {
			Vehicle oldModel = vehicleRepository.findById(id).get();
			updateFlag = oldModel.getHashcode() != vehicle.hashCode();
		}

		if(dto.getTransporterId() != null){
			Partners partners = partnersRepository.findById(dto.getTransporterId()).get();
			vehicle.setPartners(partners);
		}
		vehicle.setHashcode(vehicle.hashCode());
		Vehicle savedVehicle = vehicleRepository.save(vehicle);

		if (updateFlag) {
			operationLogService.log(id == null, vehicle, vehicle.getClass().getSimpleName(),
					vehicle.getId().toString());
		}

		return savedVehicle;
	}

	@Override
	public VehicleDto getById(Long id) {
		Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
		VehicleDto vehicleDto = null;
		if (vehicle != null) {
			vehicleDto = new VehicleDto();
			final BeanCopier copier = BeanCopier.create(Vehicle.class, VehicleDto.class, false);
			copier.copy(vehicle, vehicleDto, null);
			Location loc = vehicle.getAssetDefaultLoc();
			if (loc != null) {
				vehicleDto.setAssetDefaultLocId(loc.getId());
				vehicleDto.setLocCode(loc.getLocCode());
				vehicleDto.setLocDesc(loc.getLocDesc());
			}
			Driver driver = vehicle.getDefaultDriver();
			if (driver != null) {
				vehicleDto.setDefaultDriverId(driver.getId());
				vehicleDto.setDefaultDriverName(driver.getName());
			}
			if(vehicle.getPartners() != null) {
				vehicleDto.setTransporterId(vehicle.getPartners().getId());
			}else{
				vehicleDto.setTransporterId(null);
			}
		}
		return vehicleDto;
	}

	@Override
	public DataTablesOutput<Vehicle> query(DataTablesInput input, Long tenantUserId) {
		Specification<Vehicle> sQuery = (root, query, criteriaBuilder) -> criteriaBuilder.and();
		sQuery = profileScopeService.getQueryFromProfileScope(tenantUserId, sQuery, AppConstants.ProfileScopeSettings.VEHICLE);
		if(input.getColumn("tenantId") != null
				&& StringUtils.isNotEmpty(input.getColumn("tenantId").getSearch().getValue())
				&& input.getColumn("tenantId").getSearchable()) {
			Specification<Vehicle> specification = new Specification<Vehicle>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Predicate toPredicate(Root<Vehicle> root, CriteriaQuery<?> query,
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
		return vehicleRepository.findAll(input, sQuery);
	}

	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		Vehicle model = vehicleRepository.findById(id).get();
		if (model.getActiveInd() != status) {
			vehicleRepository.updateStatus(id, status);
			operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	public List<VehicleDto> findByTenantId(Long tenantId, Long tenantUserId, VehicleFilter filter) {
		QVehicle qVehicle = QVehicle.vehicle;
		BooleanExpression query = qVehicle.isNotNull();

		query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.VEHICLE, null);
		query = query.and(qVehicle.tenantId.eq(tenantId));

		if (!StringUtils.isBlank(filter.getVehicleRegNumber())) {
			query = query.and(qVehicle.vehicleRegNumber.like("%" + filter.getVehicleRegNumber() + "%"));
		}

		if (!StringUtils.isBlank(filter.getVehicleType())) {
			query = query.and(qVehicle.vehicleType.like("%" + filter.getVehicleType() + "%"));
		}

		if (!StringUtils.isBlank(filter.getLicenceTypeRequired())) {
			query = query.and(qVehicle.licenceTypeRequired.like("%" + filter.getLicenceTypeRequired() + "%"));
		}

		if (!Objects.isNull(filter.getLocId())) {
			query = query.and(qVehicle.assetDefaultLoc.id.eq(filter.getLocId()));
		}

		if (!Objects.isNull(filter.getPartnerId())) {
			query = query.and(qVehicle.partners.id.eq(filter.getPartnerId()));
		}

		List<Vehicle> vehicleList = vehicleRepository.findAll(query);

		if (vehicleList.isEmpty()) {
			return Collections.emptyList();
		}

		return vehicleList.stream().map(vehicle -> {
			VehicleDto vehicleDto = new VehicleDto();
			final BeanCopier copier = BeanCopier.create(Vehicle.class, VehicleDto.class, false);
			copier.copy(vehicle, vehicleDto, null);
			Location loc = vehicle.getAssetDefaultLoc();
			if (!Objects.isNull(loc)) {
				vehicleDto.setAssetDefaultLocId(loc.getId());
				vehicleDto.setLocName(loc.getLocName());
				vehicleDto.setLocCode(loc.getLocCode());
				vehicleDto.setLocDesc(loc.getLocDesc());
			}
			Driver driver = vehicle.getDefaultDriver();
			if (!Objects.isNull(driver)) {
				vehicleDto.setDefaultDriverId(driver.getId());
				vehicleDto.setDefaultDriverName(driver.getName());
			}
			if (!Objects.isNull(vehicle.getPartners())) {
				vehicleDto.setTransporterId(vehicle.getPartners().getId());
				vehicleDto.setTransporterName(vehicle.getPartners().getPartnerName());
			}
			return vehicleDto;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Vehicle> findByDefaultDriverIdAndTenantId(Long defaultDriverId, Long tenantId) {
		return vehicleRepository.findByDefaultDriverIdAndTenantId(defaultDriverId, tenantId);
	}

	@Override
	public List<Vehicle> findByFilter(VehicleFilter filter) {

		QVehicle vehicle = QVehicle.vehicle;
		BooleanExpression query = vehicle.isNotNull();

		if (filter.getTenantId() != null) {
			query = query.and(vehicle.tenantId.eq(filter.getTenantId()));
		}

		if (StringUtils.isNoneBlank(filter.getVehicleRegisterNo())) {
			String keyword = filter.getVehicleRegisterNo().trim();
			query = query.and(vehicle.vehicleRegNumber.like("%" + keyword + "%"));
		}

		if(CollectionUtils.isNotEmpty(filter.getVehicleIdList())) {
			query = query.and(vehicle.id.in(filter.getVehicleIdList()));
		}

		if (filter.getTransporterId() != null) {
			query = query.and(vehicle.partners.id.in(filter.getTransporterId()));
		}

		if(CollectionUtils.isNotEmpty(filter.getTransporterIdList())) {
			query = query.and(vehicle.partners.id.in(filter.getTransporterIdList()));
		}

		if(filter.getActiveInd() != null){
			query = query.and(vehicle.activeInd.eq(filter.getActiveInd()));
		}

		if(filter.getAssignedTransporter() != null){
			if(filter.getAssignedTransporter() == Boolean.FALSE){
				query = query.and(vehicle.partners.id.isNull());
			}else{
				query = query.and(vehicle.partners.id.isNotNull());
			}
		}

		return vehicleRepository.findAll(query);
	}

	public int countByCondition (Long tenantId, String vehicleRegNumber){
		return vehicleRepository.countByTenantIdAndVehicleRegNumber(tenantId,vehicleRegNumber);
	}

	@Override
	public ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId, Long currentTenantUserId) {
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.VEHICLE_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(VehicleDto.class, workbook, sheet, fileName
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
	public List<Long> findVehicleIdsByVehiclePlateNo(Long tenantId, String vehiclePlateNo) {
		return vehicleRepository.findVehicleIdsByVehiclePlateNo(tenantId, vehiclePlateNo);
	}

	@Override
	public VehicleDto findByVehicleNo(Long tenantId, String vehicleNo) {
		Vehicle vehicle =  vehicleRepository.findByVehicleNo(tenantId, vehicleNo);

		if (ObjectUtils.isEmpty(vehicle)) {
			return null;
		}

		VehicleDto vehicleDto = new VehicleDto();
		final BeanCopier copier = BeanCopier.create(Vehicle.class, VehicleDto.class, false);
		copier.copy(vehicle, vehicleDto, null);
		if (ObjectUtils.isNotEmpty(vehicle.getPartners())) {
			vehicleDto.setTransporterId(vehicle.getPartners().getId());
		}
		return vehicleDto;
	}

	public void checkDuplicatedEntries(List<VehicleDto> listVehicleDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (VehicleDto dto : listVehicleDto) {
			if (StringUtils.isEmpty(dto.getVehicleRegNumber())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getVehicleRegNumber().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getVehicleRegNumber().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < listVehicleDto.size(); itemIndex++) {
			VehicleDto dto = listVehicleDto.get(itemIndex);
			if (StringUtils.isEmpty(dto.getVehicleRegNumber())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getVehicleRegNumber().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("vehicle.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getVehicleRegNumber().trim().toLowerCase()), dto.getVehicleRegNumber()}));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	public void checkExistedEntries(List<VehicleDto> listVehicleDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < listVehicleDto.size(); itemIndex++) {
			VehicleDto vehicleDto = listVehicleDto.get(itemIndex);
			if (vehicleDto.getTenantId() == null || vehicleDto.getVehicleRegNumber() == null){
				continue;
			}

			int existedTransporterAmount = countByCondition(vehicleDto.getTenantId(), vehicleDto.getVehicleRegNumber());
			if (existedTransporterAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Vehicle Reg Number"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	public void checkExcelError(List<VehicleDto> listVehicleDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId, Long currentTenantUserId, String token){
		Page<TransporterDto> validTransporters = transporterService.findBySearch(new PartnersFilter(currentTenantId, 0, Integer.MAX_VALUE), currentTenantUserId, token);
		Map<String, Boolean> transporterNameMap = validTransporters.getContent().stream().collect(Collectors.toMap(TransporterDto::getPartnerName, transporter -> true));
		List<String> listDriverName = new ArrayList<>();
		List<String> listLocName = new ArrayList<>();
		List<String> listTransporterName = new ArrayList<>();
		List<String> listVehicleType = new ArrayList<>();
		List<String> listLicenceType = new ArrayList<>();
		listVehicleDto.stream().forEach(vehicleDto -> {
			if (vehicleDto.getDefaultDriverName()!=null){
				listDriverName.add(vehicleDto.getDefaultDriverName().toLowerCase().trim());
			}

			if (vehicleDto.getLocName()!=null){
				listLocName.add(vehicleDto.getLocName().toLowerCase().trim());
			}

			if (vehicleDto.getTransporterName()!=null){
				listTransporterName.add(vehicleDto.getTransporterName().toLowerCase().trim());
			}

			if (vehicleDto.getVehicleType()!=null){
				listVehicleType.add(vehicleDto.getVehicleType().toLowerCase().trim());
			}

			if (vehicleDto.getLicenceTypeRequired()!=null && !vehicleDto.getLicenceTypeRequired().isEmpty()){
				String[] licenceTypeArr = vehicleDto.getLicenceTypeRequired().split("\\|");
				Arrays.stream(licenceTypeArr).forEach(licenceType -> {
					listLicenceType.add(licenceType.toLowerCase().trim());
				});
			}
		});

		Map<String,Driver> driverByDriverName = new HashMap();
		if (!listDriverName.isEmpty()){
			List<Driver> listDriver = driverRepository.findByListName(listDriverName, currentTenantId);
			driverByDriverName = listDriver.stream().filter(driver -> !StringUtils.isEmpty(driver.getName())).collect(Collectors.toMap(driver ->
					driver.getName().toLowerCase().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		Map<String, Location> locationByLocName = new HashMap();
		if (!listLocName.isEmpty()){
			List<Location> listLocation = locationRepository.findByListLocName(currentTenantId, listLocName);
			locationByLocName = listLocation.stream().filter(location -> !StringUtils.isEmpty(location.getLocName())).collect(Collectors.toMap(location ->
					location.getLocName().toLowerCase().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		Map<String, Partners> transporterByName = new HashMap();
		if (!listTransporterName.isEmpty()){
			List<Partners> listPartner = partnersRepository.findByListPartnerNameAndLookupCode(listTransporterName, AppConstants.PartnerType.TRANSPORTER, currentTenantId);
			transporterByName = listPartner.stream().filter(partners -> !StringUtils.isEmpty(partners.getPartnerName())).collect(Collectors.toMap(partners ->
					partners.getPartnerName().toLowerCase().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		Map<String, Lookup> vehicleTypeByName = new HashMap();
		if (!listVehicleType.isEmpty()){
			List<Lookup> listLookup = lookupRepository.findByLookupTypeAndListDescription(0l,"vehicleType",listVehicleType);
			vehicleTypeByName = listLookup.stream().filter(lookup -> !StringUtils.isEmpty(lookup.getLookupDescription())).collect(Collectors.toMap(lookup ->
					lookup.getLookupDescription().toLowerCase().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		Map<String, Lookup> licenseTypeByName = new HashMap();
		if (!listLicenceType.isEmpty()){
			List<Lookup> listLicenseType = lookupRepository.findByListDescriptionAndTenantId(listLicenceType, currentTenantId, "VehicleLicenceClass");
			licenseTypeByName = listLicenseType.stream().filter(lookup -> !StringUtils.isEmpty(lookup.getLookupDescription())).collect(Collectors.toMap(lookup ->
					lookup.getLookupDescription().toLowerCase().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		for (int itemIndex = 0; itemIndex < listVehicleDto.size(); itemIndex++){
			VehicleDto vehicleDto = listVehicleDto.get(itemIndex);

			if (!StringUtils.isEmpty(vehicleDto.getDefaultDriverName())){
				Driver existedDriver = driverByDriverName.get(vehicleDto.getDefaultDriverName().toLowerCase().trim());
				if (existedDriver == null){
					buildExcelError(mapExcelError,excelEmailErrors, itemIndex
							, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.found", new String[]{"Driver"})
							,messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				} else {
					vehicleDto.setDefaultDriverId(existedDriver.getId());
				}
			}

			if (!StringUtils.isEmpty(vehicleDto.getLocName())){
				Location existedLocation = locationByLocName.get(vehicleDto.getLocName().toLowerCase().trim());
				if (existedLocation == null){
					buildExcelError(mapExcelError,excelEmailErrors, itemIndex
							, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.found", new String[]{"Location"})
							, messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				} else {
					vehicleDto.setAssetDefaultLocId(existedLocation.getId());
				}
			}

			if (!StringUtils.isEmpty(vehicleDto.getTransporterName())){
				Partners existedTransporter = transporterByName.get(vehicleDto.getTransporterName().toLowerCase().trim());
				if (existedTransporter == null){
					buildExcelError(mapExcelError,excelEmailErrors, itemIndex
							, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.found", new String[]{"Transporter"})
							, messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				} else {
					vehicleDto.setTransporterId(existedTransporter.getId());
				}
			}

			if (!StringUtils.isEmpty(vehicleDto.getVehicleType())){
				Lookup existedVehicleType = vehicleTypeByName.get(vehicleDto.getVehicleType().toLowerCase().trim());
				if (existedVehicleType == null){
					buildExcelError(mapExcelError,excelEmailErrors, itemIndex
							, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.found", new String[]{"Vehicle type"})
							, messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				}
			}

			if (!StringUtils.isEmpty(vehicleDto.getLicenceTypeRequired())){
				String[] licenceTypeArr = vehicleDto.getLicenceTypeRequired().split("\\|");
				Arrays.sort(licenceTypeArr);
				checkDuplicatedLicenceTypeRequired(itemIndex, licenceTypeArr, mapExcelError, excelEmailErrors);

				for (String licenseType : licenceTypeArr) {
					Lookup existedLicenseType = licenseTypeByName.get(licenseType.toLowerCase().trim());
					if (existedLicenseType == null){
						buildExcelError(mapExcelError,excelEmailErrors, itemIndex
								, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.found", new String[]{"License type"})
								, messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
					}
				}
			}

			if (Objects.isNull(transporterNameMap.get(vehicleDto.getTransporterName()))) {
				buildExcelError(mapExcelError,excelEmailErrors, itemIndex
						, messagesUtilities.getMessageWithParam("upload.excel.error.data.no.access", new String[]{"Driver", "transporter"})
						, messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
			}
		}
	}

	public void saveExcelData(List<VehicleDto> listVehicleDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		for (int itemIndex = 0; itemIndex < listVehicleDto.size(); itemIndex++){
			try {
				Vehicle vehicle = save(listVehicleDto.get(itemIndex));
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

	private void checkDuplicatedLicenceTypeRequired(int itemIndex, String[] listLicenceType, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors){
		for (int i=0; i<listLicenceType.length - 1; i++){
			if (listLicenceType[i].trim().equals(listLicenceType[i+1].trim())){
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.duplicated", new String[]{"License type"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				mapExcelError.put(itemIndex, cellErrors);

				break;
			}
		}
	}

	private void buildExcelError(Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, int itemIndex, String excelError, String emailError){
		StringBuilder cellErrors = mapExcelError.get(itemIndex);

		if (cellErrors == null) {
			cellErrors = new StringBuilder();
		}

		excelUtils.buildCellErrors(cellErrors, excelError);
		excelEmailErrors.add(emailError);
		mapExcelError.put(itemIndex, cellErrors);
	}

	public void validateAndSaveExcelData(Long currentTenantId, Long currentTenantUserId, Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo, String token) {
		List<VehicleDto> listVehicleAfterReadExcel = excelUtils.parseExcelToDto(VehicleDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		//need pass tenant Id here because it's one of conditions to check if existed in database
		for (int i=0; i<listVehicleAfterReadExcel.size(); i++){
			listVehicleAfterReadExcel.get(i).setTenantId(currentTenantId);
		}

		String emailTemplateName = StringUtils.EMPTY;

		checkDuplicatedEntries(listVehicleAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listVehicleAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExcelError(listVehicleAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId, currentTenantUserId, token);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listVehicleAfterReadExcel, mapExcelError, excelEmailErrors);

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

		excelUtils.sendNotificationEmail(emailTemplateName,  messagesUtilities.getMessageWithParam("menu.label.vehicles", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);
	}
}
