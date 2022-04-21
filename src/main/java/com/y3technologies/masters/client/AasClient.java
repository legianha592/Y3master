package com.y3technologies.masters.client;

import javax.validation.Valid;

import com.y3technologies.masters.dto.aas.ProfileScopeDTO;
import com.y3technologies.masters.dto.aas.TenantUserProfileScopeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.TenantUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;

import java.util.List;

@FeignClient(contextId = "aas-service", name = "aas", path = "/${api.version.aas}")
public interface AasClient {

	@GetMapping("/session/info/{token}")
	SessionUserInfoDTO getSessionInfo(@PathVariable("token") String token);

	@GetMapping("/user/proceedCreateTenantUserAccount/{email}")
	Boolean proceedCreateTenantUserAccount(@PathVariable("email") String email, @RequestParam Long tenantId);

	@PostMapping("/user/addTenantUser")
	TenantUserInfoDTO createTenantUser(@RequestBody @Valid TenantUserInfoDTO tenantUserInfoDTO);

	@GetMapping("/user/retrieve/profile/{id}")
	UpdateUserProfileDTO retrieveUserProfile(@PathVariable("id") Long id);

	@PutMapping("/user/updateTenantUserProfileValue/{id}")
	ResponseEntity updateTenantUserProfileValue(@RequestBody @Valid String profileValue, @PathVariable("id") Long refId);

	@GetMapping("/user/getTenantUserProfileScopes/{profileCode}/{tenantUserId}")
	List<TenantUserProfileScopeDTO> getTenantUserProfileScope(@PathVariable("tenantUserId") Long tenantUserId,
															  @PathVariable("profileCode") String profileCode);

	@GetMapping("/user/getAllTenantUserProfileScopes/{tenantUserId}")
	List<ProfileScopeDTO> getProfileScopeByTenantUserId(@PathVariable("tenantUserId") Long tenantUserId);

	@GetMapping("/user/getAllTenantUserProfileScopes/{tenantUserId}")
	List<ProfileScopeDTO> getProfileScopeByTenantUserId(@PathVariable("tenantUserId") Long tenantUserId, @RequestHeader("Authorization") String token);

}
