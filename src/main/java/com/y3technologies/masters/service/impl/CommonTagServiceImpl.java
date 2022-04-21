package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.dto.CommonTagAndTagType;
import com.y3technologies.masters.dto.CommonTagDto;
import com.y3technologies.masters.dto.filter.CommonTagFilter;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.QCommonTag;
import com.y3technologies.masters.repository.CommonTagRepository;
import com.y3technologies.masters.service.CommonTagService;
import com.y3technologies.masters.util.EntitySpecificationBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommonTagServiceImpl extends BaseServiceImpl implements CommonTagService {

	@Autowired
	private CommonTagRepository commonTagRepository;

	@Autowired
	ModelMapper modelMapper;

	@Override
	public List<CommonTag> listByParam(CommonTag tag) {
		EntitySpecificationBuilder<CommonTag> es = new EntitySpecificationBuilder<CommonTag>();
		if(StringUtils.isNotEmpty(tag.getTagType())) {
			es.with("tagType", ":", tag.getTagType(), "and");
		}
		if(tag.getTenantId() != null) {
			es.with("tenantId", ":", tag.getTenantId(), "and");
		}
		else {
			tag.setTenantId(0L);
			es.with("tenantId", ":", 0, "and");
		}
		es.with("activeInd", ":", true, "and");
    	List<CommonTag> list = commonTagRepository.findAll(es.build());
    	if (0 == list.size() && !tag.getTenantId().equals(0L)){
			EntitySpecificationBuilder<CommonTag> es_tenantId_0 = new EntitySpecificationBuilder<>();
			es_tenantId_0.with("tenantId", ":", 0, "and");
			if(StringUtils.isNotEmpty(tag.getTagType())) {
				es_tenantId_0.with("tagType", ":", tag.getTagType(), "and");
			}
			es_tenantId_0.with("activeInd", ":", true, "and");
			list = commonTagRepository.findAll(es_tenantId_0.build());
		}
		return list;
	}
	public List<CommonTag> getAllCommonTag(){
		EntitySpecificationBuilder<CommonTag> es = new EntitySpecificationBuilder<CommonTag>();
		es.with("activeInd", ":", true, "and");
    	List<CommonTag> list = commonTagRepository.findAll(es.build());
		return list;
	}

	public List<CommonTag> getAllCommonTagByTenantId(Long tenantId) {
		EntitySpecificationBuilder<CommonTag> es = new EntitySpecificationBuilder<CommonTag>();
		es.with("tenantId", ":", tenantId != null ? tenantId : 0, "and");
		es.with("activeInd", ":", true, "and");
		List<CommonTag> list = commonTagRepository.findAll(es.build());
		return list;
	}

	private BooleanExpression queryString(CommonTagFilter filter) {

		QCommonTag commonTag = QCommonTag.commonTag;
		BooleanExpression query = commonTag.isNotNull();

		if (filter.getTenantId() != null) {
			// If Tenant ID is NOT NULL then get only lookup created by Tenant itself;
			query = query.and(commonTag.tenantId.eq(filter.getTenantId()));
		} else {
			query = query.and(commonTag.tenantId.eq(0L));
		}

		if (StringUtils.isNoneBlank(filter.getTag())) {
			String keyword = filter.getTag().trim();
			query = query.and(commonTag.tag.like("%" + keyword + "%"));
		}

		if (StringUtils.isNoneBlank(filter.getTagType())) {
			String keyword = filter.getTagType().trim();
			query = query.and(commonTag.tagType.eq(keyword));
		}

		if (filter.getActive() != null) {
			query = query.and(commonTag.activeInd.eq(filter.getActive()));
		}

		if (CollectionUtils.isNotEmpty(filter.getCommonTagIdList())) {
			query = query.and(commonTag.id.in(filter.getCommonTagIdList()));
		}

		return query;
	}

	@Override
	public List<CommonTag> findByFilter(CommonTagFilter filter) {

		BooleanExpression query = this.queryString(filter);

		List<CommonTag> listCommonTag = commonTagRepository.findAll(query, Sort.by("tag"));

		if (CollectionUtils.isNotEmpty(listCommonTag))
			return listCommonTag;

		if (filter.getTenantId() != 0L) {

			filter.setTenantId(0L);

			return commonTagRepository.findAll(this.queryString(filter), Sort.by("tag"));
		}

		return Collections.emptyList();
	}

	@Override
	public CommonTag save(CommonTag commonTag) {
		return commonTagRepository.save(commonTag);
	}

	@Override
	public List<CommonTag> getAllByTagAndTagType(String tag, String tagType)
	{
		return commonTagRepository.findByTagAndTagType(tag,tagType);
	}

	@Override
	public CommonTag findById(Long id) {
		return commonTagRepository.findById(id).get();
	}

	@Override
	public int countByTagAndTypeAndTenantId(String tag, String tagType, Long tenantId) {
		return commonTagRepository.countByTagAndTypeAndTenantId(tag,tagType,tenantId);
	}

	public List<CommonTag> getAllCommonTag(Long tenantId){
		EntitySpecificationBuilder<CommonTag> es = new EntitySpecificationBuilder<CommonTag>();
		if(tenantId != null) {
			es.with("tenantId", ":", tenantId, "and");
		}
		else {
			es.with("tenantId", ":", 0, "and");
		}
		es.with("activeInd", ":", true, "and");
		List<CommonTag> list = commonTagRepository.findAll(es.build());
		return list;
	}

	@Override
	public List<CommonTag> saveMultiple(List<CommonTagDto> commonTagDtoList) {
		List<CommonTag> commonTagList = commonTagDtoList.stream().map(commonTagDto -> {
			var commonTag = modelMapper.map(commonTagDto, CommonTag.class);
			commonTag.setActiveInd(true);
			return commonTag;
		}).collect(Collectors.toList());
		return commonTagRepository.saveAll(commonTagList);
	}

	@Override
	public List<CommonTag> findByTagAndTagType(List<CommonTagAndTagType> commonTagAndTagTypeList) {
		if (commonTagAndTagTypeList.isEmpty()) {
			return Collections.emptyList();
		}
		QCommonTag commonTag = QCommonTag.commonTag;
		BooleanExpression query = commonTag.isNotNull();

		var firstElement = commonTagAndTagTypeList.get(0);
		query = query
				.and(commonTag.tenantId.eq(firstElement.getTenantId()))
				.and(commonTag.tag.eq(firstElement.getTag()))
				.and(commonTag.tagType.eq(firstElement.getTagType()));

		for (var i = 1; i < commonTagAndTagTypeList.size(); i++) {
			var commonTagAndTagType = commonTagAndTagTypeList.get(i);
				query = query.or(commonTag
						.isNotNull()
						.and(commonTag.tenantId.eq(commonTagAndTagType.getTenantId()))
						.and(commonTag.tag.eq(commonTagAndTagType.getTag()))
						.and(commonTag.tagType.eq(commonTagAndTagType.getTagType()))
				);
		}

		return commonTagRepository.findAll(query);
	}
}
