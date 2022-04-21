package com.y3technologies.masters.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * PartnersDto
 *
 * @author Su Xia
 * @since 2019/10/29
 */
@Getter
@Setter
public class PartnersDto extends BaseDto {

	private static final long serialVersionUID = 1L;

	private Long tenantId;	

	private String partnerCode;	
	
	@NotNull(message = "partnerName {notBlank.message}")
	private String partnerName;

	private String description;
	
	private String mobileNumber1;
	
	private String mobileNumber1CountryShortName;

	private String email;

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
	
	private AddrDTO addr;
	private AddrContact addrContact;

	@Valid
	private List<PartnerConfigDto> partnerConfigDtos;

	@NotNull(message = "partnerType {notBlank.message}")
    private List<Long> partnerTypeIds = null;

	private List<PartnerTypesDto> partnerTypeDtos;

}