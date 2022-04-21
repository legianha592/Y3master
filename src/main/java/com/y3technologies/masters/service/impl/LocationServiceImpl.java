package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.AddrClient;
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CreateLocationDto;
import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.TenantUserProfileScopeDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CityDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.comm.StateDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.AddrContactFilter;
import com.y3technologies.masters.dto.filter.AddressFilter;
import com.y3technologies.masters.dto.filter.CommonTagFilter;
import com.y3technologies.masters.dto.filter.LocationFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.helper.LoggedUserInfo;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.QLocation;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.CommonTagRepository;
import com.y3technologies.masters.repository.LocationRepository;
import com.y3technologies.masters.service.CommonTagService;
import com.y3technologies.masters.service.LocationService;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.ProfileScopeService;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import com.y3technologies.masters.util.SortingUtils;
import com.y3technologies.masters.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LocationServiceImpl extends BaseServiceImpl implements LocationService {

	public final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private AasClient aasClient;

	@Autowired
	private AddrClient addrClient;

	@Autowired
	private AddrContactClient addrContactClient;

	@Autowired
	private ModelMapper modelMapper;

	private final LoggedUserInfo loggedUserInfo;

	@Autowired
	private ExcelClient excelClient;

	@Autowired
	MessagesUtilities messagesUtilities;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private CommonTagRepository commonTagRepository;

	@Autowired
	private MastersApplicationPropertiesConfig propertiesConfig;

	@Autowired
	private CommonTagService commonTagService;

	@Autowired
	private ProfileScopeService profileScopeService;

	@Autowired
	private ThreadPoolTaskExecutor executor;


	public void save(Location model) {
		Long id = model.getId();
		Boolean updateFlag = Boolean.TRUE;

		if (model.getId() != null) {
			Location oldModel = findById(model.getId());
			updateFlag = null != oldModel.getHashcode() ? oldModel.getHashcode() != model.hashCode() : true;
		}
		model.setHashcode(model.hashCode());
		locationRepository.save(model);
		if (updateFlag) {
			operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
			aasClient.updateTenantUserProfileValue(model.getLocName(), model.getId());
		}
	}

	public void saveLocation(Location model) {
		model.setHashcode(model.hashCode());
		locationRepository.save(model);
	}

	@Override
	public Location findById(Long id) {
		return locationRepository.getOne(id);
	}

	public Location getById(Long id) {
		return locationRepository.findById(id).orElse(null);
	}

	@Override
	public Page<LocationDto> findBySearch(LocationFilter filter, Long tenantUserId) {

		if (filter == null || filter.getPageNo() < 0 || filter.getPageSize() < 1)
			return Page.empty();

		QLocation location = QLocation.location;
		BooleanExpression query = location.isNotNull();

		query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.LOCATION, null);

		if (filter.getTenantId() != null) {
			query = query.and(location.tenantId.eq(filter.getTenantId()));
		}

		if(filter.getActiveInd() != null){
			query = query.and(location.activeInd.eq(filter.getActiveInd()));
		}

		if (StringUtils.isNoneBlank(filter.getLocCode())) {
			String keyword = filter.getLocCode().trim();
			query = query.and(location.locCode.like("%" + keyword + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getLocName())) {
			String keyword = filter.getLocName().trim();
			query = query.and(location.locName.like("%" + keyword + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getLocationTag())) {
			String keyword = filter.getLocationTag().trim();
			List<Long> locTagsIds = new ArrayList<>();
			if (keyword.contains("|")) {
				String[] ids = keyword.split("\\|");
				for (int i = 0; i < ids.length; i++) {
					locTagsIds.add(Long.parseLong(ids[i]));
				}
			} else {
				locTagsIds.add(Long.parseLong(keyword));
			}

			if(locTagsIds.size() == 1){
				query = query.and(location.locationTag.like("%" + locTagsIds.get(0) + "%"));
			}else{
				List<BooleanExpression> booleanExpressionList = new ArrayList<>();
				for(int i=0; i<locTagsIds.size(); i++){
					BooleanExpression booleanExpression = location.locationTag.like("%" + locTagsIds.get(i) + "%");
					booleanExpressionList.add(booleanExpression);
				}

				BooleanExpression orExpression = null;
				for(int i=0; i<booleanExpressionList.size(); i++){
					if(i == 0){
						orExpression = booleanExpressionList.get(i);
					}else {
						orExpression = orExpression.or(booleanExpressionList.get(i));
					}
				}
				query = query.and(orExpression);
			}
		}

		if (StringUtils.isNoneBlank(filter.getAddress())) {
			String keyword = filter.getAddress().trim();
			List<AddrDTO> addrList = addrClient.findByFilter(new AddressFilter(keyword, keyword, keyword, true));
			List<Long> addrIdList = addrList.stream().map(d -> d.getId()).collect(Collectors.toList());
			query = query.and(location.addressId.in(addrIdList));
		}

		if (StringUtils.isNoneBlank(filter.getZipCode())) {
			String keyword = filter.getZipCode().trim();
			AddressFilter addressFilter = new AddressFilter();
			addressFilter.setZipCode(keyword);
			List<AddrDTO> addrList = addrClient.findByFilter(addressFilter);
			List<Long> addrIdList = addrList.stream().map(d -> d.getId()).collect(Collectors.toList());
			query = query.and(location.addressId.in(addrIdList));
		}

		Page<Location> locationPage = null;
		List<Location> locationList;
		List<LocationDto> locationDtoList;
		Pageable pageable;

		if (!StringUtils.isEmpty(filter.getSortBy())) {
			//address and zipCode from other tables from different database, locationTag display lookup instead
			if (filter.getSortBy().contains("address") || filter.getSortBy().contains("zipCode") || filter.getSortBy().contains("locationTag")) {
				locationList = locationRepository.findAll(query);
				locationDtoList = mapLocationDtoFromLocation(filter.getTenantId(), locationList);
				customSort(filter, locationDtoList);
				int start = filter.getPageNo() * filter.getPageSize();
				List<LocationDto> sortedLocationDtoList = locationDtoList.subList(start, Math.min(start + filter.getPageSize(), locationDtoList.size()));
				return new PageImpl<>(sortedLocationDtoList, PageRequest.of(filter.getPageNo(), filter.getPageSize()), locationDtoList.size());
			} else {
				locationPage = locationRepository.findAll(query, PageRequest.of(filter.getPageNo(), filter.getPageSize(), Sort.by(filter.getSort())));
				locationList = locationPage.getContent();
				locationDtoList = mapLocationDtoFromLocation(filter.getTenantId(), locationList);
			}
		} else {
			locationPage = locationRepository.findAll(query, PageRequest.of(filter.getPageNo(), filter.getPageSize(), Sort.by("activeInd").descending().and(Sort.by("locCode").ascending())));
			locationList = locationPage.getContent();
			locationDtoList = mapLocationDtoFromLocation(filter.getTenantId(), locationList);
		}
		if (CollectionUtils.isEmpty(locationList))
			return Page.empty();

		return new PageImpl<>(locationDtoList, PageRequest.of(filter.getPageNo(), filter.getPageSize()), locationPage.getTotalElements());
	}

    public void customSort(LocationFilter filter, List<LocationDto> locationDtoList) {
		Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(filter.getSortBy().toLowerCase().contains(",asc"));
        if (filter.getSortBy().contains("zipCode")) {
			locationDtoList.sort(SortingUtils.addDefaultSortExtraBaseDto(
					Comparator.comparing(LocationDto::getZipCode, sortOrder)
			));
        }
        //"address" field is the combine of 2 fields "greet" and "greet2"
        if (filter.getSortBy().contains("address")) {
			locationDtoList.sort(SortingUtils.addDefaultSortExtraBaseDto(
					Comparator.comparing(LocationDto::getStreet, sortOrder).thenComparing(LocationDto::getStreet2, sortOrder)
			));
        }
		if (filter.getSortBy().contains("locationTag")) {
			locationDtoList.sort(SortingUtils.addDefaultSortExtraBaseDto(
					Comparator.comparing(LocationDto::getLocationTag, sortOrder)
			));
		}
    }

	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		Location model = locationRepository.findById(id).get();
		if (model.getActiveInd() != status) {
			locationRepository.updateStatus(id, status);
			operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	@Transactional
	public Location findOneByLocation(Location search) {
		return locationRepository.findByTenantIdLocName(search.getTenantId(), search.getLocName());
	}

	@Transactional
	public Location findOrCreateLocation(LocationDto locationDto) {
		Location model = new Location();
		BeanCopier copier = BeanCopier.create(LocationDto.class, Location.class, false);
		copier.copy(locationDto, model, null);

		// save address or reuse addressId if exist
		Optional<AddrDTO> savedAddress = Optional.ofNullable(addrClient.createOrUpdateAddress(mapAddrDto(locationDto)));
		savedAddress.ifPresent(value -> model.setAddressId(value.getId()));

		// save address contact or reuse addressContactId if exist
		Optional<AddrContact> savedAddrContact = Optional
				.ofNullable(addrContactClient.createOrUpdateAddressContact(mapAddrContactDto(locationDto)));
		savedAddrContact.ifPresent(value -> model.setAddressContactId(value.getId()));

		Location result = findOneByLocation(model);
		if (result == null) {
			save(model);
			return model;
		} else {
			result.setAddressId(model.getAddressId());
			result.setAddressContactId(model.getAddressContactId());
			return result;
		}
	}

	@Transactional
	public List<LocationDto> findAllByTenantId(Long tenantId) {

		List<TenantUserProfileScopeDTO> tenantUserProfileScopeDTOList = aasClient.getTenantUserProfileScope(loggedUserInfo.getCurrentTenantUserId(), AppConstants.ProfileCode.LOCATION);

		Set<Long> uniqueLocationIds = tenantUserProfileScopeDTOList.stream()
				.filter(profileScope -> profileScope.getRefId() != null && profileScope.getRefId() > 0L)
				.map(profileScope -> profileScope.getRefId()).collect(Collectors.toSet());

		List<Location> locationList;
		if(tenantUserProfileScopeDTOList.isEmpty()) {
			locationList = locationRepository.findAllByTenantId(tenantId);
		}else{
			locationList = locationRepository.findAllById(uniqueLocationIds);
		}

		if (CollectionUtils.isEmpty(locationList))
			return Collections.emptyList();

		return mapLocationDtoFromLocation(tenantId, locationList);
	}

	@Override
	public List<LocationDto> findByFilter(LocationFilter filter) {

		QLocation location = QLocation.location;
		BooleanExpression query = location.isNotNull();

		if (filter.getTenantId() != null) {
			query = query.and(location.tenantId.eq(filter.getTenantId()));
		}

		if(filter.getActiveInd() != null){
			query = query.and(location.activeInd.eq(filter.getActiveInd()));
		}

		if (!StringUtils.isEmpty(filter.getLocName())){
			query = query.and(location.locName.eq(filter.getLocName()));
		}

		if (!CollectionUtils.isEmpty(filter.getIds())) {
			query = query.and(location.id.in(filter.getIds()));
		}

		if (!CollectionUtils.isEmpty(filter.getLocNames())) {
			List<String> locationNames = filter.getLocNames().stream().map(String::trim).collect(Collectors.toList());
			query = query.and(location.locName.in(locationNames));
		}

		List<Location> locationList = locationRepository.findAll(query);

		if (CollectionUtils.isEmpty(locationList))
			return Collections.emptyList();

		return mapLocationDtoFromLocation(filter.getTenantId(), locationList);
	}

	@Override
	public LocationDto findLocationByCode(Long tenantId, String locCode) {
		Location location = locationRepository.findByTenantIdAndLocCode(tenantId, locCode);
		if (location != null) {
			return mapLocationDtoFromLocation(tenantId, List.of(location)).get(0);
		}
		return null;
	}

	@Override
	public LocationDto findLocationByName(Long tenantId, String locCode) {
		Location location = locationRepository.findByTenantIdLocName(tenantId, locCode);
		if (location != null) {
			return mapLocationDtoFromLocation(tenantId, List.of(location)).get(0);
		}
		return null;
	}

	@Override
	public Location createOrFind(Location location) {

		QLocation qLocation = QLocation.location;
		BooleanExpression query = qLocation.isNotNull();

		query = qLocation.isNotNull();

		if (location != null) {
			if (location.getTenantId() != null)
				query = query.and(qLocation.tenantId.eq(location.getTenantId()));
			if (StringUtils.isNotBlank(location.getLocName()))
				query = query.and(qLocation.locName.eq(location.getLocName()));
		}

		List<Location> locationList = locationRepository.findAll(query);

		if (!CollectionUtils.isEmpty(locationList)) {
			return locationList.get(0);
		}

		location.setHashcode(location.hashCode());

		return locationRepository.save(location);
	}

	@Transactional
	public Set<Long> findAllByLocCode(String locCode) {

		List<Location> locationList = locationRepository.findByAllLocCode(locCode);

		if (CollectionUtils.isEmpty(locationList))
			return Collections.emptySet();

		Set<Long> locationIdList = locationList.stream().map(location -> location.getId()).collect(Collectors.toSet());

		return locationIdList;
	}

	private UpdateAddrDTO mapAddrDto(LocationDto createLocationDto) {
		UpdateAddrDTO updateAddrDTO = new UpdateAddrDTO();

		BeanCopier copier = BeanCopier.create(LocationDto.class, UpdateAddrDTO.class, false);
		copier.copy(createLocationDto, updateAddrDTO, null);

		return updateAddrDTO;
	}

	private AddrContactDTO mapAddrContactDto(LocationDto createLocationDto) {
		AddrContactDTO locationAddrContactDTO = new AddrContactDTO();

		BeanCopier copier = BeanCopier.create(LocationDto.class, AddrContactDTO.class, false);
		copier.copy(createLocationDto, locationAddrContactDTO, null);
		return locationAddrContactDTO;
	}

	public int countByCondition (Long tenantId, String locName){
		return locationRepository.countByTenantIdAndLocName(tenantId, locName);
	}


	public void checkDuplicatedEntries(List<CreateLocationDto> locationDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (CreateLocationDto dto : locationDtoList) {
			if (StringUtils.isEmpty(dto.getLocName())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getTenantId() + "||" + dto.getLocName().trim().toLowerCase());
			if (StringUtils.isEmpty(listPositionDup)) {
				listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
			} else {
				listPositionDup += ", " + dto.getExcelRowPosition();
			}
			mapCheckDup.put(dto.getTenantId() + "||" + dto.getLocName().trim().toLowerCase(), listPositionDup);
		}

		for (int itemIndex = 0; itemIndex < locationDtoList.size(); itemIndex++) {
			CreateLocationDto dto = locationDtoList.get(itemIndex);
			if (dto.getTenantId() == null || StringUtils.isEmpty(dto.getLocName())) {
				continue;
			}
			String listPositionDup = mapCheckDup.get(dto.getTenantId() + "||" + dto.getLocName().trim().toLowerCase());
			if (!listPositionDup.contains(",")) {
				continue;
			}
			StringBuilder cellErrors = mapExcelError.get(itemIndex);
			if (cellErrors == null) {
				cellErrors = new StringBuilder();
			}
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("location.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getTenantId() + "||" + dto.getLocName().trim().toLowerCase()), dto.getLocName() }));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	public void checkExistedEntries(List<CreateLocationDto> locationDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < locationDtoList.size(); itemIndex++) {
			CreateLocationDto dto = locationDtoList.get(itemIndex);
			if (dto.getTenantId() == null || StringUtils.isEmpty(dto.getLocName())) {
				continue;
			}

			int existedLocationAmount = countByCondition(dto.getTenantId(),dto.getLocName());
			if (existedLocationAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Location Name"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	private void checkValidCountryAndStateAndCity (List<CreateLocationDto> locationDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		for (int itemIndex = 0; itemIndex < locationDtoList.size(); itemIndex++) {
			CreateLocationDto locationDto = locationDtoList.get(itemIndex);
			if(ObjectUtils.anyNotNull(locationDto.getState(), locationDto.getCountryShortName(), locationDto.getCity())){
				//Check valid country
				CountryDTO country;
				try {
					country = addrClient.getCountryByCountryShortName(locationDto.getCountryShortName());
				}
				catch (Exception e){
					country = null;
				}
				if (country == null) {
					StringBuilder cellErrors = mapExcelError.get(itemIndex);
					if (cellErrors == null) {
						cellErrors = new StringBuilder();
					}
					excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Country"}));
					excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
					mapExcelError.put(itemIndex, cellErrors);
				}
				else {
					//Check valid state and city
					List<StateDTO> listState = addrClient.getStatesByCountryShortName(country.getCountryShortName());
					if (StringUtils.isEmpty(locationDto.getState()) && StringUtils.isEmpty(locationDto.getCity())){
						continue;
					}
					boolean stateValid = false;
					for (StateDTO state : listState) {
						if (state.getStateFullName().equals(locationDto.getState())) {
							stateValid = true;
							if (stateValid) {
								List<CityDTO> listCity = addrClient.getCitiesByStateId(state.getId());
								if (StringUtils.isEmpty(locationDto.getCity())) {
									continue;
								}
								boolean cityValid = false;
								for (CityDTO city : listCity) {
									if (city.getCityFullName().equals(locationDto.getCity())) {
										cityValid = true;
										break;
									}
								}
								if (!cityValid) {
									StringBuilder cellErrors = mapExcelError.get(itemIndex);
									if (cellErrors == null) {
										cellErrors = new StringBuilder();
										excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.location.data.not.existing", new String[]{"City","State"}));
										excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
										mapExcelError.put(itemIndex, cellErrors);
									}
								}
							}
						}
					}
					if (!stateValid) {
						StringBuilder cellErrors = mapExcelError.get(itemIndex);
						if (cellErrors == null) {
							cellErrors = new StringBuilder();
						}
						excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.location.data.not.existing", new String[]{"State","Country"}));
						excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
						mapExcelError.put(itemIndex, cellErrors);
					}
				}
			}
		}
	}

	public void saveExcelData(List<CreateLocationDto> locationDtoList, Map<Integer, StringBuilder> mapExcelError, Long tenantId, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < locationDtoList.size(); itemIndex++) {
			CreateLocationDto locationDto = locationDtoList.get(itemIndex);
			try {
				saveLocationItem(locationDto, tenantId);
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

	public void saveLocationItem(@RequestBody @Valid CreateLocationDto createLocationDto, Long tenantId) {
		Location model = new Location();
		try {
			BeanCopier copier = BeanCopier.create(CreateLocationDto.class, Location.class, false);
			copier.copy(createLocationDto, model, null);

			UpdateAddrDTO addrDTO = new UpdateAddrDTO();
			addrDTO.setCountryShortName(createLocationDto.getCountryShortName());
			addrDTO.setState(createLocationDto.getState());
			addrDTO.setCity(createLocationDto.getCity());
			addrDTO.setZipCode(createLocationDto.getZipCode());
			addrDTO.setStreet(createLocationDto.getStreet());
			addrDTO.setStreet2(createLocationDto.getStreet2());
			String[] arrayLocationTag = createLocationDto.getLocationTagsString().split("\\|");
			List<CommonTag> commonTagList = new ArrayList<>();
			for (int i =0; i<arrayLocationTag.length;i++){
				CommonTag commonTag = new CommonTag();
				commonTag.setTag(arrayLocationTag[i]);
				commonTag.setTagType(AppConstants.CommonTag.LOCATION_TAG);
				commonTagList.add(commonTag);
			}
			createLocationDto.setLocationTags(commonTagList);

			// save address or reuse addressId if exist when any one of the fields entered
			if(null != addrDTO && ObjectUtils.anyNotNull(addrDTO.getState(), addrDTO.getCountryShortName(), addrDTO.getZipCode(), addrDTO.getStreet(), addrDTO.getStreet2())) {
				Optional<AddrDTO> savedAddress = Optional
						.ofNullable(addrClient.createOrUpdateAddress(addrDTO));
				savedAddress.ifPresent(value -> model.setAddressId(value.getId()));
			}

			// save commonTag if new
			StringBuilder lookupBuilder = processLocationTags(createLocationDto, tenantId);
			if(lookupBuilder.length() > 0) {
				model.setLocationTag(lookupBuilder.toString());
			} else {
				model.setLocationTag(null);
			}

			saveLocation(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.location.duplicate");
		}
	}

	public StringBuilder processLocationTags(CreateLocationDto locationDto, Long tenantId) {
		List<CommonTag> locationTagList = locationDto.getLocationTags();
		List<CommonTag> savedLocationTagList = new ArrayList<>();
		List<CommonTag> newLocationTagList = null != locationTagList ? locationTagList.stream().filter(i -> i.getId() == null).collect(Collectors.toList()) : new ArrayList<>();
		List<CommonTag> existingLocationTagList = null != locationTagList ? locationTagList.stream().filter(i -> i.getId() != null).collect(Collectors.toList()) : new ArrayList<>();
		List<CommonTag> updatedNewLocationTagList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(newLocationTagList)) {
			updatedNewLocationTagList = newLocationTagList.stream().filter(commonTag -> {
				CommonTagFilter commonTagFilter = new CommonTagFilter();
				commonTagFilter.setTagType(AppConstants.CommonTag.LOCATION_TAG);
				commonTagFilter.setTag(commonTag.getTag());
				commonTagFilter.setTenantId(tenantId);
				List<CommonTag> existingLocationTag = commonTagService.findByFilter(commonTagFilter);
				if (null != existingLocationTag && 0  < existingLocationTag.size()) {
					existingLocationTagList.addAll(existingLocationTag);
					return false;
				} else {
					return true;
				}
			}).map(commonTag -> {
				commonTag.setTenantId(tenantId);
				commonTag.setTagType(AppConstants.CommonTag.LOCATION_TAG);
				commonTag.setTag(commonTag.getTag());
				return commonTag;
			}).collect(Collectors.toList());

			savedLocationTagList = commonTagRepository.saveAll(updatedNewLocationTagList);
		}

		if (CollectionUtils.isNotEmpty(savedLocationTagList)) {
			existingLocationTagList.addAll(savedLocationTagList);
		}
		StringBuilder locationTagBuilder = new StringBuilder();
		existingLocationTagList.forEach(i -> {
			if (locationTagBuilder.length() > 0) {
				locationTagBuilder.append("|");
			}
			locationTagBuilder.append(i.getId());
		});
		return locationTagBuilder;
	}

	public ExcelResponseMessage uploadExcel(MultipartFile file, Long tenantId){
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();;
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(CreateLocationDto.class, workbook, sheet, fileName
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

	public void downloadExcel(LocationFilter filter,HttpServletResponse response, Long tenantUserId){
		Page<LocationDto> dataPage = findBySearch(filter,tenantUserId);
		List<LocationDto> dataList = dataPage.toList();
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_EXPORT);
		excelUtils.exportExcel(response, dataList, LocationDto.class, template);
	}

	private List<LocationDto> mapLocationDtoFromLocation(Long tenantId, List<Location> locationList){
		CommonTagFilter locationTagFilter = new CommonTagFilter();
		locationTagFilter.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		if(tenantId != null) locationTagFilter.setTenantId(tenantId);
		List<CommonTag> locationTagList = commonTagService.findByFilter(locationTagFilter);

		Map<Long, CommonTag> locationTagMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(locationTagList)) {
			locationTagMap = locationTagList.stream().collect(Collectors.toMap(commonTag -> commonTag.getId(), commonTag -> commonTag));
		}

		Map<Long, CommonTag> finalLocationTagMap = locationTagMap;

		Set<Long> uniqueAddressId = locationList.stream()
				.filter(location -> location.getAddressId() != null && location.getAddressId() > 0L)
				.map(location -> location.getAddressId()).collect(Collectors.toSet());

		Set<Long> uniqueAddressContactId = locationList.stream()
				.filter(location -> location.getAddressContactId() != null && location.getAddressContactId() > 0L)
				.map(location -> location.getAddressContactId()).collect(Collectors.toSet());

		List<AddrDTO> addressList = new ArrayList<>();
		List<AddrContact> addressContactList = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(uniqueAddressId)) {
			addressList = addrClient.findByFilter(new AddressFilter(new ArrayList<>(uniqueAddressId)));
		}
		if(CollectionUtils.isNotEmpty(uniqueAddressId)) {
			addressContactList = addrContactClient
					.findByFilter(new AddrContactFilter(new ArrayList<>(uniqueAddressContactId)));
		}

		Map<Long, AddrDTO> addressMap = new HashMap<>();
		Map<Long, AddrContact> addressContactMap = new HashMap<>();

		if (CollectionUtils.isNotEmpty(addressList)) {
			addressMap = addressList.stream().collect(Collectors.toMap(address -> address.getId(), address -> address));
		}

		if (CollectionUtils.isNotEmpty(addressContactList))
			addressContactMap = addressContactList.stream()
					.collect(Collectors.toMap(contact -> contact.getId(), contact -> contact));

		List<LocationDto> locationDtoList = new ArrayList<>();

		for (Location location : locationList) {

			LocationDto locationDto = modelMapper.map(location, LocationDto.class);

			if (ValidationUtils.Number.isValidId(location.getAddressId()) && addressMap.size() > 0
					&& addressMap.get(location.getAddressId()) != null) {
				locationDto.setAddr(addressMap.get(location.getAddressId()));
				locationDto.setUnit(addressMap.get(location.getAddressId()).getUnit());
				locationDto.setStreet(addressMap.get(location.getAddressId()).getStreet());
				locationDto.setStreet2(addressMap.get(location.getAddressId()).getStreet2());
				locationDto.setState(addressMap.get(location.getAddressId()).getState());
				locationDto.setZipCode(addressMap.get(location.getAddressId()).getZipCode());
				locationDto.setCountryShortName(addressMap.get(location.getAddressId()).getCountryShortName());
				locationDto.setCity(addressMap.get(location.getAddressId()).getCity());
				locationDto.setZipCode(addressMap.get(location.getAddressId()) != null? addressMap.get(location.getAddressId()).getZipCode() : "");
			}

			if (ValidationUtils.Number.isValidId(location.getAddressContactId()) && addressContactMap.size() > 0
					&& addressContactMap.get(location.getAddressContactId()) != null)
				locationDto.setAddrContact(addressContactMap.get(location.getAddressContactId()));

			List<Long> locTagsIds = new ArrayList<>();
			if(location.getLocationTag() != null) {
				if (location.getLocationTag().contains("|")) {
					String[] ids = location.getLocationTag().split("\\|");
					for (int i = 0; i < ids.length; i++) {
						locTagsIds.add(Long.parseLong(ids[i]));
					}
				} else {
					locTagsIds.add(Long.parseLong(location.getLocationTag()));
				}

				List<CommonTag> locationTags = locTagsIds.stream()
						.filter(id -> finalLocationTagMap.get(id) != null).map(id -> finalLocationTagMap.get(id)).collect(Collectors.toList());
				StringBuilder tagSb = new StringBuilder();
				StringBuilder idSb = new StringBuilder();
				locationTags.forEach(locationTag -> {
					if (tagSb.length() == 0) {
						tagSb.append(locationTag.getTag());
						idSb.append(locationTag.getId());
					} else {
						tagSb.append("|").append(locationTag.getTag());
						idSb.append("|").append(locationTag.getId());
					}
				});
				locationDto.setLocationTag(tagSb.toString());
				locationDto.setLocationTagId(idSb.toString());
				locationDto.setLocationTags(locationTags);
			}
			locationDtoList.add(locationDto);
		}
		return locationDtoList;
	}

	private List<LocationDto> mapLocationDtoFromLocationWithoutAddrAndContact(Long tenantId, List<Location> locationList){
		CommonTagFilter locationTagFilter = new CommonTagFilter();
		locationTagFilter.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		if(tenantId != null) locationTagFilter.setTenantId(tenantId);
		List<CommonTag> locationTagList = commonTagService.findByFilter(locationTagFilter);

		Map<Long, CommonTag> locationTagMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(locationTagList)) {
			locationTagMap = locationTagList.stream().collect(Collectors.toMap(commonTag -> commonTag.getId(), commonTag -> commonTag));
		}

		Map<Long, CommonTag> finalLocationTagMap = locationTagMap;
		List<LocationDto> locationDtoList = new ArrayList<>();

		for (Location location : locationList) {

			LocationDto locationDto = modelMapper.map(location, LocationDto.class);

			List<Long> locTagsIds = new ArrayList<>();
			if(location.getLocationTag() != null) {
				if (location.getLocationTag().contains("|")) {
					String[] ids = location.getLocationTag().split("\\|");
					for (int i = 0; i < ids.length; i++) {
						locTagsIds.add(Long.parseLong(ids[i]));
					}
				} else {
					locTagsIds.add(Long.parseLong(location.getLocationTag()));
				}

				List<CommonTag> locationTags = locTagsIds.stream()
						.filter(id -> finalLocationTagMap.get(id) != null).map(id -> finalLocationTagMap.get(id)).collect(Collectors.toList());
				StringBuilder tagSb = new StringBuilder();
				StringBuilder idSb = new StringBuilder();
				locationTags.forEach(locationTag -> {
					if (tagSb.length() == 0) {
						tagSb.append(locationTag.getTag());
						idSb.append(locationTag.getId());
					} else {
						tagSb.append("|").append(locationTag.getTag());
						idSb.append("|").append(locationTag.getId());
					}
				});
				locationDto.setLocationTag(tagSb.toString());
				locationDto.setLocationTagId(idSb.toString());
				locationDto.setLocationTags(locationTags);
			}
			locationDtoList.add(locationDto);
		}
		return locationDtoList;
	}

	protected List<Sort.Order> getSort (String sortBy){
		String[] result = sortBy.split("\\|");

		List<Sort.Order> orders = new ArrayList<>();
		for (String order : result) {
			orders.add(new Sort.Order(order.toUpperCase().contains("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, order.split(",")[0]));
		}
		return orders;
	}

	public List<LocationDto> createOrUpdateMultiple(List<LocationDto> locationDtoList) {
		List<Long> idList = locationDtoList.stream()
				.map(LocationDto::getId)
				.filter(ObjectUtils::isNotEmpty)
				.collect(Collectors.toList());

		List<Location> locationsToSave;
		if (idList.isEmpty()) {
			locationsToSave = buildListLocationToCreate(locationDtoList);
		} else {
			locationsToSave = buildListLocationToSaveAndUpdate(locationDtoList, idList);
		}

		List<Location> locationsSaved = locationRepository.saveAll(locationsToSave);
		return locationsSaved.stream().map(location -> modelMapper.map(location, LocationDto.class)).collect(Collectors.toList());
	}

	@Override
	public List<Location> findByListLocName(Long tenantId, Collection<String> listLocName) {
		return locationRepository.findByListLocName(tenantId, listLocName);
	}

	private List<Location> buildListLocationToCreate(List<LocationDto> locationDtoList) {
		return locationDtoList.stream().map(location -> {
			var locationToSave = modelMapper.map(location, Location.class);
			setLocationInfo(location, locationToSave);
			return locationToSave;
		}).filter(location -> ObjectUtils.isNotEmpty(location.getLocName())).collect(Collectors.toList());
	}

	private List<Location> buildListLocationToSaveAndUpdate(List<LocationDto> locationDtoList, List<Long> idList) {
		List<Location> existingLocationList = locationRepository.findAllByIdList(idList);
		Map<Long, Location> existingLocationById = existingLocationList.stream().collect(Collectors.toMap(Location::getId, Function.identity()));

		return locationDtoList.stream().map(location -> {
			var locationToSave = modelMapper.map(location, Location.class);
			if (ObjectUtils.isNotEmpty(location.getId())) {
				var existingLocation = existingLocationById.get(location.getId());
				if (ObjectUtils.isNotEmpty(existingLocation)) {
					locationToSave = existingLocation;
				}
			}
			setLocationInfo(location, locationToSave);
			return locationToSave;
		}).filter(location -> ObjectUtils.isNotEmpty(location.getLocName())).collect(Collectors.toList());
	}

	private void setLocationInfo(LocationDto location, Location locationToSave) {
		var newAddress = location.getAddr();
		if (ObjectUtils.isNotEmpty(newAddress)) {
			locationToSave.setAddressId(newAddress.getId());
		}
		var newContact = location.getAddrContact();
		if (ObjectUtils.isNotEmpty(newContact)) {
			locationToSave.setAddressContactId(newContact.getId());
		}
		if (ObjectUtils.isEmpty(locationToSave.getLocCode())) {
			locationToSave.setLocCode(locationToSave.getLocName());
		}
		locationToSave.setActiveInd(true);
	}

	public void validateAndSaveExcelData(Long currentTenantId, Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo) {
		List<CreateLocationDto> listLocationAfterReadExcel = excelUtils.parseExcelToDto(CreateLocationDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		String emailTemplateName = StringUtils.EMPTY;
		for (CreateLocationDto dto: listLocationAfterReadExcel){
			dto.setTenantId(currentTenantId);
		}
		checkDuplicatedEntries(listLocationAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listLocationAfterReadExcel, mapExcelError, excelEmailErrors);
		checkValidCountryAndStateAndCity(listLocationAfterReadExcel, mapExcelError, excelEmailErrors);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listLocationAfterReadExcel, mapExcelError, currentTenantId, excelEmailErrors);

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

		excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.locations", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);;
	}

	@Override
	public List<Location> findByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		QLocation qLocation = QLocation.location;
		BooleanExpression query = qLocation.isNotNull();
		query = query.and(qLocation.id.in(ids));
		return locationRepository.findAll(query);
	}
}
