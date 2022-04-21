package com.y3technologies.masters.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.y3technologies.masters.dto.CreateLocationDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.LocationFilter;
import org.springframework.data.domain.Page;

import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.model.Location;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface LocationService {

	void save(Location location);

	Location findById(Long id);

	Location getById(Long id);

	Page<LocationDto> findBySearch(LocationFilter filter, Long tenantUserId);

	void updateStatus(Long id, Boolean status);

	Location findOneByLocation(Location search);

	Location findOrCreateLocation(LocationDto dto);

	List<LocationDto> findAllByTenantId(Long tenantId);

	Location createOrFind(Location location);

	Set<Long> findAllByLocCode(String locCode);

	List<LocationDto> findByFilter(LocationFilter filter);

	LocationDto findLocationByCode(Long tenantId, String locCode);

	LocationDto findLocationByName(Long tenantId, String locCode);

	int countByCondition (Long tenantId, String locName);

	ExcelResponseMessage uploadExcel(MultipartFile file, Long tenantId);

	void downloadExcel(LocationFilter filter, HttpServletResponse response, Long tenantUserId);

	StringBuilder processLocationTags(CreateLocationDto locationDto, Long tenantId);

	List<LocationDto> createOrUpdateMultiple(List<LocationDto> locationDtoList);

	List<Location> findByListLocName(Long tenantId, Collection<String> listLocName);

	List<Location> findByIds(List<Long> ids);
}
