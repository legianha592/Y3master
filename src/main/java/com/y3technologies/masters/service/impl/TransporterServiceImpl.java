package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.AddrClient;
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CusTomerRetrieveInf;
import com.y3technologies.masters.dto.DropdownPartnerDto;
import com.y3technologies.masters.dto.TransporterDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.PartnerCustomer;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.PartnerTypes;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.QPartnerCustomer;
import com.y3technologies.masters.model.QPartnerTypes;
import com.y3technologies.masters.model.QPartners;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.LocationRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.repository.PartnerConfigRepository;
import com.y3technologies.masters.repository.PartnerCustomerRepository;
import com.y3technologies.masters.repository.PartnerLocationRepository;
import com.y3technologies.masters.repository.PartnerTypesRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import com.y3technologies.masters.service.CustomerService;
import com.y3technologies.masters.service.LocationService;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.PartnerLocationService;
import com.y3technologies.masters.service.PartnerTypesService;
import com.y3technologies.masters.service.PartnersService;
import com.y3technologies.masters.service.ProfileScopeService;
import com.y3technologies.masters.service.TransporterService;
import com.y3technologies.masters.util.BeanCopierFactory;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import com.y3technologies.masters.util.SortingUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.Projections.list;

@Service
@RequiredArgsConstructor
public class TransporterServiceImpl implements TransporterService {
	private final PartnersRepository partnersRepository;
	private final OperationLogService operationLogService;
	private final PartnerTypesService partnerTypesService;
	private final PartnerLocationService partnerLocationService;
	private final LocationService locationService;
	private final PartnerConfigRepository partnerConfigRepository;
	private final LookupRepository lookupRepository;
	private final PartnerTypesRepository partnerTypesRepository;
	private final PartnerLocationRepository partnerLocationRepository;
	private final CustomerService customerService;
	private final AddrClient addrClient;
	private final AddrContactClient addrContactClient;
	private final AasClient aasClient;
	private final ExcelClient excelClient;
	private final ExcelUtils excelUtils;
	private final MessagesUtilities messagesUtilities;
	private final MastersApplicationPropertiesConfig propertiesConfig;
	private final PartnersService partnersService;
	private final ProfileScopeService profileScopeService;
	private final LocationRepository locationRepository;
	private final ThreadPoolTaskExecutor executor;
	private final PartnerCustomerRepository partnerCustomerRepository;

	@Autowired
	private EntityManager entityManager;
	@Override
	@Transactional
	public Partners save(TransporterDto dto) {
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
			BeanCopierFactory.TransporterDto_partners_copier.copy(dto,  model, null);
		}
		model.setHashcode(model.hashCode());
		model.setPartnerCode(dto.getPartnerCode());
		model.setEmail(dto.getEmail());
		model.setPhoneCountryShortName(dto.getCountryShortName());

		UpdateAddrDTO updateAddrDTO = new UpdateAddrDTO();
		// save address or reuse addressId if exist
		BeanCopierFactory.TransporterDto_updateAddressDto_copier.copy(dto, updateAddrDTO, null);
		Optional<AddrDTO> savedAddress = Optional.ofNullable(addrClient.createOrUpdateAddress(updateAddrDTO));

		AddrContactDTO addrContactDto = new AddrContactDTO();
		// save address contact or reuse addressContactId if exist
		BeanCopierFactory.TransporterDto_addressContactDto_copier.copy(dto, addrContactDto, null);
		Optional<AddrContact> savedAddrContact = Optional
				.ofNullable(addrContactClient.createOrUpdateAddressContact(addrContactDto));

		Location existingLocation = locationService.findById(dto.getLocationId());
		if(existingLocation == null) throw new TransactionException("exception.location.invalid");

		partnersRepository.save(model);

		Long transId = model.getId();
		int partnerCustomerHashcode = model.getHashcode();
		List<PartnerCustomer> partnerCustomerList = new ArrayList<>();
		dto.getCustomerIdList().forEach(cusId -> {
			PartnerCustomer partnerCustomer = new PartnerCustomer();
			partnerCustomer.setPartnerId(transId);
			partnerCustomer.setCustomerId(cusId);
			partnerCustomer.setHashcode(partnerCustomerHashcode);
			partnerCustomerList.add(partnerCustomer);
		});
		partnerCustomerRepository.saveAll(partnerCustomerList);

		if (updateFlag) {
			operationLogService.log(dto.getId() == null, model, model.getClass().getSimpleName(), model.getId().toString());
		}

