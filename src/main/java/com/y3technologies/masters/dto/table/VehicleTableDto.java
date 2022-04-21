package com.y3technologies.masters.dto.table;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author Shao Hui
 * @since 2020-02-10
 */
@Getter
@Setter
public class VehicleTableDto extends BaseTableDto{
  
  private String vehicleRegNumber;
  private String vehicleType;
  private String licenceTypeRequired;
  private String assetDefaultLoc;
  private Long transporterId;
  private String transporterName;
  private String vehicleTypeSort;
  private String licenseTypeRequiredSort;

  public String getVehicleTypeSort() {
    if (Objects.isNull(this.vehicleType)) {
      return "";
    }
    return this.vehicleType.toLowerCase();
  }

  public String getLicenseTypeRequiredSort() {
    if (Objects.isNull(this.licenceTypeRequired)){
      return "";
    }
    return this.licenceTypeRequired.toLowerCase();
  }
}
