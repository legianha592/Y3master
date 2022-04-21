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
import com.y3technologies.masters.client.CountryClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.BaseDto;
import com.y3technologies.masters.dto.ConsigneeDto;
import com.y3technologies.masters.dto.ConsigneePlaceDto;
import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.dto.PartnersDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrContactDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.AddrContactFilter;
import com.y3technologies.masters.dto.filter.AddressFilter;
import com.y3technologies.masters.dto.filter.LocationFilter;
import com.y3technologies.masters.dto.filter.PartnerLocationFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.dto.filter.PlacesFilter;
import com.y3technologies.masters.dto.table.ExcelConsigneeDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.BaseEntity;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.PartnerTypes;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.QLocation;
import com.y3technologies.masters.model.QPartnerLocation;
import com.y3technologies.masters.model.QPartnerTypes;
import com.y3technologies.masters.model.QPartners;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.model.comm.Country;
import com.y3technologies.masters.repository.CommonTagRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.repository.PartnerLocationRepository;
import com.y3technologies.masters.repository.PartnerTypesRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import com.y3technologies.masters.service.ConsigneeService;
import com.y3technologies.masters.service.CustomerService;
import com.y3technologies.masters.service.LocationService;
import com.y3technologies.masters.service.PartnerLocationService;
import com.y3technologies.masters.service.PartnerTypesService;
import com.y3technologies.masters.service.PartnersService;
import com.y3technologies.masters.service.ProfileScopeService;
import com.y3technologies.masters.util.BeanCopierFactory;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import com.y3technologies.masters.util.SortingUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Sivasankari Subramaniam
 */
@Service
@RequiredArgsConstructor
public class ConsigneeServiceImpl extends BaseServiceImpl implements ConsigneeService {
    private final PartnersRepository partnersRepository;
    private final PartnerTypesService partnerTypesService;
    private final PartnersService partnersService;
    private final LocationService locationService;
    private final CustomerService customerService;
    private final LookupRepository lookupRepository;
    private final PartnerLocationRepository partnerLocationRepository;
    private final PartnerTypesRepository partnerTypesRepository;
    private final AddrClient addrClient;
    private final AddrContactClient addrContactClient;
    private final ExcelClient excelClient;
    private final MessagesUtilities messagesUtilities;
    private final MastersApplicationPropertiesConfig propertiesConfig;
    private final ExcelUtils excelUtils;
    private final CommonTagRepository commonTagRepository;
    private final ProfileScopeService profileScopeService;
    private final ThreadPoolTaskExecutor executor;
    private final AasClient aasClient;
    private final CountryClient countryClient;
    private final PartnerLocationService partnerLocationService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public Partners save(ConsigneeDto consigneeDto) {
        PartnersFilter partnersFilter = new PartnersFilter();
        partnersFilter.setTenantId(consigneeDto.getTenantId());
        partnersFilter.setPartnerCode(consigneeDto.getPartnerCode());
        partnersFilter.setCustomerId(consigneeDto.getCustomerId());
        partnersFilter.setPartnerType(AppConstants.PartnerType.CONSIGNEE);

        partnersFilter.setName(consigneeDto.getPartnerName());
        partnersFilter.setPartnerCode(null);

        List<Partners> existingPartnerNameList = findByFilter(partnersFilter);

        if(!existingPartnerNameList.isEmpty())
            throw new TransactionException("exception.consignee.duplicate");

        Partners partners = mapToPartners(consigneeDto);

        // save
        partnersRepository.save(partners);

        // add partner type for consignee
        Lookup lookup = lookupRepository.findByLookupTypeAndLookupCode(AppConstants.LookupType.PARTNER_TYPES, AppConstants.PartnerType.CONSIGNEE);
        PartnerTypes partnerTypes = new PartnerTypes();
        partnerTypes.setPartners(partners);
        partnerTypes.setLookup(lookup);
        partnerTypesService.save(partnerTypes);

        return partners;
    }

    @Override
    @Transactional
    public Partners update(Long id, ConsigneeDto consigneeDto) {
        Partners partners = partnersRepository.findById(id).orElseThrow(() -> new TransactionException("exception.consignee.invalid"));
        List<PartnerTypes> partnerTypes = partnerTypesRepository.getByPartnerId(id);

        // update status
        partners.setActiveInd(consigneeDto.getActiveInd());
        partnerTypes.forEach(pt -> {
            pt.setActiveInd(consigneeDto.getActiveInd());
        });

        //update customerId
        partners.setCustomerId(consigneeDto.getCustomerId());

        //update consignee code
        partners.setPartnerCode(consigneeDto.getPartnerCode());

        // save
        partners = partnersRepository.save(partners);
        partnerTypesRepository.saveAll(partnerTypes);
        return partners;
    }