		if (newPartner) {
			Lookup lookup = lookupRepository.findByLookupTypeAndLookupCode(AppConstants.LookupType.PARTNER_TYPES, AppConstants.PartnerType.TRANSPORTER);
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
		partnerLocation.setLocationId(existingLocation.getId());
		partnerLocation.setAddressId(savedAddress.get().getId());
		partnerLocation.setAddressContactId(savedAddrContact.get().getId());
		partnerLocationService.save(partnerLocation);
		return model;
	}

	@Override
	@Transactional(readOnly = true)
	public TransporterDto getById(Long id, Long tenantId) {
		Partners partners = partnersRepository.findById(id).get();
		TransporterDto dto = new TransporterDto();
		BeanCopierFactory.Partners_transporterDto_copier.copy(partners, dto, null);
		Set<Long> setPartnerIds = new HashSet<>();
		setPartnerIds.add(id);

		return mapDto(setAddressAndAddressContact(dto, id), setPartnerIds, tenantId);
	}

	private TransporterDto setAddressAndAddressContact(TransporterDto dto, Long id){
 		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(id);
		if (!partnerLocationList.isEmpty()) {
			PartnerLocation partnerLocation = partnerLocationList.get(0);
			Long addressId = partnerLocation.getAddressId();
			AddrDTO address = null;
			if (addressId != null) {
				try {
					Optional<AddrDTO> existingAddress = Optional.ofNullable(addrClient.getAddress(addressId));
					if (existingAddress.isPresent())
						address = existingAddress.get();
				} catch (Exception exception) {
					throw exception;
				}
			}
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
			if(partnerLocation.getLocationId() != null) {
				Location location = locationService.getById(partnerLocation.getLocationId());
				if (location != null) {
					dto.setLocationId(location.getId());
					dto.setLocationName(location.getLocName());
				}
			}
			dto.setAddr(address);
			dto.setAddrContact(addrContact);
		}

		return dto;
	}

	@Override
	public Partners findById(Long id) {
		return partnersRepository.findById(id).orElse(null);
	}

	@Override
	public Page<TransporterDto> findBySearch(PartnersFilter filter, Long tenantUserId, String token) {

		if (filter == null || filter.getPageNo() < 0 || filter.getPageSize() < 1)
			return Page.empty();

		QPartners partners = QPartners.partners;
		QPartners customers = new QPartners("customers");
		QPartnerCustomer partnerCustomer = new QPartnerCustomer("partnerCustomer");
		QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
		BooleanExpression query = partners.isNotNull();

		query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.TRANSPORTER, token);

		if (filter.getTenantId() != null) {
			query = query.and(partners.tenantId.eq(filter.getTenantId()));
		}

