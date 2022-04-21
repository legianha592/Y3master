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
public class DriverTableDto extends BaseTableDto {
	private String name;
	private String mobileNumber1;
	private String mobileNumber1CountryShortName;
	private String licenceType;
	private Long transporterId;
	private String transporterName;
	private String mobileNumber1Sort;
	private String licenseTypeSort;

	public String getMobileNumber1Sort() {
		if (Objects.isNull(this.mobileNumber1)) {
			return "";
		}
		return this.mobileNumber1.toLowerCase();
	}

	public String getLicenseTypeSort() {
		if (Objects.isNull(this.licenceType)) {
			return "";
		}
		return this.licenceType.toLowerCase();
	}
}