    @Override
    @Transactional
    public ConsigneeDto updateStatus(Long id, Boolean isActive) {
        Partners partners = partnersRepository.findById(id).orElseThrow(() -> new TransactionException("exception.consignee.invalid"));
        List<PartnerTypes> partnerTypes = partnerTypesRepository.getByPartnerId(id);

        // update status
        partners.setActiveInd(isActive);
        partnerTypes.forEach(pt -> {
            pt.setActiveInd(isActive);
        });

        // save
        partners = partnersRepository.save(partners);
        partnerTypesRepository.saveAll(partnerTypes);
        return mapToConsigneeDto(partners);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsigneeDto findById(Long id) {
        Partners partners = partnersRepository.findById(id).orElseThrow(() -> new TransactionException("exception.consignee.invalid"));
        ConsigneeDto consigneeDto = null;
        if (partners != null) {
            consigneeDto = mapToConsigneeDto(partners);
        }
        return consigneeDto;
    }

    @Override
    public Page<ConsigneeDto> findBySearch(PartnersFilter filter, Long tenantUserId) {
        if (filter == null || filter.getPageNo() < 0 || filter.getPageSize() < 1)
            return Page.empty();

        QPartners partners = QPartners.partners;
        QPartners customers = new QPartners("customers");
        QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
        BooleanExpression query = partners.isNotNull();

        query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.CONSIGNEE, null);

        if (null != filter.getTenantId()) {
            query = query.and(partners.tenantId.eq(filter.getTenantId()));
        }

        if (StringUtils.isNotBlank(filter.getCustomerName())) {
            List<Partners> customerList = customerService.findByFilter(
                    new PartnersFilter(filter.getTenantId(), filter.getCustomerName(), AppConstants.PartnerType.CUSTOMER), tenantUserId, false, null);

            List<Long> customerIds = customerList.stream().map(d -> d.getId()).collect(Collectors.toList());
            query = query.and(partners.customerId.in(customerIds));
        }

        if (StringUtils.isNotBlank(filter.getName())) {
            String keyword = filter.getName().trim();
            query = query.and(partners.partnerName.like("%" + keyword + "%"));
        }

        if (StringUtils.isNotBlank(filter.getPartnerCode())) {
            String keyword = filter.getPartnerCode().trim();
            query = query.and(partners.partnerCode.like("%" + keyword + "%"));
        }

        if (StringUtils.isNoneBlank(filter.getPartnerType())) {
            query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
                    .where(partnerTypes.lookup.lookupCode.eq(filter.getPartnerType()))));
        }

        JPQLQuery<Partners> q = new JPAQuery<>(entityManager);
        q.from(partners).leftJoin(customers).on(partners.customerId.eq(customers.id)).where(query);

        Pageable pageable = PageRequest.of(filter.getPageNo(), filter.getPageSize());

        //Set orderBy to query
        if (StringUtils.isNotEmpty(filter.getSortBy())){
            if (filter.getSortBy().contains("name")) {
                JPQLQuery jpqlQuery = filter.getSortBy().toLowerCase().contains(",asc") ? q.orderBy(partners.partnerName.asc()).orderBy(partners.id.desc()): q.orderBy(partners.partnerName.desc()).orderBy(partners.id.desc());
            } else if (filter.getSortBy().contains("customerName")){
                JPQLQuery jpqlQuery = filter.getSortBy().toLowerCase().contains(",asc") ? q.orderBy(customers.partnerName.asc()).orderBy(partners.id.desc()): q.orderBy(customers.partnerName.desc()).orderBy(partners.id.desc());
            } else if (filter.getSortBy().contains("partnerCode")){
                JPQLQuery jpqlQuery = filter.getSortBy().toLowerCase().contains(",asc") ? q.orderBy(partners.partnerCode.asc()).orderBy(partners.id.desc()): q.orderBy(partners.partnerCode.desc()).orderBy(partners.id.desc());
            }
        } else {
            q.orderBy(partners.partnerCode.asc()).orderBy(partners.id.desc());
        }
        //query without paging to get total element
        List<ConsigneeDto> consigneeDtoList =
                q.select(Projections.bean(ConsigneeDto.class,
                        partners.tenantId, partners.id, partners.activeInd, partners.createdBy, partners.createdDate, partners.updatedBy, partners.updatedDate, partners.version,
                        partners.partnerCode,partners.partnerName,partners.customerId,customers.partnerName.as("customerName")))
                        .fetch();

        //query with paging
        List<ConsigneeDto> consigneeDtoListPaging =
                q.select(Projections.bean(ConsigneeDto.class,
                        partners.tenantId, partners.id, partners.activeInd, partners.createdBy, partners.createdDate, partners.updatedBy, partners.updatedDate, partners.version,
                        partners.partnerCode,partners.partnerName,partners.customerId,customers.partnerName.as("customerName")))
                        .offset(pageable.getOffset()).limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(consigneeDtoListPaging, PageRequest.of(filter.getPageNo(), filter.getPageSize()), consigneeDtoList.size());
    }

    @Override
    @Transactional
    public Long savePlace(ConsigneePlaceDto consigneePlaceDto) {
        Partners partners = partnersRepository.findById(consigneePlaceDto.getConsigneeId()).orElseThrow(() -> new TransactionException("exception.consignee.invalid"));

        // save or update addr
        AddrDTO addrDTO = saveOrUpdateAddr(consigneePlaceDto.getPartnerAddr());
        // save or update addr contact
        AddrContact addrContact = saveOrUpdateAddrContact(consigneePlaceDto.getPartnerAddrContact());

        // save partner location
        PartnerLocation partnerLocation = new PartnerLocation();
        partnerLocation.setAddressContactId(addrContact.getId());
        partnerLocation.setPartners(partners);
        partnerLocation.setAddressId(addrDTO.getId());
        partnerLocation.setLocationId(consigneePlaceDto.getLocationId());
        partnerLocationRepository.save(partnerLocation);

        return partnerLocation.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsigneePlaceDto> findByPlacesSearch(PlacesFilter placesFilter, Long tenantUserId) {
        if (placesFilter == null || placesFilter.getPageNo() < 0 || placesFilter.getPageSize() < 1)
            return Page.empty();

        QPartnerLocation qPartnerLocation = QPartnerLocation.partnerLocation;
        QLocation qLocation = QLocation.location;
        QPartners qPartners = QPartners.partners;
        BooleanExpression query = qPartnerLocation.isNotNull();

        query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.CONSIGNEE_PLACE, null);

        // get addr contact
        List<AddrContact> addrContactList = new ArrayList<>();
        if (StringUtils.isNotBlank(placesFilter.getMobileNumber1()) || StringUtils.isNotBlank(placesFilter.getPerson()) || StringUtils.isNotBlank(placesFilter.getEmail())) {
            addrContactList = addrContactClient
                    .findByFilter(new AddrContactFilter(placesFilter.getMobileNumber1(), placesFilter.getPerson(), placesFilter.getEmail()));
            List<Long> addrContactIdList = addrContactList.stream().map(d -> d.getId()).collect(Collectors.toList());
            query = query.and(qPartnerLocation.addressContactId.in(addrContactIdList));
        }

        // get addr
        List<AddrDTO> addrList = new ArrayList<>();
        if (StringUtils.isNotBlank(placesFilter.getUnit()) || StringUtils.isNotBlank(placesFilter.getStreet())
                || StringUtils.isNotBlank(placesFilter.getStreet2()) || StringUtils.isNotBlank(placesFilter.getCity())
                || StringUtils.isNotBlank(placesFilter.getState()) || StringUtils.isNotBlank(placesFilter.getZipCode()) || StringUtils.isNotBlank(placesFilter.getCountryShortName())) {
            addrList = addrClient
                    .findByFilter(new AddressFilter(placesFilter.getUnit(), placesFilter.getStreet(), placesFilter.getStreet2(),
                            placesFilter.getCity(), placesFilter.getState(), placesFilter.getZipCode(), placesFilter.getCountryShortName()));
            List<Long> addrIdList = addrList.stream().map(d -> d.getId()).collect(Collectors.toList());
            query = query.and(qPartnerLocation.addressId.in(addrIdList));
        }

        // query conditions
        if (StringUtils.isNotBlank(placesFilter.getLocCode())) {
            Set<Long> locationIdList = locationService.findAllByLocCode(placesFilter.getLocCode());
            if (CollectionUtils.isNotEmpty(locationIdList)) {
                query = query.and(qPartnerLocation.locationId.in(locationIdList));
            }

        }
        if (null != placesFilter.getConsigneeId()) {
            query = query.and(qPartnerLocation.partners.id.eq(placesFilter.getConsigneeId()));
        }
        if (null != placesFilter.getLocationId()) {
            query = query.and(qPartnerLocation.locationId.eq(placesFilter.getLocationId()));
        }

        // partner location results
        Page<PartnerLocation> partnerLocationPage = partnerLocationRepository.findAll(query, PageRequest.of(placesFilter.getPageNo(), placesFilter.getPageSize()));
        List<PartnerLocation> partnerLocations = partnerLocationPage.getContent();

        List<Long> locationIds = partnerLocations.stream().map(PartnerLocation::getLocationId).collect(Collectors.toList());
        List<Long> addressIds = partnerLocations.stream().map(PartnerLocation::getAddressId).collect(Collectors.toList());
        List<Long> addressContactIds = partnerLocations.stream().map(PartnerLocation::getAddressContactId).collect(Collectors.toList());

        List<Location> locations = locationService.findByIds(locationIds);

        List<AddrDTO> addrDTOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(addressIds)) {
            addrDTOs = addrClient.postFindByFilter(new AddressFilter(addressIds));
        }

        List<AddrContact> addrContacts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(addressContactIds)) {
            addrContacts = addrContactClient.postFindByFilter(new AddrContactFilter(addressContactIds));
        }

        Map<Long, Location> mapLocation = locations.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        Map<Long, AddrDTO> mapAddrDTO = addrDTOs.stream().collect(Collectors.toMap(BaseDto::getId, Function.identity()));
        Map<Long, AddrContact> mapAddrContact = addrContacts.stream().collect(Collectors.toMap(AddrContact::getId, Function.identity()));

        List<ConsigneePlaceDto> consigneePlaceDTOs = partnerLocations.stream().map(partnerLocation -> {
            PartnerAddrContactDTO partnerAddrContactDTO = new PartnerAddrContactDTO();
            PartnerAddrDTO partnerAddrDTO = new PartnerAddrDTO();

            AddrContact addrContact = mapAddrContact.get(partnerLocation.getAddressContactId());
            if (Objects.nonNull(addrContact)) {
                BeanCopierFactory.AddrContact_PartnerAddrContactDto_copier.copy(addrContact, partnerAddrContactDTO, null);
            }

            AddrDTO addr = mapAddrDTO.get(partnerLocation.getAddressId());
            if (Objects.nonNull(addr)) {
                BeanCopierFactory.AddrDto_PartnerAddrDto_copier.copy(addr, partnerAddrDTO, null);
            }

            Location location = mapLocation.get(partnerLocation.getLocationId());

            return mapToConsigneePlaceDto(partnerLocation, location, partnerLocation.getPartners().getId(), partnerAddrContactDTO, partnerAddrDTO);
        }).collect(Collectors.toList());

        return new PageImpl<>(consigneePlaceDTOs, partnerLocationPage.getPageable(), partnerLocationPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsigneePlaceDto findByPlaceId(Long id) {
        PartnerLocation partnerLocation = partnerLocationRepository.findById(id).orElseThrow(() -> new TransactionException("exception.consignee.invalid"));
        ConsigneePlaceDto consigneePlaceDto = new ConsigneePlaceDto();
        if (partnerLocation != null) {
            return mapToConsigneePlaceDto(partnerLocation, retrieveLocation(partnerLocation.getLocationId()), partnerLocation.getPartners().getId(), retrieveAddressContact(partnerLocation.getAddressContactId()), retrieveAddress(partnerLocation.getAddressId()));
        }
        return null;
    }

    @Override
    @Transactional
    public Long updatePlace(ConsigneePlaceDto consigneePlaceDto) {
        PartnerLocation partnerLocation = partnerLocationRepository.findById(consigneePlaceDto.getId()).orElseThrow(() -> new TransactionException("exception.consignee.place.invalid"));

        // save or update addr
        AddrDTO addrDTO = saveOrUpdateAddr(consigneePlaceDto.getPartnerAddr());
        // save or update addr contact
        AddrContact addrContact = saveOrUpdateAddrContact(consigneePlaceDto.getPartnerAddrContact());

        // save partner location
        partnerLocation.setAddressContactId(addrContact.getId());
        partnerLocation.setAddressId(addrDTO.getId());
        partnerLocation.setLocationId(consigneePlaceDto.getLocationId());
        partnerLocationRepository.save(partnerLocation);

        return partnerLocation.getId();
    }

    @Override
    @Transactional
    public Long updatePlaceStatus(Long id, Boolean isActive) {
        PartnerLocation partnerLocation = partnerLocationRepository.findById(id).orElseThrow(() -> new TransactionException("exception.consignee.invalid"));

        // update status
        partnerLocation.setActiveInd(isActive);

        // save
        partnerLocationRepository.save(partnerLocation);
        return partnerLocation.getPartners().getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsigneeDto> findConsigneeByCustomerId(Long id, Long tenantId) {
        PartnersFilter partnersFilter = new PartnersFilter();
        partnersFilter.setCustomerId(id);
        partnersFilter.setTenantId(tenantId);
        partnersFilter.setPartnerType(AppConstants.PartnerType.CONSIGNEE);
        List<Partners> partners = partnersService.findByFilter(partnersFilter);
        List<ConsigneeDto> consigneeDtoArrayList = new ArrayList<>();
        partners.forEach(p -> {
            consigneeDtoArrayList.add(mapToConsigneeDto(p));
        });
        return consigneeDtoArrayList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDto> findLocationsByConsigneeId(PartnerLocationFilter partnerLocationFilter, Boolean isSearchDropdown, Long tenantUserId, boolean isCallFromController) {
        Partners partners = partnersRepository.findById(partnerLocationFilter.getConsigneeId()).orElseThrow(() -> new TransactionException("exception.consignee.invalid"));

        QPartnerLocation qPartnerLocation = QPartnerLocation.partnerLocation;
        BooleanExpression query = qPartnerLocation.isNotNull();

        if (isCallFromController) {
            query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.CONSIGNEE_PLACE, null);
        }

        query = query.and(qPartnerLocation.partners.id.eq(partners.getId())).and(qPartnerLocation.activeInd.eq(Boolean.TRUE));

        if(ObjectUtils.allNotNull(partnerLocationFilter.getLocationId())) {
            query = query.and(qPartnerLocation.locationId.eq(partnerLocationFilter.getLocationId()));
        }

        List<PartnerLocation> partnerLocationList = partnerLocationRepository.findAll(query);
        List<LocationDto> locationDtoList = new ArrayList<>();
        if((CollectionUtils.isNotEmpty(partnerLocationList) && !isSearchDropdown) || isSearchDropdown) {
            List<LocationDto> finalLocationDtoList = locationDtoList;
            partnerLocationList.stream().filter(distinctByKey(PartnerLocation::getLocationId)).forEach(pl -> {
                if(ObjectUtils.allNotNull(pl.getLocationId())) {
                    finalLocationDtoList.add(mapToLocationDto(locationService.findById(pl.getLocationId())));
                }
            });
            locationDtoList = finalLocationDtoList;
        } else {
            locationDtoList = locationService.findAllByTenantId(partnerLocationFilter.getTenantId());
        }
        return locationDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsigneePlaceDto> findPlacesByLocationId(PartnerLocationFilter partnerLocationFilter) {
        List<ConsigneePlaceDto> consigneePlaceDtoList = new ArrayList<>();
        if(null == partnerLocationFilter.getConsigneeId()) {
            Location location= locationService.findById(partnerLocationFilter.getLocationId());
            if(ObjectUtils.allNotNull(location.getAddressId())) {
                ConsigneePlaceDto consigneePlaceDto = new ConsigneePlaceDto();
                consigneePlaceDto.setId(location.getId());
                consigneePlaceDto.setLocationId(location.getId());
                consigneePlaceDto.setPartnerAddrContact(retrieveAddressContact(location.getAddressContactId()));
                consigneePlaceDto.setPartnerAddr(retrieveAddress(location.getAddressId()));
                consigneePlaceDtoList.add(consigneePlaceDto);
            }
        } else {
            Partners partners = partnersRepository.findById(partnerLocationFilter.getConsigneeId()).orElseThrow(() -> new TransactionException("exception.consignee.invalid"));

            QPartnerLocation qPartnerLocation = QPartnerLocation.partnerLocation;
            BooleanExpression query = qPartnerLocation.isNotNull();

            query = query.and(qPartnerLocation.partners.id.eq(partners.getId()));
            query = query.and(qPartnerLocation.activeInd.eq(Boolean.TRUE));

            if(ObjectUtils.allNotNull(partnerLocationFilter.getLocationId())) {
                query = query.and(qPartnerLocation.locationId.eq(partnerLocationFilter.getLocationId()));
            }
            if(ObjectUtils.allNotNull(partnerLocationFilter.getConsigneeId())) {
                query = query.and(qPartnerLocation.partners.id.eq(partnerLocationFilter.getConsigneeId()));
            }

            List<PartnerLocation> partnerLocationList = partnerLocationRepository.findAll(query);


            partnerLocationList.forEach(pl -> {
                if(ObjectUtils.allNotNull(pl.getAddressId())) {
                    ConsigneePlaceDto consigneePlaceDto = new ConsigneePlaceDto();
                    consigneePlaceDto.setId(pl.getId());
                    consigneePlaceDto.setLocationId(pl.getLocationId());
                    consigneePlaceDto.setConsigneeId(partnerLocationFilter.getConsigneeId());
                    consigneePlaceDto.setPartnerAddrContact(retrieveAddressContact(pl.getAddressContactId()));
                    consigneePlaceDto.setPartnerAddr(retrieveAddress(pl.getAddressId()));
                    consigneePlaceDtoList.add(consigneePlaceDto);
                }
            });
        }
        return consigneePlaceDtoList;
    }

    private Location retrieveLocation(Long locationId) {
        Location existingLocation = null;
        if (null != locationId) {
            existingLocation = locationService.findById(locationId);
            if (ObjectUtils.allNotNull(existingLocation)) {
                existingLocation.getId();
            } else {
                throw new TransactionException("exception.consignee.place.invalid");
            }
        }
        return existingLocation;
    }

    private PartnerAddrContactDTO retrieveAddressContact(Long addressContactId) {
        PartnerAddrContactDTO partnerAddrContactDTO = null;
        if (null != addressContactId) {
            AddrContact existingAddrContact = Optional.ofNullable(addrContactClient.getAddressContact(addressContactId)).orElse(null);
            if (ObjectUtils.allNotNull(existingAddrContact)) {
                partnerAddrContactDTO = new PartnerAddrContactDTO();
                BeanCopierFactory.AddrContact_PartnerAddrContactDto_copier.copy(existingAddrContact, partnerAddrContactDTO, null);
            }
        }
        return partnerAddrContactDTO;
    }

    private PartnerAddrDTO retrieveAddress(Long addressId) {
        PartnerAddrDTO partnerAddrDTO = null;
        if (null != addressId) {
            AddrDTO existingAddr = Optional.ofNullable(addrClient.getAddress(addressId)).orElse(null);
            if (ObjectUtils.allNotNull(existingAddr)) {
                partnerAddrDTO = new PartnerAddrDTO();
                BeanCopierFactory.AddrDto_PartnerAddrDto_copier.copy(existingAddr, partnerAddrDTO, null);
            }
        }
        return partnerAddrDTO;
    }

    @Override
    public List<Partners> findByFilter(PartnersFilter filter) {

        QPartners partners = QPartners.partners;
        QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
        BooleanExpression query = partners.isNotNull();

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
                .where(partnerTypes.lookup.lookupCode.eq(AppConstants.PartnerType.CONSIGNEE))));

        if (CollectionUtils.isNotEmpty(filter.getPartnerIdList())) {
            query = query.and(partners.id.in(filter.getPartnerIdList()));
        }

        if(filter.getActiveInd() != null){
            query = query.and(partners.activeInd.eq(filter.getActiveInd()));
        }

        return partnersRepository.findAll(query);
    }

    @Override
    public Long createOrFind(ConsigneeDto consigneeDto) {
        QPartners qPartners = QPartners.partners;
        QPartnerTypes qPartnerTypes = QPartnerTypes.partnerTypes;
        BooleanExpression query = qPartners.isNotNull();

        if (consigneeDto.getTenantId() != null) {
            query = query.and(qPartners.tenantId.eq(consigneeDto.getTenantId()));
        }
        if (consigneeDto.getCustomerId() != null) {
            query = query.and(qPartners.customerId.eq(consigneeDto.getCustomerId()));
        }
        if (StringUtils.isNotBlank(consigneeDto.getPartnerCode())) {
            query = query.and(qPartners.partnerCode.eq(consigneeDto.getPartnerCode()));
        }

        Partners partners = partnersRepository.findOne(query).orElseGet(() -> save(consigneeDto));
        if (partners != null) {
            return partners.getId();
        }
        return null;
    }

    private Partners mapToPartners(ConsigneeDto consigneeDto) {
        Partners partners = new Partners();
        BeanCopierFactory.ConsigneeDto_Partners_copier.copy(consigneeDto, partners, null);
        return partners;
    }

    private ConsigneeDto mapToConsigneeDto(Partners partners) {
        ConsigneeDto consigneeDto = new ConsigneeDto();
        BeanCopierFactory.Partners_ConsigneeDto_copier.copy(partners, consigneeDto, null);
        return consigneeDto;
    }

    private LocationDto mapToLocationDto(Location location) {
        LocationDto locationDto = new LocationDto();
        BeanCopierFactory.Location_LocationDto_copier.copy(location, locationDto, null);
        locationDto.setLocationTagId(location.getLocationTag());
        if(StringUtils.isNotEmpty(location.getLocationTag())) {
            String [] arrayOfIds = location.getLocationTag().split("\\|");
            List<CommonTag> commonTagList = commonTagRepository.findAllById((Arrays.stream(arrayOfIds).map(i -> {
                return Long.parseLong(i);
            }).collect(Collectors.toList())));
            if(CollectionUtils.isNotEmpty(commonTagList)) {
                locationDto.setLocationTags(commonTagList);
            }
        }
        return locationDto;
    }

    private ConsigneePlaceDto mapToConsigneePlaceDto(PartnerLocation partnerLocation, Location location, Long consigneeId, PartnerAddrContactDTO partnerAddrContactDTO, PartnerAddrDTO partnerAddrDTO) {
        ConsigneePlaceDto consigneePlaceDto = new ConsigneePlaceDto();
        if (Objects.nonNull(partnerLocation)) {
            consigneePlaceDto.setId(partnerLocation.getId());
            consigneePlaceDto.setLocationId(partnerLocation.getLocationId());
            consigneePlaceDto.setActiveInd(partnerLocation.getActiveInd());
            consigneePlaceDto.setVersion(partnerLocation.getVersion());
        }
        if (Objects.nonNull(location)) {
            consigneePlaceDto.setLocCode(location.getLocCode());
            consigneePlaceDto.setLocName(location.getLocName());
        }
        if (Objects.nonNull(partnerAddrContactDTO)) {
            consigneePlaceDto.setPartnerAddrContact(partnerAddrContactDTO);
        }
        if (Objects.nonNull(consigneeId)) {
            consigneePlaceDto.setConsigneeId(consigneeId);
        }
        if (Objects.nonNull(partnerAddrDTO)) {
            consigneePlaceDto.setPartnerAddr(partnerAddrDTO);
        }
        return consigneePlaceDto;
    }

    private AddrDTO saveOrUpdateAddr(PartnerAddrDTO partnerAddrDTO) {
        UpdateAddrDTO updateAddrDTO = new UpdateAddrDTO();
        BeanCopierFactory.PartnerAddrDTO_UpdateAddrDto_copier.copy(partnerAddrDTO, updateAddrDTO, null);
        return addrClient.createOrUpdateAddress(updateAddrDTO);
    }

    private AddrContact saveOrUpdateAddrContact(PartnerAddrContactDTO partnerAddrContactDTO) {
        AddrContactDTO addrContactDTO = new AddrContactDTO();
        BeanCopierFactory.PartnerAddrContactDTO_UpdateAddrContactDto_copier.copy(partnerAddrContactDTO, addrContactDTO, null);
        return addrContactClient.createOrUpdateAddressContact(addrContactDTO);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public List<Partners> getConsigneeToExport(Long tenantId, Long tenantUserId, PartnersFilter filter) {

        QPartners partners = QPartners.partners;
        QPartners customers = new QPartners("customers");
        QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
        BooleanExpression query = partners.isNotNull();

        if (tenantId != null) {
            query = query.and(partners.tenantId.eq(tenantId));
            query = profileScopeService.getQueryFromProfileScope(tenantUserId, query, AppConstants.ProfileScopeSettings.CONSIGNEE, null);
        }

        if (StringUtils.isNotBlank(filter.getCustomerName())) {
            List<Partners> customerList = customerService.findByFilter(
                    new PartnersFilter(filter.getTenantId(), filter.getCustomerName(), AppConstants.PartnerType.CUSTOMER), tenantUserId, false, null);

            List<Long> customerIds = customerList.stream().map(d -> d.getId()).collect(Collectors.toList());
            query = query.and(partners.customerId.in(customerIds));
        }

        if (StringUtils.isNotBlank(filter.getName())) {
            String keyword = filter.getName().trim();
            query = query.and(partners.partnerName.like("%" + keyword + "%"));
        }

        if (StringUtils.isNotBlank(filter.getPartnerCode())) {
            String keyword = filter.getPartnerCode().trim();
            query = query.and(partners.partnerCode.like("%" + keyword + "%"));
        }

        if (StringUtils.isNotBlank(filter.getPartnerType())) {
            query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
                    .where(partnerTypes.lookup.lookupCode.eq(filter.getPartnerType()))));
        }

        query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
                .where(partnerTypes.lookup.lookupCode.eq(AppConstants.PartnerType.CONSIGNEE))));

        JPQLQuery<Partners> q = new JPAQuery<>(entityManager);
        q.from(partners).leftJoin(customers).on(partners.customerId.eq(customers.id)).where(query);

        List<Partners> partnersList =
                q.select(Projections.bean(Partners.class, partners.tenantId, partners.id, partners.createdBy, partners.createdDate, partners.updatedBy
                        , partners.updatedDate, partners.hashcode, partners.version, partners.addressContactId, partners.addressId, partners.description,
                        partners.partnerCode, partners.partnerName, partners.customerId, partners.phoneCountryShortName, partners.phoneAreaCode
                        , partners.email, partners.phone,
                        customers.partnerName.as("customerName"), partners.activeInd))
                        .fetch();

        return partnersList;
    }

    private void sortByMultiFields(List<Sort.Order> lstSort, List<ExcelConsigneeDto> consigneeList) {
        Comparator<ExcelConsigneeDto> comparator = Comparator.comparing(ExcelConsigneeDto::getPartnerName, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
        if (lstSort.isEmpty()) {
            consigneeList.sort(comparator);
            return;
        }
        Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(0).getDirection().isAscending());
        if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.CONSIGNEE_NAME)) {
            comparator = Comparator.comparing(ExcelConsigneeDto::getPartnerName, sortOrder);
        }
        if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.CONSIGNEE_CODE)) {
            comparator = Comparator.comparing(ExcelConsigneeDto::getPartnerCode, sortOrder);
        }
        if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.CONSIGNEE_ASSIGNED_TO)) {
            comparator = Comparator.comparing(ExcelConsigneeDto::getCustomerName, sortOrder);
        }
        if (Objects.equals(1, lstSort.size())) {
            comparator = SortingUtils.addDefaultSortExtraBaseDto(comparator);
            consigneeList.sort(comparator);
            return;
        }
        for (int i = 1; i < lstSort.size(); i++) {
            sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(i).getDirection().isAscending());
            if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.CONSIGNEE_NAME)) {
                comparator = comparator.thenComparing(ExcelConsigneeDto::getPartnerName, sortOrder);
            }
            if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.CONSIGNEE_CODE)) {
                comparator = comparator.thenComparing(ExcelConsigneeDto::getPartnerCode, sortOrder);
            }
            if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.CONSIGNEE_ASSIGNED_TO)) {
                comparator = comparator.thenComparing(ExcelConsigneeDto::getCustomerName, sortOrder);
            }
        }
        comparator = SortingUtils.addDefaultSortExtraBaseDto(comparator);
        consigneeList.sort(comparator);
    }

    @Override
    public int countByConsigneeName(String consigneeName, Long tenantId, Long customerId) {
        return partnersRepository.countByPartnerNameAndTenantIdAnAndCustomerId(consigneeName, AppConstants.PartnerType.CONSIGNEE, tenantId, customerId );
    }

    @Override
    public ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId, Long currentTenantUserId){
        Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
        Map<Integer, StringBuilder> mapExcelError = new HashMap<>();
        Set<String> excelEmailErrors = new HashSet<>();
        UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_UPLOAD);
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

        boolean validHeader = excelUtils.validateExcelHeader(ConsigneeDto.class, workbook, sheet, fileName
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
                validateAndSaveExcelData(currentTenantId, workbook, sheet, fileName, mapExcelError, excelEmailErrors, uploadTemplateHdrIdDto, timeStartReadingFile, userInfo, authToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return excelResponseMessage;
    }

    public void checkDuplicatedEntries(List<ExcelConsigneeDto> consigneeDtoList, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors) {
        Map<String, String> mapCheckDup = new HashMap<>();
        for (ExcelConsigneeDto dto : consigneeDtoList) {
            if (StringUtils.isEmpty(dto.getPartnerName())) {
                continue;
            }

            String listPositionDup = StringUtils.isNotEmpty(dto.getLocationName())
                    ? mapCheckDup.get(dto.getPartnerName().trim().toLowerCase() + "||" + dto.getLocationName().trim().toLowerCase())
                    : mapCheckDup.get(dto.getPartnerName().trim().toLowerCase());
            if (StringUtils.isEmpty(listPositionDup)) {
                listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
            } else {
                listPositionDup += ", " + dto.getExcelRowPosition();
            }
            if (StringUtils.isNotEmpty(dto.getLocationName())) {
                mapCheckDup.put(dto.getPartnerName().trim().toLowerCase() + "||" + dto.getLocationName().trim().toLowerCase(), listPositionDup);
            } else {
                mapCheckDup.put(dto.getPartnerName().trim().toLowerCase(), listPositionDup);
            }
        }

        for (int itemIndex = 0; itemIndex < consigneeDtoList.size(); itemIndex++) {
            ExcelConsigneeDto dto = consigneeDtoList.get(itemIndex);
            if (StringUtils.isEmpty(dto.getPartnerName())) {
                continue;
            }
            String listPositionDup = StringUtils.isNotEmpty(dto.getLocationName())
                    ? mapCheckDup.get(dto.getPartnerName().trim().toLowerCase() + "||" + dto.getLocationName().trim().toLowerCase())
                    : mapCheckDup.get(dto.getPartnerName().trim().toLowerCase());
            if (!listPositionDup.contains(",")) {
                continue;
            }
            if (StringUtils.isNotEmpty(dto.getLocationName())) {
                buildErrors(mapExcelError, excelEmailErrors, itemIndex, "consignee.excel.duplicate.line",
                        new String[]{mapCheckDup.get(dto.getPartnerName().trim().toLowerCase() + "||" + dto.getLocationName().trim().toLowerCase())
                                , dto.getLocationName()},"upload.excel.email.duplicate.entries", null);
            } else {
                buildErrors(mapExcelError, excelEmailErrors, itemIndex, "consignee.excel.duplicate.line",
                        new String[]{mapCheckDup.get(dto.getPartnerName().trim().toLowerCase()), dto.getPartnerName()},"upload.excel.email.duplicate.entries", null);
            }
        }
    }

    public void checkExistedEntriesAndMatchingLocationInfo(List<ExcelConsigneeDto> listConsigneeDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId) {
        for (int itemIndex = 0; itemIndex < listConsigneeDto.size(); itemIndex++) {
            ExcelConsigneeDto dto = listConsigneeDto.get(itemIndex);
            if (StringUtils.isEmpty(dto.getPartnerName())) {
                continue;
            }

            if (StringUtils.isEmpty(dto.getLocationName())) {
                checkExistConsignee(mapExcelError, excelEmailErrors, currentTenantId, itemIndex, dto);
            } else {
                //check location data matching
                LocationDto location = getLocationByTenantIdAndLocName(dto.getLocationName(), currentTenantId);
                if (ObjectUtils.isEmpty(location)) {
                    if (ObjectUtils.isEmpty(dto.getLocationName()) || ObjectUtils.isEmpty(dto.getCountry()) ||
                            ObjectUtils.isEmpty(dto.getPostalCode()) || ObjectUtils.isEmpty(dto.getUnitNo()) ||
                            ObjectUtils.isEmpty(dto.getAddressLine1()) || ObjectUtils.isEmpty(dto.getName()) ||
                            ObjectUtils.isEmpty(dto.getMobileCountryNo()) || ObjectUtils.isEmpty(dto.getPlacesStatus())
                    ) {
                        buildErrors(mapExcelError, excelEmailErrors, itemIndex, "upload.excel.consignee.place.is.mandatory",
                                null ,"upload.excel.error.data.invalid",new String[]{"Location's information"});
                    }
                } else {
                    if (!isMatchingLocation(dto, location)) {
                        buildErrors(mapExcelError, excelEmailErrors, itemIndex, "upload.excel.location.information.not.match",
                                null ,"upload.excel.error.data.invalid", new String[]{"Location's information"});
                    } else {
                        if (ObjectUtils.isEmpty(dto.getLocationName()) || ObjectUtils.isEmpty(dto.getCountry()) ||
                        ObjectUtils.isEmpty(dto.getPostalCode()) || ObjectUtils.isEmpty(dto.getUnitNo()) ||
                        ObjectUtils.isEmpty(dto.getAddressLine1()) || ObjectUtils.isEmpty(dto.getName()) ||
                        ObjectUtils.isEmpty(dto.getMobileCountryNo()) || ObjectUtils.isEmpty(dto.getPlacesStatus())
                        ) {
                            buildErrors(mapExcelError, excelEmailErrors, itemIndex, "upload.excel.consignee.place.is.mandatory",
                                    null,"upload.excel.error.data.invalid", new String[]{"Location's information"});
                        }
                        Partners consigneeFound = partnersRepository.getByPartnerNameAndTenantIdAndCustomerId(dto.getPartnerName(),
                                AppConstants.PartnerType.CONSIGNEE, currentTenantId, dto.getCustomerId());
                        if (Objects.nonNull(consigneeFound)) {
                            if (existConsigneePlace(consigneeFound.getId(), location.getId(), currentTenantId)) {
                                buildErrors(mapExcelError, excelEmailErrors, itemIndex, "upload.excel.consignee.place.already.exists",
                                        null,"upload.excel.error.data.invalid", new String[]{"Location's information"});
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean existConsigneePlace(Long consigneeId, Long locationId, Long currentTenantId) {
        PartnerLocationFilter filter = new PartnerLocationFilter();
        filter.setConsigneeId(consigneeId);
        filter.setTenantId(currentTenantId);
        filter.setLocationId(locationId);
        List<ConsigneePlaceDto> placesByLocation = findPlacesByLocationId(filter);
        return !placesByLocation.isEmpty();
    }


    private boolean isMatchingLocation(ExcelConsigneeDto dto, LocationDto location) {

        Country country = countryClient.getCountryByShortName(location.getCountryShortName());
        if (Objects.isNull(country)) {
            return false;
        }
        if ((StringUtils.isNotEmpty(dto.getLocationName()) && !dto.getLocationName().equals(location.getLocName()))
                || (StringUtils.isNotEmpty(location.getLocName()) && !location.getLocName().equals(dto.getLocationName()))
        ) {
            return false;
        }
        if ((StringUtils.isNotEmpty(dto.getCountry()) && !dto.getCountry().equals(country.getCountryFullName()))
                || (StringUtils.isNotEmpty(country.getCountryFullName()) && !country.getCountryFullName().equals(dto.getCountry()))
        ) {
            return false;
        }
        if ((StringUtils.isNotEmpty(dto.getState()) && !dto.getState().equals(location.getState()))
                || (StringUtils.isNotEmpty(location.getState()) && !location.getState().equals(dto.getState()))
        ) {
            return false;
        }
        if ((StringUtils.isNotEmpty(dto.getCity()) && !dto.getCity().equals(location.getCity()))
                || (StringUtils.isNotEmpty(location.getCity()) && !location.getCity().equals(dto.getCity()))
        ) {
            return false;
        }
        if ((StringUtils.isNotEmpty(dto.getPostalCode()) && !dto.getPostalCode().equals(location.getZipCode()))
                || (StringUtils.isNotEmpty(location.getZipCode()) && !location.getZipCode().equals(dto.getPostalCode()))
        ) {
            return false;
        }
        if ((StringUtils.isNotEmpty(dto.getAddressLine1()) && !dto.getAddressLine1().equals(location.getStreet()))
                || (StringUtils.isNotEmpty(location.getStreet()) && !location.getStreet().equals(dto.getAddressLine1()))
        ) {
            return false;
        }
        if ((StringUtils.isNotEmpty(dto.getAddressLine2()) && !dto.getAddressLine2().equals(location.getStreet2()))
                || (StringUtils.isNotEmpty(location.getStreet2()) && !location.getStreet2().equals(dto.getAddressLine2()))
        ) {
            return false;
        }
        return true;
    }

    private LocationDto getLocationByTenantIdAndLocName(String locationName, Long currentTenantId) {
        return locationService.findLocationByName(currentTenantId, locationName);
    }

    private void checkExistConsignee(Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId, int itemIndex, ExcelConsigneeDto dto) {
        int existedConsigneeAmount = countByConsigneeName(dto.getPartnerName(), currentTenantId, dto.getCustomerId());
        if (existedConsigneeAmount > 0){
            buildErrors(mapExcelError, excelEmailErrors, itemIndex, "upload.excel.error.data.existing.entries",
                    new String[]{"Consignee Name"},"upload.excel.email.existing.entries", null);
        }
    }

    public void checkValidCustomer(List<ExcelConsigneeDto> listConsigneeDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId, Long currentTenantUserId, String token) {
        List<Partners> customerList = customerService.findByFilter(new PartnersFilter(currentTenantId, 0, Integer.MAX_VALUE), currentTenantUserId, true, token);
        Map<String, Long> customerNameMap = customerList.stream().collect(Collectors.toMap(Partners::getPartnerName, Partners::getId));
        for (int itemIndex = 0; itemIndex < listConsigneeDto.size(); itemIndex++){
            ExcelConsigneeDto dto = listConsigneeDto.get(itemIndex);

            if (!Objects.isNull(customerNameMap.get(dto.getCustomerName()))){
                dto.setCustomerId(customerNameMap.get(dto.getCustomerName()));
                continue;
            }

            buildErrors(mapExcelError, excelEmailErrors, itemIndex, "upload.excel.error.data.no.access",
                    new String[]{"Consignee", "customer"}, "upload.excel.email.existing.entries", null);

        }
    }

    private void buildErrors(Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors,
                             int itemIndex, String cellError, String[] errorCellParams, String mailError, String[] errorMailParams) {
        StringBuilder cellErrors = mapExcelError.get(itemIndex);

        if (cellErrors == null) {
            cellErrors = new StringBuilder();
        }

        excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam(cellError, errorCellParams));
        excelEmailErrors.add(messagesUtilities.getMessageWithParam(mailError, errorMailParams));

        mapExcelError.put(itemIndex, cellErrors);
    }

    public void saveExcelData(
            List<ExcelConsigneeDto> excelConsigneeDtoList,
            Map<Integer, StringBuilder> mapExcelError,
            Set<String> excelEmailErrors,
            Long currentTenantId
    ) {
        // Get all active countries
        List<CountryDTO> countryDTOList = countryClient.getAllActiveCountries();
        Map<String, String> countryShortNameByName = countryDTOList
                .stream()
                .collect(Collectors.toMap(CountryDTO::getCountryFullName, CountryDTO::getCountryShortName, (existing, replacement) -> existing));

        // List location
        Set<String> locationNameSet = new HashSet<>();

        for (ExcelConsigneeDto excelConsigneeDto : excelConsigneeDtoList) {
            locationNameSet.add(excelConsigneeDto.getLocationName());
        }
        List<Location> locationList = locationService.findByListLocName(currentTenantId, locationNameSet);
        Map<String, Location> locationByName = locationList
                .stream()
                .collect(Collectors.toMap(Location::getLocName, Function.identity(), (existing, replacement) -> existing));

        Map<ConsigneeDto, Partners> consigneeMap = new HashMap<>();
        for (int itemIndex = 0; itemIndex < excelConsigneeDtoList.size(); itemIndex++){
            ExcelConsigneeDto excelConsigneeDto = excelConsigneeDtoList.get(itemIndex);

            // create consignee
            ConsigneeDto consigneeDto = excelConsigneeDto.toConsigneeDto();
            consigneeDto.setTenantId(currentTenantId);
            Partners consignee = createOrGetExistingConsignee(consigneeDto, excelConsigneeDto, consigneeMap, mapExcelError, excelEmailErrors, itemIndex, currentTenantId);

            // add consignee place
            if (ObjectUtils.isNotEmpty(excelConsigneeDto.getLocationName())) {
                Location location = locationByName.get(excelConsigneeDto.getLocationName());
                // create new AddrContact
                AddrContactDTO addrContactDTO = excelConsigneeDto.toAddrContactDTO();
                AddrContact createdContact = addrContactClient.createOrUpdateAddressContact(addrContactDTO);

                if (ObjectUtils.isEmpty(location)) {
                    // create new Addr
                    UpdateAddrDTO updateAddrDTO = excelConsigneeDto.toUpdateAddrDTO(countryShortNameByName.get(excelConsigneeDto.getCountry()));
                    AddrDTO createdAddress = addrClient.createOrUpdateAddress(updateAddrDTO);

                    // create new Location
                    Location newLocation = excelConsigneeDto.toLocation(createdAddress.getId(), createdContact.getId(), currentTenantId);
                    location = locationService.createOrFind(newLocation);
                    locationByName.put(location.getLocName(), location);
                }

                // create consignee place
                PartnerLocation partnerLocation = excelConsigneeDto.toPartnerLocation(consignee, location, createdContact.getId());
                partnerLocationRepository.save(partnerLocation);
            }
        }
    }

    private Partners createOrGetExistingConsignee(
            ConsigneeDto consigneeDto,
            ExcelConsigneeDto excelConsigneeDto,
            Map<ConsigneeDto, Partners> consigneeMap,
            Map<Integer, StringBuilder> mapExcelError,
            Set<String> excelEmailErrors,
            int itemIndex,
            Long currentTenantId) {
        Partners consignee = consigneeMap.get(consigneeDto);
        if (ObjectUtils.isEmpty(consignee)) {
            List<Partners> existingConsignee = findByFilter(
                    new PartnersFilter(currentTenantId, excelConsigneeDto.getPartnerName(), excelConsigneeDto.getCustomerId()));
            if (existingConsignee.isEmpty()) {
                // create new consignee
                consignee = createConsigneeFromExcel(consigneeDto, mapExcelError, excelEmailErrors, itemIndex);
                if (ObjectUtils.isNotEmpty(consignee)) {
                    consigneeMap.put(consigneeDto, consignee);
                }
            } else {
                consignee = existingConsignee.get(0);
                if (doesUpdateConsignee(consignee, consigneeDto)) {
                    consignee = update(consignee.getId(), consigneeDto);
                }
                consigneeMap.put(consigneeDto, consignee);
            }
        } else {
            if (doesUpdateConsignee(consignee, consigneeDto)) {
                consignee = update(consignee.getId(), consigneeDto);
                consigneeMap.put(consigneeDto, consignee);
            }
        }
        return consignee;
    }

    private boolean doesUpdateConsignee(Partners consignee, ConsigneeDto consigneeDto) {
        return (consignee.getActiveInd() ^ consigneeDto.getActiveInd() ||
                (ObjectUtils.isNotEmpty(consignee.getPartnerCode()) && !consignee.getPartnerCode().equals(consigneeDto.getPartnerCode())) ||
                (ObjectUtils.isNotEmpty(consigneeDto.getPartnerCode()) && !consigneeDto.getPartnerCode().equals(consignee.getPartnerCode()))
        );
    }

    private Partners createConsigneeFromExcel(
            ConsigneeDto consigneeDto,
            Map<Integer, StringBuilder> mapExcelError,
            Set<String> excelEmailErrors,
            int itemIndex) {
        List<Partners> listCustomer = partnersService.findByPartnerNameAndLookupCode(consigneeDto.getCustomerName(), AppConstants.PartnerType.CUSTOMER);

        if (listCustomer.size() == 1){
            consigneeDto.setCustomerId(listCustomer.get(0).getId());
        } else if (listCustomer.isEmpty()) {
            StringBuilder cellErrors = mapExcelError.get(itemIndex);

            if (cellErrors == null) {
                cellErrors = new StringBuilder();
            }

            excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Customer"}));
            mapExcelError.put(itemIndex, cellErrors);

        } else {
            buildErrors(mapExcelError, excelEmailErrors, itemIndex, "upload.excel.error.data.more.than.one",
                    new String[]{"Customer", "this Customer Name"}, "upload.excel.email.invalid.entries", null);
        }

        try {
            if (mapExcelError.isEmpty()) {
                return save(consigneeDto);
            }
            return null;
        } catch (Exception e) {
            StringBuilder cellErrors = mapExcelError.get(itemIndex);

            if (ObjectUtils.isEmpty(cellErrors)) {
                cellErrors = new StringBuilder();
            }
            excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.saving.failed", null));
            excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.saving.failed", null));
            mapExcelError.put(itemIndex, cellErrors);
            return null;
        }
    }

    @Override
    public void downloadExcel(HttpServletResponse response, Long currentTenantId, Long tenantUserId, PartnersFilter filter) {
        List<Partners> consigneeList = getConsigneeToExport(currentTenantId, tenantUserId, filter);
        List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_EXPORT);

        if (CollectionUtils.isEmpty(consigneeList)) {
            excelUtils.exportExcel(response, Collections.emptyList(), ExcelConsigneeDto.class, template);
            return;
        }

        Map<Long, Partners> mapConsignee = consigneeList.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        List<Long> consigneeIds = consigneeList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<PartnerLocation> partnerLocations = partnerLocationService.findByPartnerIds(consigneeIds);
        List<Long> locationIds = partnerLocations.stream().map(PartnerLocation::getLocationId).collect(Collectors.toList());
        List<Long> addressIds = partnerLocations.stream().map(PartnerLocation::getAddressId).collect(Collectors.toList());
        List<Long> addressContactIds = partnerLocations.stream().map(PartnerLocation::getAddressContactId).collect(Collectors.toList());

        List<Location> locations = locationService.findByIds(locationIds);

        List<AddrDTO> addrDTOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(addressIds)) {
            addrDTOs = addrClient.postFindByFilter(new AddressFilter(addressIds));
        }

        List<AddrContact> addrContacts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(addressContactIds)) {
            addrContacts = addrContactClient.postFindByFilter(new AddrContactFilter(addressContactIds));
        }

        Map<Long, Location> mapLocation = locations.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        Map<Long, AddrDTO> mapAddrDTO = addrDTOs.stream().collect(Collectors.toMap(BaseDto::getId, Function.identity()));
        Map<Long, AddrContact> mapAddrContact = addrContacts.stream().collect(Collectors.toMap(AddrContact::getId, Function.identity()));

        List<ConsigneePlaceDto> consigneePlaceDTOs = partnerLocations.stream().map(partnerLocation -> {
            PartnerAddrContactDTO partnerAddrContactDTO = new PartnerAddrContactDTO();
            PartnerAddrDTO partnerAddrDTO = new PartnerAddrDTO();

            AddrContact addrContact = mapAddrContact.get(partnerLocation.getAddressContactId());
            if (Objects.nonNull(addrContact)) {
                BeanCopierFactory.AddrContact_PartnerAddrContactDto_copier.copy(addrContact, partnerAddrContactDTO, null);
            }

            AddrDTO addr = mapAddrDTO.get(partnerLocation.getAddressId());
            if (Objects.nonNull(addr)) {
                BeanCopierFactory.AddrDto_PartnerAddrDto_copier.copy(addr, partnerAddrDTO, null);
            }

            Location location = mapLocation.get(partnerLocation.getLocationId());

            return mapToConsigneePlaceDto(partnerLocation, location, partnerLocation.getPartners().getId(), partnerAddrContactDTO, partnerAddrDTO);
        }).collect(Collectors.toList());

        Set<Long> consigneeHaveLocationIds = consigneePlaceDTOs.stream().map(ConsigneePlaceDto::getConsigneeId).collect(Collectors.toSet());
        List<Long> consigneeNotHaveLocationIds = consigneeIds.stream().filter(id -> !consigneeHaveLocationIds.contains(id)).collect(Collectors.toList());

        List<ExcelConsigneeDto> excelConsigneeDTOs = new ArrayList<>();
        consigneePlaceDTOs.forEach(consigneePlaceDto -> excelConsigneeDTOs.add(new ExcelConsigneeDto(mapConsignee.get(consigneePlaceDto.getConsigneeId()), consigneePlaceDto)));
        consigneeNotHaveLocationIds.forEach(id -> excelConsigneeDTOs.add(new ExcelConsigneeDto(mapConsignee.get(id), null)));

        this.sortByMultiFields(filter.getSort(), excelConsigneeDTOs);

        excelUtils.exportExcel(response, excelConsigneeDTOs, ExcelConsigneeDto.class, template);
    }

    @Override
    public List<Long> findConsigneeIdsByConsigneeName(Long tenantId, String consignee) {
        return partnersRepository.findConsigneeIdsByConsigneeName(tenantId, consignee);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<LocationDto>> findLocationsByMultipleConsigneeId(PartnerLocationFilter partnerLocationFilter, Boolean isSearchDropdown) {
        List<Partners> partnersList = null;
        if(CollectionUtils.isNotEmpty(partnerLocationFilter.getConsigneeIdList())) {
            QPartners qPartners = QPartners.partners;
            BooleanExpression queryPartners = qPartners.isNotNull();
            queryPartners = queryPartners.and(qPartners.id.in(partnerLocationFilter.getConsigneeIdList()));
            List<Partners> partners = partnersRepository.findAll(queryPartners);
            partnersList = (List<Partners>) partners.stream().filter(p -> {
                List<PartnerTypes> partnerTypes = p.getPartnerTypes().stream().collect(Collectors.toList());
                Lookup lookup = lookupRepository.findByLookupTypeAndLookupCode(AppConstants.LookupType.PARTNER_TYPES, AppConstants.PartnerType.CONSIGNEE);

                partnerTypes.stream().filter(k -> k.getLookup().getId() == lookup.getId()).collect(Collectors.toList());
                return null != partnerTypes && partnerTypes.size() > 0;
            }).collect(Collectors.toList());

        }
        QPartnerLocation qPartnerLocation = QPartnerLocation.partnerLocation;
        BooleanExpression query = qPartnerLocation.isNotNull();
        Map<Long, List<LocationDto>> partnerIdMap = (Map<Long, List<LocationDto>>) processLocations(partnerLocationFilter, query, qPartnerLocation, isSearchDropdown);
        return partnerIdMap;
    }

    private Object processLocations(PartnerLocationFilter partnerLocationFilter, BooleanExpression query, QPartnerLocation qPartnerLocation, Boolean isSearchDropdown) {
        List<LocationDto> locationDtoList = new ArrayList<>();
        if(ObjectUtils.allNotNull(partnerLocationFilter.getLocationId())) {
            query = query.and(qPartnerLocation.locationId.eq(partnerLocationFilter.getLocationId()));
        }

        List<PartnerLocation> partnerLocationList = partnerLocationRepository.findAll(query);
        Map<Long, List<PartnerLocation>> groupPartnerMap = partnerLocationList.stream().collect(Collectors.groupingBy(s -> s.getPartners().getId()));

        Map<Long, List<LocationDto>> partnerMap = new HashMap<>();
        List<LocationDto> finalLocationDtoList = locationDtoList;

        if((CollectionUtils.isNotEmpty(partnerLocationList) && !isSearchDropdown) || isSearchDropdown) {
            if(null != groupPartnerMap) {
                groupPartnerMap.forEach((i, p) -> {
                    List<LocationDto> locationForPartner = new ArrayList<>();
                    List<Long> locationIdList = p.stream().map(q -> q.getLocationId()).filter(Objects::nonNull).filter(distinctByKey(Long::longValue)).filter(Objects::nonNull).collect(Collectors.toList());
                    List<LocationDto> locationList = new ArrayList<>();
                    if(CollectionUtils.isNotEmpty(locationIdList)) {
                        LocationFilter locationFilter = new LocationFilter();
                        locationFilter.setActiveInd(Boolean.TRUE);
                        locationFilter.setTenantId(partnerLocationFilter.getTenantId());
                        locationFilter.setIds(locationIdList);
                        locationFilter.setIsAddrAndContact(Boolean.FALSE);
                        locationList = locationService.findByFilter(locationFilter);
                    }
                    finalLocationDtoList.addAll(locationList);
                    locationForPartner.addAll(locationList);
                    partnerMap.put(i, locationForPartner);
                });
            }
            locationDtoList = finalLocationDtoList;
        } else {
            locationDtoList = locationService.findAllByTenantId(partnerLocationFilter.getTenantId());
        }

        if(null != partnerLocationFilter.getConsigneeId()) {
            return locationDtoList;
        } else {
            return partnerMap;
        }
    }

    @Override
    @Transactional
    public List<PartnersDto> createOrUpdateMultiple(List<ConsigneeDto> consigneeDtoList) {
        List<Partners> partners = consigneeDtoList.stream().map(this::mapToPartners).collect(Collectors.toList());
        List<Partners> consigneeSaved = partnersRepository.saveAll(partners);

        // Add partner types as consignees
        var lookUp = lookupRepository.findByLookupTypeAndLookupCode(AppConstants.LookupType.PARTNER_TYPES, AppConstants.PartnerType.CONSIGNEE);
        List<PartnerTypes> partnerTypes = new ArrayList<>();

        partners.forEach(partner -> {
            var partnerType = new PartnerTypes();
            partnerType.setPartners(partner);
            partnerType.setLookup(lookUp);
            partnerTypes.add(partnerType);
        });

        partnerTypesRepository.saveAll(partnerTypes);

        return consigneeSaved.stream().map(consignee -> modelMapper.map(consignee, PartnersDto.class)).collect(Collectors.toList());
    }

    public void validateAndSaveExcelData(Long currentTenantId, Workbook workbook, Sheet sheet, String fileName, Map<Integer,
            StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo, String token) {
        List<ExcelConsigneeDto> listConsigneeAfterReadExcel = excelUtils.parseExcelToDto(ExcelConsigneeDto.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
        String emailTemplateName = StringUtils.EMPTY;

        checkValidCustomer(listConsigneeAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId, currentTenantId, token);
        checkDuplicatedEntries(listConsigneeAfterReadExcel, mapExcelError, excelEmailErrors);
        checkExistedEntriesAndMatchingLocationInfo(listConsigneeAfterReadExcel, mapExcelError, excelEmailErrors, currentTenantId);

        if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
            emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);

        } else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
            saveExcelData(listConsigneeAfterReadExcel, mapExcelError,  excelEmailErrors, currentTenantId);

            //if there is error while saving
            if (!mapExcelError.isEmpty() || !excelEmailErrors.isEmpty()) {
                emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
            } else {
                emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.success", null);
            }
        }

        byte[] attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError, uploadTemplateHdrIdDto, timeStartReadingFile);

        // Send upload success email
        StringBuilder excelEmailErrorsVl = new StringBuilder();
        excelEmailErrors.forEach(cellError -> {
            excelEmailErrorsVl.append(cellError);
        });

        excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.consignee", null),
                excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);
    }
}
