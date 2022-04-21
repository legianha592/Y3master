package com.y3technologies.masters.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.y3technologies.masters.model.CommonTag;
import com.querydsl.core.annotations.QueryInit;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Shao Hui
 * @since 2019-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LocationDto extends BaseDto {

	private String locCode;

	private String locDesc;


	@NotNull(message = "locName {notBlank.message}")
	private String locName;


	private Long tenantId;

	// Address Contact
	@NotNull(message = "Contact No. {notBlank.message}")
	@Schema(required = true)
	private String mobileNumber1;
	private String mobileNumber1CountryShortName;
	
	private String telephone;
	private String telephoneCountryShortName;
	private String telephoneAreaCode;
	
	private String fax;
	private String faxCountryShortName;
	private String faxAreaCode;
	
	private String email;
	private String person;
	
	private String unit;
	@NotNull(message = "Address Line 1 {notBlank.message}")
	@Schema(required = true)
	private String street;

	private String street2;

	private String city;

	private String state;
	@NotNull(message = "Postal Code / ZIP {notBlank.message}")
	@Schema(required = true)
	private String zipCode;
	@NotNull(message = "Country {notBlank.message}")
	@Schema(required = true)
	private String countryShortName;

	private BigDecimal latitude;
	private BigDecimal longitude;
	
	private AddrDTO addr;
	private AddrContact addrContact;

	private String locationTag;

	private String locationTagId;

	private List<CommonTag> locationTags;

	private String locContactEmail;

	private String locContactName;

	private String locContactPhone;

	private String multiPartnerAddresses;

	private Long addressId;

	private Long addressContactId;

	private String locContactOfficeNumber;

	private String streetSort;

	private String street2Sort;

	private String locationTagSort;

	private String zipCodeSort;

	private String locationTagsString;

	public String getLocationTagSort() {
		if (Objects.isNull(this.locationTag)) {
			return "";
		}
		return this.locationTag.toLowerCase();
	}

	public String getZipCodeSort() {
		if (Objects.isNull(this.zipCode)) {
			return "";
		}
		return this.zipCode.toLowerCase();
	}

	public String getStreetSort() {
		if (Objects.isNull(this.street)) {
			return "";
		}
		return this.street.toLowerCase();
	}

	public String getStreet2Sort() {
		if (Objects.isNull(this.street2)) {
			return "";
		}
		return this.street2.toLowerCase();
	}
}
