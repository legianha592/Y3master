package com.y3technologies.masters.dto.apiConfig;

import com.y3technologies.masters.model.apiConfig.AuthenticationType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiConfigExcelDTO {
  private Long id;
  private Long apiId;
  private String apiName;
  private String apiDesc;
  private String customerName;
  private Long customerId;
  private String url;
  private String urn;
  private String description;
  private Long tenantId;
  private AuthenticationType authenType;
  private String apiKey;
  private String username;
  private String password;
  private Boolean activeInd;
}
