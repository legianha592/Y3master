package com.y3technologies.masters.dto.aas;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TenantUserInfoDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long tenantId;

	private Long aasUserId;

	private Long tenantUserId;

	private boolean isActive = true;

	private String firstName;

	private String lastName;

	private String email;

	private String phoneCountryShortName;

	private String phone;

	private String userLevel;

	private Set<String> roles = new HashSet<>();

	public void addTenantUserRole(String roleCode) {
		roles.add(roleCode);
	}
}
