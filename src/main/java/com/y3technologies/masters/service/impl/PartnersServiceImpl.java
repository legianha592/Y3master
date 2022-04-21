package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.AddrClient;
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.dto.PartnerConfigDto;
import com.y3technologies.masters.dto.PartnerTypesDto;
import com.y3technologies.masters.dto.PartnersDto;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.model.*;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.*;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.PartnerConfigService;
import com.y3technologies.masters.service.PartnerTypesService;
import com.y3technologies.masters.service.PartnersService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnersServiceImpl implements PartnersService {

	private final PartnersRepository partnersRepository;
	private final OperationLogService operationLogService;
	private final PartnerConfigService partnerConfigService;
	private final PartnerTypesService partnerTypesService;
	private final PartnerConfigRepository partnerConfigRepository;
	private final ConfigCodeRepository configCodeRepository;
	private final LookupRepository lookupRepository;
	private final PartnerTypesRepository partnerTypesRepository;
	private final AddrClient addrClient;
	private final AddrContactClient addrContactClient;
	private final AasClient aasClient;

	@Override
	@Transactional
	public Partners save(PartnersDto dto) {
		Partners model = new Partners();

		BeanCopier copier = BeanCopier.create(PartnersDto.class, Partners.class, false);
		copier.copy(dto, model, null);

		// save address or reuse addressId if exist
		Optional<AddrDTO> savedAddress = Optional.ofNullable(addrClient.createOrUpdateAddress(mapAddrDto(dto)));
		savedAddress.ifPresent(value -> model.setAddressId(value.getId()));
		// save address contact or reuse addressContactId if exist
		if (StringUtils.isNotBlank(dto.getMobileNumber1()) || StringUtils.isNotBlank(dto.getEmail())) {
			Optional<AddrContact> savedAddrContact = Optional
					.ofNullable(addrContactClient.createOrUpdateAddressContact(mapAddrContactDto(dto)));
			savedAddrContact.ifPresent(value -> model.setAddressContactId(value.getId()));
		} else {
			model.setAddressId(null);
		}

		Long id = model.getId();
		Boolean updateFlag = Boolean.TRUE;

		List<PartnerConfig> originalConfigs = null;
		List<PartnerTypes> originalPartnerTypes = null;

		if (model.getId() != null) {
			Partners oldModel = partnersRepository.findById(id).get();
			originalConfigs = partnerConfigRepository.getByPartnerId(model.getId());
			originalPartnerTypes = partnerTypesRepository.getByPartnerId(model.getId());
			updateFlag = oldModel.getHashcode() != model.hashCode();
		}
		model.setHashcode(model.hashCode());
		partnersRepository.save(model);

		//save partnerTypes
		if(CollectionUtils.isNotEmpty(originalPartnerTypes)){
			if (CollectionUtils.isNotEmpty(dto.getPartnerTypeIds())) {

				List<PartnerTypes> partnerTypesToBeRemovedList=
						originalPartnerTypes.stream()
								.filter (
										originalPartnerType -> dto.getPartnerTypeIds().stream()
												.noneMatch(newPartnerTypeId -> originalPartnerType.getLookup().getId().equals(newPartnerTypeId)))
								.collect(Collectors.toList());

				partnerTypesRepository.deleteAll(partnerTypesToBeRemovedList);
			}else{
				partnerTypesRepository.deleteAll(originalPartnerTypes);
			}
		}

		if (CollectionUtils.isNotEmpty(dto.getPartnerTypeIds())) {
			List<Long> partnerTypesToBeAddedList;
			if(originalPartnerTypes != null) {
				List<PartnerTypes> finalOriginalPartnerTypes = originalPartnerTypes;
				partnerTypesToBeAddedList =
						dto.getPartnerTypeIds().stream()
								.filter(
										pId -> finalOriginalPartnerTypes.stream()
												.noneMatch(original -> original.getLookup().getId().equals(pId)))
								.collect(Collectors.toList());
			}else{
				partnerTypesToBeAddedList = dto.getPartnerTypeIds();
			}

			for (Long partnerTypeId : partnerTypesToBeAddedList) {
				Lookup lookup = lookupRepository.getOne(partnerTypeId);
				PartnerTypes partnerTypes = new PartnerTypes();
				partnerTypes.setPartners(model);
				partnerTypes.setLookup(lookup);
				partnerTypesService.save(partnerTypes);
			}
		}

//		//save partner config
		if (CollectionUtils.isNotEmpty(originalConfigs)) {
			if (CollectionUtils.isNotEmpty(dto.getPartnerConfigDtos())) {
				List<PartnerConfigDto> idNotNull = dto.getPartnerConfigDtos().stream()
						.filter(item -> item.getId() != null).collect(Collectors.toList());
				List<Long> dtoConfigIdList = CollectionUtils.isNotEmpty(idNotNull)
						? idNotNull.stream().map(PartnerConfigDto::getId).collect(Collectors.toList())
						: null;
				if (CollectionUtils.isNotEmpty(dtoConfigIdList)) {
					List<PartnerConfig> needRemove = originalConfigs.stream().filter(item -> {
						if (dtoConfigIdList.contains(item.getId())) {
							return false;
						}
						return true;
					}).collect(Collectors.toList());
					partnerConfigRepository.deleteAll(needRemove);
				}
			} else {
				partnerConfigRepository.deleteAll(originalConfigs);
			}
		}
		if (CollectionUtils.isNotEmpty(dto.getPartnerConfigDtos())) {
			for (PartnerConfigDto configDto : dto.getPartnerConfigDtos()) {
				PartnerConfig config = new PartnerConfig();
				final BeanCopier configCopier = BeanCopier.create(PartnerConfigDto.class, PartnerConfig.class, false);
				configCopier.copy(configDto, config, null);
				config.setPartners(model);
				if (configDto.getConfigCodeId() != null) {
					config.setConfigCode(configCodeRepository.getOne(configDto.getConfigCodeId()));
				}
				partnerConfigService.save(config);
			}
		}

		if (updateFlag) {
			//update partnerCode in aasTenantUserProfile (ProfileValue)
			aasClient.updateTenantUserProfileValue(model.getPartnerName(), model.getId());

			operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
		}
		return model;
	}

	@Override
	@Transactional(readOnly = true)
	public PartnersDto getById(Long id) {
		Partners partners = partnersRepository.findById(id).get();
		PartnersDto dto = null;
		if (partners != null) {
			dto = new PartnersDto();
			final BeanCopier copier = BeanCopier.create(Partners.class, PartnersDto.class, false);
			copier.copy(partners, dto, null);

			Long addressId = partners.getAddressId();
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
			Long addressContactId = partners.getAddressContactId();
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
			dto.setAddr(address);
			dto.setAddrContact(addrContact);

			List<PartnerConfigDto> partnerConfigDtos = partnerConfigRepository.findByPartnerId(id);
			dto.setPartnerConfigDtos(partnerConfigDtos);

			List<PartnerTypesDto> partnerTypesDtos = partnerTypesRepository.findByPartnerId(id);
			dto.setPartnerTypeDtos(partnerTypesDtos);
		}
		return dto;
	}

	@Override
	public Partners findById(Long id) {
		return partnersRepository.findById(id).orElse(null);
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

			if (input.getColumn("partnerTypes") != null
					&& StringUtils.isNotEmpty(input.getColumn("partnerTypes").getSearch().getValue())
					&& input.getColumn("partnerTypes").getSearchable()) {
				Specification<Partners> sPartnerType = new Specification<Partners>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Predicate toPredicate(Root<Partners> root, CriteriaQuery<?> query,
												 CriteriaBuilder criteriaBuilder) {
						Join<Partners, PartnerTypes> join = root.join("partnerTypes", JoinType.LEFT);
						Path<Lookup> lookup = join.get("lookup");
						return criteriaBuilder.equal(lookup.get("lookupCode"), input.getColumn("partnerTypes").getSearch().getValue());
					}
				};
				return partnersRepository.findAll(input, sTenantId, sPartnerType);
			}

			return partnersRepository.findAll(input, sTenantId);
		}
		return partnersRepository.findAll(input);
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
	public List<PartnersDto> getByTenantIdAndPartnerTypes(Long tenantId, String partnerType) {
		List<Partners> partnersList = partnersRepository.getByTenantIdAndPartnerTypes(tenantId, partnerType);
		List<PartnersDto> partnersDtoList = new ArrayList<>();
		if (!partnersList.isEmpty()) {
			partnersDtoList = new ArrayList<>();
			for (Partners partners : partnersList) {
				PartnersDto dto = new PartnersDto();
				final BeanCopier copier = BeanCopier.create(Partners.class, PartnersDto.class, false);
				copier.copy(partners, dto, null);

				List<PartnerTypesDto> typeDtos = new ArrayList<>();
				for (PartnerTypes pt : partners.getPartnerTypes()) {
					PartnerTypesDto typeDto = new PartnerTypesDto();
					typeDto.setPartnerTypeId(pt.getLookup().getId());
					typeDto.setPartnerType(pt.getLookup().getLookupCode());
					typeDtos.add(typeDto);
				}
				dto.setPartnerTypeDtos(typeDtos);

				Long addressId = partners.getAddressId();
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
				dto.setAddr(address);
				partnersDtoList.add(dto);
			}
		}
		return partnersDtoList;
	}

	private UpdateAddrDTO mapAddrDto(PartnersDto dto) {
		UpdateAddrDTO updateAddrDTO = new UpdateAddrDTO();

		BeanCopier copier = BeanCopier.create(PartnersDto.class, UpdateAddrDTO.class, false);
		copier.copy(dto, updateAddrDTO, null);

		return updateAddrDTO;
	}

	private AddrContactDTO mapAddrContactDto(PartnersDto dto) {
		AddrContactDTO addrContactDTO = new AddrContactDTO();

		BeanCopier copier = BeanCopier.create(PartnersDto.class, AddrContactDTO.class, false);
		copier.copy(dto, addrContactDTO, null);
		return addrContactDTO;
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
			query = query.and(partners.partnerName.like("%" + keyword + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getExacName())) {
			query = query.and(partners.partnerName.eq(filter.getExacName()));
		}

		if (StringUtils.isNoneBlank(filter.getPartnerType())) {
			query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
					.where(partnerTypes.lookup.lookupCode.eq(filter.getPartnerType()))));
		}

		if (CollectionUtils.isNotEmpty(filter.getPartnerIdList())) {
			query = query.and(partners.id.in(filter.getPartnerIdList()));
		}

		if(filter.getActiveInd() != null){
			query = query.and(partners.activeInd.eq(filter.getActiveInd()));
		}

		if (CollectionUtils.isNotEmpty(filter.getPartnerNames())) {
			List<String> partnerNames = filter.getPartnerNames().stream().map(String::trim).collect(Collectors.toList());
			query = query.and(partners.partnerName.in(partnerNames));
		}

		return partnersRepository.findAll(query);
	}

	@Override
	public List<Partners> findByFilterIgnoreCase(PartnersFilter filter) {

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
			query = query.and(partners.partnerName.likeIgnoreCase("%" + keyword + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getExacName())) {
			query = query.and(partners.partnerName.equalsIgnoreCase(filter.getExacName()));
		}

		if (StringUtils.isNoneBlank(filter.getPartnerType())) {
			query = query.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
					.where(partnerTypes.lookup.lookupCode.eq(filter.getPartnerType()))));
		}

		if (CollectionUtils.isNotEmpty(filter.getPartnerIdList())) {
			query = query.and(partners.id.in(filter.getPartnerIdList()));
		}

		if(filter.getActiveInd() != null){
			query = query.and(partners.activeInd.eq(filter.getActiveInd()));
		}

		if (CollectionUtils.isNotEmpty(filter.getPartnerNames())) {
			List<String> partnerNames = filter
					.getPartnerNames()
					.stream()
					.map(String::trim)
					.map(String::toUpperCase)
					.collect(Collectors.toList());
			query = query.and(partners.partnerName.toUpperCase().in(partnerNames));
		}

		return partnersRepository.findAll(query);
	}

	@Override
	public Partners findByPartnerCodeAndTenantId(String partnerCode, Long tenantId) {

		return partnersRepository.findByPartnerCodeAndTenantId(partnerCode, tenantId);
	}

	@Override
	public List<Partners> findByPartnerNameAndLookupCode(String partnerName, String lookupCode) {
		return partnersRepository.findByPartnerNameAndLookupCode(partnerName,lookupCode);
	}

	@Override
	public  List<Partners> findByTenantIdAndPartnerNameAndLookupCode (Long tenantId, String partnerName, String lookupCode){
		return  partnersRepository.findByTenantIdAndPartnerNameAndLookupCode(tenantId, partnerName, lookupCode);
	}

	@Override
	public Partners findByPartnerNameAndTenantId(String partnerName, Long tenantId) {

		return partnersRepository.findByPartnerNameAndTenantId(partnerName, tenantId);
	}
}
