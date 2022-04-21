package com.y3technologies.masters.controller;

import com.y3technologies.masters.dto.apiConfig.AddConfigRequest;
import com.y3technologies.masters.dto.apiConfig.ApiConfigDTO;
import com.y3technologies.masters.dto.apiConfig.ApiConfigFilter;
import com.y3technologies.masters.dto.apiConfig.ApiEnum;
import com.y3technologies.masters.dto.apiConfig.GenerateApiKeyResponse;
import com.y3technologies.masters.dto.apiConfig.UpdateApiConfigRequest;
import com.y3technologies.masters.dto.apiConfig.UpdateApiConfigStatusRequest;
import com.y3technologies.masters.exception.PermissionDeniedException;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.apiConfig.ApiConfig;
import com.y3technologies.masters.model.apiConfig.ApiType;
import com.y3technologies.masters.service.ApiConfigService;
import com.y3technologies.masters.service.CustomerService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.util.EncryptUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/${api.version.masters}/apiConfig")
@RequiredArgsConstructor
public class ApiConfigController extends BaseController {

  private static final Logger logger = LoggerFactory.getLogger(ApiConfigController.class);

  private final LookupService lookupService;

  private final CustomerService customerService;

  private final ApiConfigService apiConfigService;

  private final EncryptUtils encryptUtils;

  // API for add new api config
  @PostMapping("/create")
  @ResponseBody
  public ResponseEntity<ApiConfig> addNewApiConfig(@Valid @RequestBody AddConfigRequest request) {

    // Get lookup
    Lookup lookup;
    if (lookupService.existById(request.getApiId())) {
      lookup = lookupService.findById(request.getApiId());
    } else {
      throw new TransactionException("exception.apiconfig.data.notexist");
    }

    // Get customer
    Partners customer;
    if (customerService.existById(request.getCustomerId())) {
      customer = customerService.findById(request.getCustomerId());
    } else {
      throw new TransactionException("exception.apiconfig.data.notexist");
    }

    // Check exist
    if (apiConfigService.existsByLookupAndCustomer(lookup, customer)) {
      throw new TransactionException("exception.apiconfig.data.exist");
    }

    // Get current tenantId
    Long tenantId = getCurrentTenantId();

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(apiConfigService.save(request, lookup, customer, tenantId));
  }

  @GetMapping("/newApiKey")
  public GenerateApiKeyResponse generateApiKey() {
    String newApiKey = apiConfigService.generateApiKey();
    return new GenerateApiKeyResponse(newApiKey);
  }

  @PutMapping("/update")
  @ResponseBody
  public ApiConfigDTO updateApiConfig(@Valid @RequestBody UpdateApiConfigRequest requestBody,
      @RequestParam("id") Long apiConfigId) {
    ApiConfig apiConfigFound = apiConfigService.findById(apiConfigId);
    if (apiConfigFound == null) {
      throw new TransactionException("exception.apiConfig.config.not.found");
    }
    Long tenantId = getCurrentTenantId();
    if (!apiConfigFound.getTenantId().equals(tenantId)) {
      throw new PermissionDeniedException();
    }
    BeanUtils.copyProperties(requestBody, apiConfigFound);
    return mapToApiConfigDTO(apiConfigService.update(apiConfigFound));
  }

  @GetMapping("/retrieve")
  @ResponseBody
  public ApiConfigDTO findById(@RequestParam("id") Long apiConfigId) {
    ApiConfig apiConfigFound = apiConfigService.findById(apiConfigId);
    if (apiConfigFound == null) {
      throw new TransactionException("exception.apiConfig.config.not.found");
    }
    Long tenantId = getCurrentTenantId();
    if (!apiConfigFound.getTenantId().equals(tenantId)) {
      throw new PermissionDeniedException();
    }
    return mapToApiConfigDTO(apiConfigFound);
  }

  // listing API Config
  @Operation(description = "Find all api config")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful return"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  @PostMapping(path = "/filterBy")
  public Page<ApiConfigDTO> filterBy(@RequestBody ApiConfigFilter filter) {
    // Get current tenantId
    Long tenantId = getCurrentTenantId();
    return apiConfigService.findAllApiConfigWithPagination(filter, tenantId);
  }

  // listing Api type enum
  @GetMapping("/listApiType")
  public List<ApiEnum> listApi() {
    List<ApiEnum> listApiEnum = new ArrayList<>();
    for (ApiType apiType : ApiType.values()) {
      listApiEnum.add(new ApiEnum(apiType.name(), apiType.getValue()));
    }
    return listApiEnum;
  }

  @PostMapping("/downloadExcel")
  public void downloadExcel(@RequestBody ApiConfigFilter filter, HttpServletResponse response) {
    Long tenantId = getCurrentTenantId();
    filter.setPageSize(0);
    apiConfigService.downloadExcel(filter, tenantId, response);
  }

  @PutMapping("/updateStatus")
  @ResponseBody
  public ApiConfigDTO updateApiConfigStatus(@RequestParam("id") Long apiConfigId, @Valid @RequestBody UpdateApiConfigStatusRequest request) {
    ApiConfig apiConfigFound = apiConfigService.findById(apiConfigId);
    if (apiConfigFound == null) {
      throw new TransactionException("exception.apiConfig.config.not.found");
    }
    Long tenantId = getCurrentTenantId();
    if (!apiConfigFound.getTenantId().equals(tenantId)) {
      throw new PermissionDeniedException();
    }
    apiConfigFound.setIsActive(request.getIsActive());
    return mapToApiConfigDTO(apiConfigService.update(apiConfigFound));
  }

  private ApiConfigDTO mapToApiConfigDTO(ApiConfig apiConfig) {
    ApiConfigDTO dto = new ApiConfigDTO();
    BeanUtils.copyProperties(apiConfig, dto);
    dto.setApiId(apiConfig.getLookup().getId());
    dto.setCustomerId(apiConfig.getCustomer().getId());
    if(StringUtils.isNotBlank(apiConfig.getPassword())) {
      try {
        dto.setPassword(encryptUtils.decrypt(apiConfig.getPassword()));
      } catch(Exception e) {
        logger.error(e.getMessage());
      }
    }
    return dto;
  }

  @GetMapping("/checkExistApiConfig")
  public boolean checkExistApiConfig(ApiConfigFilter filter) {
    return apiConfigService.checkExistApiConfig(filter);
  }

}
