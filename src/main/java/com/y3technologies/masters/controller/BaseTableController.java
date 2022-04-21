package com.y3technologies.masters.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.table.BaseTableDto;
import com.y3technologies.masters.model.BaseEntity;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.util.ConstantVar;

@Component
public abstract class BaseTableController<D extends BaseTableDto, T extends BaseEntity> {

	@Autowired
	private LookupService lookupService;

	public static Map<String, List<Lookup>> lookupMap = new HashMap<String, List<Lookup>>();

	public DataTablesOutput<D> convertToTableDto(DataTablesOutput<T> table) {

		DataTablesOutput<D> tableDto = new DataTablesOutput<D>();
		tableDto.setDraw(table.getDraw());
		tableDto.setError(table.getError());
		tableDto.setRecordsFiltered(table.getRecordsFiltered());
		tableDto.setRecordsTotal(table.getRecordsTotal());
		tableDto.setData(CollectionUtils.isEmpty(table.getData()) ? new ArrayList<>()
				: table.getData().stream().map(e -> mapTableDto(e)).collect(Collectors.toList()));
		return tableDto;
	}

	public abstract D mapTableDto(T data);

	@Deprecated
	public String getLookupDescription(String lookupType, String lookupCode) {
		initLookupMap();
		if(StringUtils.isEmpty(lookupCode)) {
			return null;
		}
		List<Lookup> list = lookupMap.get(lookupType);
		String result = "";
		if (!CollectionUtils.isEmpty(list)) {
			Map<String, String> map = list.stream()
					.collect(Collectors.toMap(Lookup::getLookupCode, Lookup::getLookupDescription));
			String[] codes = lookupCode.split("\\|");
			for (int i = 0; i < codes.length; i++) {
				String desc = map.get(codes[i]);
				if (desc != null) {
					result += desc + ConstantVar.ORPREFIX;
				}else {
					initLookupMap();
				}
			}
		}else {
			initLookupMap();
		}
		if(result.endsWith(ConstantVar.ORPREFIX)) {
			result = result.substring(0, result.length()-1);
		}
		return result;
	}
	

//	@PostConstruct
	private void initLookupMap() {
		List<Lookup> lookupList = lookupService.getAllLookup(getCurrentTenantId());
		if (!CollectionUtils.isEmpty(lookupList)) {
			lookupMap = lookupList.stream().collect(Collectors.groupingBy(Lookup::getLookupType));
		}
	}

	protected Long getCurrentTenantId() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
				getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getAasTenantId();
		}
		return null;
	}

	protected String getCurrentTenantTimezone() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
				getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getTimezone();
		}
		return null;
	}

	protected Long getCurrentAasUserId() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
				getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getAasUserId();
		}
		return null;
	}

	protected Long getCurrentTenantUserId() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
				getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getAasTenantUserId();
		}
		return null;
	}

	protected List<SessionUserInfoDTO.SessionUserProfileDTO> getCurrentTenantUserProfileList() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
				getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getProfileDTOList();
		}
		return null;
	}

	protected SessionUserInfoDTO.SessionUserProfileDTO getCurTenantUsrProfileByProfileCode(String profileCode) {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
				getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getProfileDTOList().stream().filter(dto -> dto.getProfileCode().equals(profileCode))
					.findFirst().orElse(null);
		}
		return null;
	}
}
