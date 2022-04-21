package com.y3technologies.masters.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Shao Hui
 * @since 2019-10-31
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class VehicleDto extends BaseDto{
  private Long vehicleId;
  private String vehicleRegNumber;
  private String vehicleType;
  private Boolean available;
  private String licenceTypeRequired;
  private Long tenantId;
  private Long assetDefaultLocId;
  private String locName;
  private String locCode;
  private String locDesc;
  
  private Long defaultDriverId;
  private String defaultDriverName;
  private BigDecimal wt;
  private BigDecimal vol;
  private Integer pkgs;
  private BigDecimal costPerKm;

  private Long transporterId;
  private String transporterName;
}
