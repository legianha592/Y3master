package com.y3technologies.masters.service.impl;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.dto.PartnerConfigDto;
import com.y3technologies.masters.dto.filter.PartnerConfigFilter;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.PartnerConfig;
import com.y3technologies.masters.model.QPartnerConfig;
import com.y3technologies.masters.repository.PartnerConfigRepository;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.PartnerConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class PartnerConfigServiceImpl implements PartnerConfigService {

	@Autowired
	private PartnerConfigRepository partnerConfigRepository;
	
	@Autowired
	private OperationLogService operationLogService;

	@Override
	@Transactional
	public void save(PartnerConfig model) {
		Long id = model.getId();
		Boolean updateFlag = Boolean.TRUE;
		if(model.getId() != null) {
			PartnerConfig oldModel = partnerConfigRepository.findById(id).get();
			updateFlag = oldModel.getHashcode() != model.hashCode();
		}
		model.setHashcode(model.hashCode());
		partnerConfigRepository.save(model);
		if(updateFlag) {
			operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
		}
	}
	
	@Override
	public PartnerConfigDto getById(Long id) {
		PartnerConfig partnerConfig = partnerConfigRepository.findById(id).get();
		PartnerConfigDto dto = null;
		if(partnerConfig != null) {
			dto = new PartnerConfigDto();
			final BeanCopier copier = BeanCopier.create(PartnerConfig.class, PartnerConfigDto.class, false);
			copier.copy(partnerConfig, dto, null);
			if(partnerConfig.getPartners() != null) {
				dto.setPartnerId(partnerConfig.getPartners().getId());
				dto.setPartnerCode(partnerConfig.getPartners().getPartnerCode());
				dto.setPartnerDescription(partnerConfig.getPartners().getDescription());
			}
			if(partnerConfig.getConfigCode() != null) {
				dto.setConfigCodeId(partnerConfig.getConfigCode().getId());
				dto.setConfigCode(partnerConfig.getConfigCode().getCode());
				dto.setConfigDescription(partnerConfig.getConfigCode().getDescription());
			}
		}
		return dto;
	}

	@Override
	public List<PartnerConfigDto> getAllPartnerConfigs(Long partnerId) {
		return partnerConfigRepository.findByPartnerId(partnerId);
	}

	@Override
	@Transactional
	public void updateStatus(Long id, Boolean status) {
		PartnerConfig model = partnerConfigRepository.findById(id).get();
		if(model.getActiveInd() != status) {
			partnerConfigRepository.updateStatus(id, status);
			operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
		}
	}

	@Override
	public Page<PartnerConfig> findBySearch(PartnerConfigFilter filter) {

		if (filter == null || filter.getPageNo() < 0 || filter.getPageSize() < 1)
			return Page.empty();

		QPartnerConfig partnerConfig = QPartnerConfig.partnerConfig;

		BooleanExpression query = partnerConfig.isNotNull();

		if (filter.getPartnerId() != null) {
			query = query.and(partnerConfig.partners.id.eq(filter.getPartnerId()));
		}

		if (StringUtils.isNoneBlank(filter.getConfigCode())) {
			String keyword = filter.getConfigCode().trim();
			query = query.and(partnerConfig.configCode.code.like("%" + keyword + "%"));
		}

		if(filter.getActiveInd() != null){
			query = query.and(partnerConfig.activeInd.eq(filter.getActiveInd()));
		}

		return partnerConfigRepository.findAll(query, PageRequest.of(filter.getPageNo(), filter.getPageSize(), Sort.by("activeInd").descending().and(Sort.by("configCode.code").ascending())));
	}

	@Override
	public PartnerConfig findById(Long id) {
		return partnerConfigRepository.findById(id).orElse(null);
	}
}
