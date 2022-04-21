package com.y3technologies.masters.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.y3technologies.masters.model.comm.AddrContact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Shao Hui
 * @since 2019-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DriverDto extends BaseDto {
	@NotEmpty(message = "{notBlank.message}")
	private String name;

	// Address Contact
	@NotNull(message = "Contact No. {notBlank.message}")
	@Pattern(regexp = "^\\d{1,14}$", message = "{exception.phone.invalid}")
	@Schema(required = true)
	private String mobileNumber1;
	private String mobileNumber1CountryShortName;

	@Email(regexp = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$", message = "{exception.email.invalid}")
	private String email;
	private Boolean available;
	@NotEmpty(message = "{notBlank.message}")
	private String licenceType;
	@NotEmpty(message = "{notBlank.message}")
	private String licenceNumber;
	private Long tenantId;

	private AddrContact addrContact;

	private Long addrContactId;
	
	private Long userId;

	private Long transporterId;

	private String transporterName;

}
