package com.y3technologies.masters.dto.apiConfig;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateApiConfigStatusRequest {

  @NotNull
  private Boolean isActive;

}