		if (CollectionUtils.isNotEmpty(filter.getCustomerIdList())) {
			List<Long> customerIds =filter.getCustomerIdList();
			query = query.and(customers.id.in(customerIds));
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
				.where(partnerTypes.lookup.lookupCode.eq(AppConstants.PartnerType.TRANSPORTER))));

		JPQLQuery<Partners> q = new JPAQuery<>(entityManager);
		q.distinct().from(partners)
				.leftJoin(partnerCustomer).on(partners.id.eq(partnerCustomer.partnerId))
				.innerJoin(customers).on(partnerCustomer.customerId.eq(customers.id))
				.where(query);

		Pageable pageable = PageRequest.of(filter.getPageNo(), filter.getPageSize());

		//Set orderBy to query
		if (StringUtils.isNotEmpty(filter.getSortBy())){
			if (filter.getSortBy().contains("name")) {
				JPQLQuery jpqlQuery = filter.getSortBy().toLowerCase().contains(",asc") ? q.orderBy(partners.partnerName.asc()): q.orderBy(partners.partnerName.desc());
			} else if (filter.getSortBy().contains("partnerCode")){
				JPQLQuery jpqlQuery = filter.getSortBy().toLowerCase().contains(",asc") ? q.orderBy(partners.partnerCode.asc()).orderBy(partners.id.desc()): q.orderBy(partners.partnerCode.desc()).orderBy(partners.id.desc());
			}
		} else {
			q.orderBy(partners.partnerCode.asc()).orderBy(partners.id.desc());
		}
		//query without paging to get total element
		List<TransporterDto> transporterDtoList =
				q.select(Projections.bean(TransporterDto.class,
						partners.tenantId, partners.id, partners.activeInd, partners.createdBy, partners.createdDate, partners.updatedBy, partners.updatedDate, partners.version,
						partners.partnerCode,partners.partnerName,partners.customerId))
				.fetch();

		//query with paging
		List<TransporterDto> transporterDtoListPaging =
				q.select(Projections.bean(TransporterDto.class,
						partners.tenantId, partners.id, partners.activeInd, partners.createdBy, partners.createdDate,
						partners.updatedBy, partners.updatedDate, partners.version,
						partners.partnerCode,partners.partnerName,partners.customerId))
						.offset(pageable.getOffset()).limit(pageable.getPageSize())
						.fetch();
		Set<Long> setPartnerIds = transporterDtoListPaging.stream().map(TransporterDto::getId).collect(Collectors.toSet());
		List<TransporterDto> result = transporterDtoListPaging.stream().map(transporterDto -> mapDto(transporterDto, setPartnerIds, filter.getTenantId()))
				.collect(Collectors.toList());
		//sort customerName in here!
		if (filter.getSortBy().contains("customerName")) {
			result = filter.getSortBy().toLowerCase().contains(",asc")
					? result.stream().sorted(Comparator.comparing(TransporterDto::getCustomerName)).collect(Collectors.toList())
					: result.stream().sorted(Comparator.comparing(TransporterDto::getCustomerName).reversed()).collect(Collectors.toList());
		}
		return new PageImpl<>(result, PageRequest.of(filter.getPageNo(), filter.getPageSize()), transporterDtoList.size());
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	class PartnerIdCustomer {
		private Long partnerId;
		private String cusNames;
		private List<CusTomerRetrieveInf> cusTomerRetrieveInfs;
	}

	private TransporterDto mapDto(TransporterDto transporterDto, Set<Long> setPartnerIds, Long tenantId) {
		List<PartnerIdCustomer> partnerIdCustomers = getPartnerIdCustomer(setPartnerIds, tenantId);
		PartnerIdCustomer partnerIdCustomer = partnerIdCustomers.stream()
				.filter(pIdCustomer -> pIdCustomer.getPartnerId().equals(transporterDto.id)).findFirst().orElse(null);
		transporterDto.setCustomerName(partnerIdCustomer.getCusNames());
		transporterDto.setCusTomerRetrieveInfs(partnerIdCustomer.getCusTomerRetrieveInfs());
		return transporterDto;
	}

	private Partners mapExcel(Partners partner, Set<Long> setPartnerIds, Long tenantId) {
		List<PartnerIdCustomer> partnerIdCustomers = getPartnerIdCustomer(setPartnerIds, tenantId);
		PartnerIdCustomer partnerIdCustomer = partnerIdCustomers.stream()
				.filter(pIdCustomer -> pIdCustomer.getPartnerId().equals(partner.getId())).findFirst().orElse(null);
		if (Objects.nonNull(partnerIdCustomer) && StringUtils.isNotEmpty(partnerIdCustomer.getCusNames())) {
			String customerName = partnerIdCustomer.getCusNames();
			partner.setCustomerName(customerName.substring(0, customerName.length() - 1));
		}
		return partner;
	}

	private List<PartnerIdCustomer> getPartnerIdCustomer(Set<Long> setPartnerIds, Long tenantId) {
		String getCusNameByPartnerIds = "SELECT  pc.partnerId, p.partnerName, pc.customerId\n" +
				"FROM PartnerCustomer pc JOIN Partners p ON pc.customerId = p.id AND p.tenantId = :tenantId\n" +
				"WHERE pc.partnerId IN (:setPartnerIds)";

		List<Object[]> dtoList = entityManager.createQuery(getCusNameByPartnerIds)
				.setParameter("setPartnerIds", setPartnerIds)
				.setParameter("tenantId", tenantId)
				.getResultList();

		List<PartnerIdCustomer> result = new ArrayList<>();
		List<Long> customerIds = new ArrayList<>();
		Map<Long, String> getCusNameByPartnerId = new HashMap<>();
		List<CusTomerRetrieveInf> cusTomerRetrieveInfList = new ArrayList<>();
		dtoList.forEach(objArr -> {
			Long partnerId = Long.parseLong(objArr[0].toString());
			String cusName = objArr[1].toString() + "|";
			Long cusId = Long.parseLong(objArr[2].toString());
			if (getCusNameByPartnerId.containsKey(partnerId)) {
				getCusNameByPartnerId.put(partnerId, getCusNameByPartnerId.get(partnerId) + cusName);
			} else {
				getCusNameByPartnerId.put(partnerId, cusName);
			}
			cusTomerRetrieveInfList.add(new CusTomerRetrieveInf(cusId, objArr[1].toString(), objArr[1].toString()));
			customerIds.add(cusId);
		});


		getCusNameByPartnerId.forEach((key, value) -> {
			PartnerIdCustomer partnerIdCustomer = new PartnerIdCustomer();
			partnerIdCustomer.setPartnerId(key);
			partnerIdCustomer.setCusNames(value);
			partnerIdCustomer.setCusTomerRetrieveInfs(cusTomerRetrieveInfList);
			result.add(partnerIdCustomer);
		});
		return result;
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
	public List<Partners> findByFilter(PartnersFilter filter, Long tenantUserId, boolean isCallFromFindByFilterController) {

		QPartners partners = QPartners.partners;
		QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
		BooleanExpression query = partners.isNotNull();

		if (isCallFromFindByFilterController) {
			query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.TRANSPORTER, null);
		}

		if (filter.getTenantId() != null) {
			query = query.and(partners.tenantId.eq(filter.getTenantId()));
		}

		if (filter.getCustomerId() != null) {
			query = query.and(partners.customerId.eq(filter.getCustomerId()));
		}

		if (StringUtils.isNoneBlank(filter.getName())) {
			String keyword = filter.getName().trim();
			query = query.and(partners.partnerName.eq(keyword));
		}

		if (StringUtils.isNoneBlank(filter.getPartnerCode())) {
			String keyword = filter.getPartnerCode().trim();
			query = query.and(partners.partnerCode.eq(keyword));
		}

		query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
				.where(partnerTypes.lookup.lookupCode.eq(AppConstants.PartnerType.TRANSPORTER))));

		if (CollectionUtils.isNotEmpty(filter.getPartnerIdList())) {
			query = query.and(partners.id.in(filter.getPartnerIdList()));
		}

		if(filter.getActiveInd() != null){
			query = query.and(partners.activeInd.eq(filter.getActiveInd()));
		}

		return partnersRepository.findAll(query);
	}

	@Override
	public DataTablesOutput<Partners> query(DataTablesInput input) {
		if(input.getColumn("tenantId") != null
				&& StringUtils.isNotEmpty(input.getColumn("tenantId").getSearch().getValue())
				&& input.getColumn("tenantId").getSearchable()) {
			Specification<Partners> sTenantId = new Specification<Partners>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Predicate toPredicate(Root<Partners> root, CriteriaQuery<?> query,
											 CriteriaBuilder criteriaBuilder) {
					return criteriaBuilder.equal(root.get("tenantId"), input.getColumn("tenantId").getSearch().getValue());
				}
			};
			input.getColumn("tenantId").setSearchable(false);

			return partnersRepository.findAll(input, sTenantId);
		}
		return partnersRepository.findAll(input);
	}

	@Override
	public List<Partners> findByTenantId(Long currentTenantId) {
		return partnersRepository.findByTenantId(currentTenantId);
	}

	@Override
	public List<Partners> getPartnerByTenantId(Long currentTenantId, String partnerType) {
		List<Partners> partners = partnersRepository.getAllByTenantIdAndPartnerType(currentTenantId, partnerType);
		partners.forEach(partner -> {
			partner.setCustomerName(partnersRepository.getPartnersByCustomerId(partner.getCustomerId()).getPartnerName());
		});
		return partners;
	}

	@Override
	public List<Partners> getTranporterToExport(Long tenantId, Long tenantUserId, PartnersFilter filter) {

		QPartners partners = QPartners.partners;
		QPartners customers = new QPartners("customers");
		QPartnerCustomer partnerCustomer = new QPartnerCustomer("partnerCustomer");
		QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
		BooleanExpression query = partners.isNotNull();

		if (tenantId != null) {
			query = query.and(partners.tenantId.eq(tenantId));
			query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.TRANSPORTER, "");
		}

		if (CollectionUtils.isNotEmpty(filter.getCustomerIdList())) {
			List<Long> customerIds =filter.getCustomerIdList();
			query = query.and(customers.id.in(customerIds));
		}

		if (StringUtils.isNotBlank(filter.getName())) {
			String keyword = filter.getName().trim();
			query = query.and(partners.partnerName.like("%" + keyword + "%"));
		}

		if (StringUtils.isNotBlank(filter.getPartnerCode())) {
			String keyword = filter.getPartnerCode().trim();
			query = query.and(partners.partnerCode.like("%" + keyword + "%"));
		}

		if (!Objects.isNull(filter.getActiveInd())) {
			query = query.and(partners.activeInd.eq(filter.getActiveInd()));
		}

		query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
				.where(partnerTypes.lookup.lookupCode.eq(AppConstants.PartnerType.TRANSPORTER))));

		JPQLQuery<Partners> q = new JPAQuery<>(entityManager);
		q.distinct().from(partners)
				.leftJoin(partnerCustomer).on(partners.id.eq(partnerCustomer.partnerId))
				.innerJoin(customers).on(partnerCustomer.customerId.eq(customers.id))
				.where(query);

		List<Partners> transporterList =
				q.select(Projections.bean(Partners.class, partners.tenantId, partners.id, partners.createdBy, partners.createdDate, partners.updatedBy
						, partners.updatedDate, partners.hashcode, partners.version, partners.addressContactId, partners.addressId, partners.description,
						partners.partnerCode, partners.partnerName, partners.customerId, partners.phoneCountryShortName, partners.phoneAreaCode
						, partners.email, partners.phone, partners.activeInd))
						.fetch();
		Set<Long> setPartnerIds = transporterList.stream().map(Partners::getId).collect(Collectors.toSet());
		List<Partners> result = transporterList.stream().map(partner -> mapExcel(partner, setPartnerIds, filter.getTenantId()))
				.collect(Collectors.toList());

		setAddrContactToExport(result);
		this.sortByMultiFields(filter.getSort(), result);
		return result;
	}

	private void sortByMultiFields(List<Sort.Order> lstSort, List<Partners> transporterList) {
		Comparator<Partners> comparator = Comparator.comparing(Partners::getPartnerName, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
		if (lstSort.isEmpty()) {
			transporterList.sort(comparator);
			return;
		}
		Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(0).getDirection().isAscending());
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.TRANSPORTER_NAME)) {
			comparator = Comparator.comparing(Partners::getPartnerName, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.TRANSPORTER_CODE)) {
			comparator = Comparator.comparing(Partners::getPartnerCode, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.TRANSPORTER_DEDICATED_TO)) {
			comparator = Comparator.comparing(Partners::getCustomerName, sortOrder);
		}
		if (Objects.equals(1, lstSort.size())) {
			comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
			transporterList.sort(comparator);
			return;
		}
		for (int i = 1; i < lstSort.size(); i++) {
			sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(i).getDirection().isAscending());
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.TRANSPORTER_NAME)) {
				comparator = comparator.thenComparing(Partners::getPartnerName, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.TRANSPORTER_CODE)) {
				comparator = comparator.thenComparing(Partners::getPartnerCode, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.TRANSPORTER_DEDICATED_TO)) {
				comparator = comparator.thenComparing(Partners::getCustomerName, sortOrder);
			}
		}
		comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
		transporterList.sort(comparator);
	}

	private void setAddrContactToExport(List<Partners> partnersList) {

		List<Long> listPartnerIds = new ArrayList<>();
		partnersList.stream().forEach(partners -> {
			listPartnerIds.add(partners.getId());
		});

		if (listPartnerIds.isEmpty()){
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
	public int countByTransporterName(String transporterName)
	{
		return partnersRepository.countByCondition(transporterName, AppConstants.PartnerType.TRANSPORTER);
	}

	@Override
	public ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId, Long currentTenantUserId){
		Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
		Map<Integer, StringBuilder> mapExcelError = new HashMap();
		Set<String> excelEmailErrors = new HashSet<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.TRANSPORTER_SETTING_UPLOAD);
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

		boolean validHeader = excelUtils.validateExcelHeader(TransporterDto.class, workbook, sheet, fileName
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
				validateAndSaveExcelData(currentTenantId, currentTenantUserId, workbook, sheet, fileName, mapExcelError,
						excelEmailErrors, uploadTemplateHdrIdDto, timeStartReadingFile, userInfo, authToken);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return excelResponseMessage;
	}

	public void checkDuplicatedEntries(List<TransporterDto> listTransporterDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
		Map<String, String> mapCheckDup = new HashMap<>();
		for (TransporterDto dto : listTransporterDto) {
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

		for (int itemIndex = 0; itemIndex < listTransporterDto.size(); itemIndex++) {
			TransporterDto dto = listTransporterDto.get(itemIndex);
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
			excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("transporter.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getPartnerName().trim().toLowerCase()), dto.getPartnerName()}));
			excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
			mapExcelError.put(itemIndex, cellErrors);
		}
	}

	public void checkExistedEntries(List<TransporterDto> listTransporterDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors)
	{
		for (int itemIndex = 0; itemIndex < listTransporterDto.size(); itemIndex++) {
			TransporterDto dto = listTransporterDto.get(itemIndex);
			if (StringUtils.isEmpty(dto.getPartnerName())) {
				continue;
			}

			int existedTransporterAmount = countByTransporterName(dto.getPartnerName());
			if (existedTransporterAmount > 0) {
				StringBuilder cellErrors = mapExcelError.get(itemIndex);

				if (cellErrors == null) {
					cellErrors = new StringBuilder();
				}

				excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Transporter Name"}));
				excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));

				mapExcelError.put(itemIndex, cellErrors);
			}
		}
	}

	private void validCountryMobileCodeAndCustomerAndLocation(List<TransporterDto> transporterDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long tenantId, Long tenantUserId, String token) {
		Set<String> setCountryIsdCode = new HashSet();
		Set<String> setCountryFullName = new HashSet();
		Map<String, CountryDTO> countryByIsdCode = new HashMap();
		Map<String, CountryDTO> countryByFullName = new HashMap();
		Set<String> setCustomerName = new HashSet();
		Map<String, Partners> customerByCusName = new HashMap();
		Set<String> setLocName = new HashSet();
		Map<String, Location> locationByLocName = new HashMap();

		List<Partners> customerList = customerService.findByFilter(new PartnersFilter(tenantId, 0, Integer.MAX_VALUE), tenantUserId, true, token);
		Map<String, Boolean> customerNameMap = customerList.stream().collect(Collectors.toMap(Partners::getPartnerName, transporter -> true));

		transporterDtoList.stream().forEach(transporterDto -> {
			if (!StringUtils.isEmpty(transporterDto.getMobileNumber1CountryShortName())){
				setCountryIsdCode.add(transporterDto.getMobileNumber1CountryShortName());
			}

			if (!StringUtils.isEmpty(transporterDto.getCustomerName())){
				setCustomerName.add(transporterDto.getCustomerName().toLowerCase().trim());
			}

			if (!StringUtils.isEmpty(transporterDto.getLocationName())){
				setLocName.add(transporterDto.getLocationName().toLowerCase().trim());
			}

			if (!StringUtils.isEmpty(transporterDto.getCountryFullName())){
				setCountryFullName.add(transporterDto.getCountryFullName());
			}
		});

		if (!setCountryIsdCode.isEmpty()){
			List<CountryDTO> countryDTOList = addrContactClient.findBySetCountryIsdCode(setCountryIsdCode);
			countryByIsdCode = countryDTOList.stream().filter(country -> !StringUtils.isEmpty(country.getCountryIsdCode())).collect(Collectors.toMap(country ->
					country.getCountryIsdCode().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		if (!setCustomerName.isEmpty()) {
			List<Partners> listCustomer = partnersRepository.findByListPartnerNameAndLookupCode(setCustomerName,AppConstants.PartnerType.CUSTOMER,tenantId);
			customerByCusName = listCustomer.stream().filter(customer -> !StringUtils.isEmpty(customer.getPartnerName())).collect(Collectors.toMap(customer ->
					customer.getPartnerName().toLowerCase().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		if (!setLocName.isEmpty()){
			List<Location> listLocation = locationRepository.findByListLocName(tenantId, setLocName);
			locationByLocName = listLocation.stream().filter(location -> !StringUtils.isEmpty(location.getLocName())).collect(Collectors.toMap(location ->
					location.getLocName().toLowerCase().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		if (!setCountryFullName.isEmpty()){
			List<CountryDTO> countryDTOList = addrContactClient.findBySetCountryFullName(setCountryFullName);
			countryByFullName = countryDTOList.stream().filter(country -> !StringUtils.isEmpty(country.getCountryFullName())).collect(Collectors.toMap(country ->
					country.getCountryFullName().trim(), Function.identity(), (existing, replacement) -> existing));
		}

		for (int itemIndex = 0; itemIndex < transporterDtoList.size(); itemIndex++){
			TransporterDto transporterDto = transporterDtoList.get(itemIndex);

			if (!StringUtils.isEmpty(transporterDto.getMobileNumber1CountryShortName())){
				CountryDTO existedCountry = countryByIsdCode.get(transporterDto.getMobileNumber1CountryShortName());

				if (existedCountry == null){
					buildExcelError(mapExcelError,excelEmailErrors,itemIndex
							,messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Country Mobile Code"})
							,messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				} else {
					transporterDto.setMobileNumber1CountryShortName(existedCountry.getCountryShortName());
				}
			}

			if (!StringUtils.isEmpty(transporterDto.getCountryFullName())){
				CountryDTO existedCountry = countryByFullName.get(transporterDto.getCountryFullName());

				if (existedCountry == null){
					buildExcelError(mapExcelError,excelEmailErrors,itemIndex
							,messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Country"})
							,messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				} else {
					transporterDto.setCountryShortName(existedCountry.getCountryShortName());
				}
			}

			if (!StringUtils.isEmpty(transporterDto.getCustomerName())){
				Partners existedCustomer = customerByCusName.get(transporterDto.getCustomerName().toLowerCase().trim());

				if (existedCustomer == null){
					buildExcelError(mapExcelError,excelEmailErrors,itemIndex
							,messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Customer"})
							,messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				} else {
//					transporterDto.setCustomerId(existedCustomer.getId());
				}
			}

			if (!StringUtils.isEmpty(transporterDto.getLocationName())){
				Location existedLocation = locationByLocName.get(transporterDto.getLocationName().toLowerCase().trim());

				if (existedLocation == null){
					buildExcelError(mapExcelError,excelEmailErrors,itemIndex
							,messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Location"})
							,messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
				} else {
					transporterDto.setLocationId(existedLocation.getId());
				}
			}

			if (Objects.isNull(customerNameMap.get(transporterDto.getCustomerName()))) {
				buildExcelError(mapExcelError, excelEmailErrors, itemIndex
						, messagesUtilities.getMessageWithParam("upload.excel.error.data.no.access", new String[]{"Transporter", "customer"})
						, messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
			}
		}
	}

	public void saveExcelData(List<TransporterDto> listTransporterDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId)
	{
		for (int itemIndex = 0; itemIndex < listTransporterDto.size(); itemIndex++) {
			try {
				TransporterDto transporterDto = listTransporterDto.get(itemIndex);
				transporterDto.setTenantId(currentTenantId);
				save(transporterDto);
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
	public void downloadExcel(PartnersFilter filter, HttpServletResponse response, Long currentTenantId, Long tenantUserId){
		List<Partners> transporterList = getTranporterToExport(currentTenantId, tenantUserId, filter);
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.TRANSPORTER_SETTING_EXPORT);
		excelUtils.exportExcel(response, transporterList, Partners.class, template);
	}

	@Override
	public Partners findByName(Long tenantId, String transporterName) {
		List<Partners> transporters = partnersService.findByTenantIdAndPartnerNameAndLookupCode(tenantId, transporterName, AppConstants.PartnerType.TRANSPORTER);

		if (transporters.isEmpty()) {
			return null;
		}
		return transporters.get(0);
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
		List<TransporterDto> listTransporterAfterReadExcel = excelUtils.parseExcelToDto(TransporterDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
		String emailTemplateName = StringUtils.EMPTY;

		checkDuplicatedEntries(listTransporterAfterReadExcel, mapExcelError, excelEmailErrors);
		checkExistedEntries(listTransporterAfterReadExcel, mapExcelError, excelEmailErrors);
		validCountryMobileCodeAndCustomerAndLocation(listTransporterAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId, currentTenantUserId, token);

		if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
			emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
		} else {
			saveExcelData(listTransporterAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId);

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
		excelEmailErrors.forEach(excelEmailErrorsVl::append);

		excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.transporter", null),
				excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);
	}

	@Override
	public Page<DropdownPartnerDto> findByTransporterName(Long tenantId, Long tenantUserId, String term, Pageable pageable) {
		QPartners partners = QPartners.partners;
		BooleanExpression query = partners.isNotNull();
		query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.TRANSPORTER, null);
		return partnersRepository.findByNameAndTypeSelectIdAndName(tenantId, term, AppConstants.PartnerType.TRANSPORTER, pageable, query);
	}
}
