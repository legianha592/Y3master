package com.y3technologies.masters.service;

import com.y3technologies.masters.dto.CommonTagAndTagType;
import com.y3technologies.masters.dto.CommonTagDto;
import com.y3technologies.masters.dto.filter.CommonTagFilter;
import com.y3technologies.masters.model.CommonTag;

import java.util.List;

public interface CommonTagService {

	List<CommonTag> listByParam(CommonTag tag);

	List<CommonTag> getAllCommonTag();
	List<CommonTag> getAllCommonTagByTenantId(Long tenantId);
	List<CommonTag> findByFilter(CommonTagFilter filter);
	CommonTag save(CommonTag commonTag);
	List<CommonTag> getAllByTagAndTagType(String tag, String tagType);
	CommonTag findById(Long id);
	int countByTagAndTypeAndTenantId (String tag, String tagType, Long tenantId);
	List<CommonTag> saveMultiple(List<CommonTagDto> commonTagDtoList);
	List<CommonTag> findByTagAndTagType(List<CommonTagAndTagType> commonTagAndTagTypeList);
}
