package com.y3technologies.masters.dto;

import com.querydsl.core.annotations.QueryInit;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrContactDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * PartnersDto
 *
 * @author Su Xia
 * @since 2019/10/29
 */
@Getter
@Setter
public class CustomerDto extends BaseDto {

	private Long tenantId;	

	private String partnerCode;

	@NotNull(message = "partnerName {notBlank.message}")
	private String partnerName;

	@NotNull(message = "Contact No. {notBlank.message}")
	@Schema(required = true)
	private String mobileNumber1;
	private String mobileNumber1CountryShortName;

	private String email;

	private AddrContact addrContact;
}