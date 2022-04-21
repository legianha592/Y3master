package com.y3technologies.masters.helper;

import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;

@Component
public class LoggedUserInfo {

	public Long getCurrentTenantId() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes()
				.getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getAasTenantId();
		}
		return null;
	}

	public String getCurrentTenantTimezone() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes()
				.getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getTimezone();
		}
		return null;
	}

	public Long getCurrentAasUserId() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes()
				.getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getAasUserId();
		}
		return null;
	}

	public Long getCurrentTenantUserId() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes().
				getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getAasTenantUserId();
		}
		return null;
	}

	public List<SessionUserInfoDTO.SessionUserProfileDTO> getCurrentTenantUserProfileList() {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes()
				.getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getProfileDTOList();
		}
		return null;
	}

	public SessionUserInfoDTO.SessionUserProfileDTO getCurTenantUsrProfileByProfileCode(String profileCode) {
		SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO) RequestContextHolder.getRequestAttributes()
				.getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
		if (sessionUserInfoDTO != null) {
			return sessionUserInfoDTO.getProfileDTOList().stream()
					.filter(dto -> dto.getProfileCode().equals(profileCode)).findFirst().orElse(null);
		}
		return null;
	}

}
