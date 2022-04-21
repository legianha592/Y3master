/**
 * 
 */
package com.y3technologies.masters.dto.comm;

import com.y3technologies.masters.dto.BaseDto;
import lombok.Data;

import javax.validation.constraints.Email;

/**
 * @author beekhon.org
 *
 */
@Data
public class PartnerAddrContactDTO extends BaseDto {
	//FOR CUSTOMER/TRANSPORTER/CONSIGNEE

	private String mobileNumber1;
	private String mobileNumber1CountryShortName;
	@Email(regexp = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$", message = "exception.email.invalid")
	private String email;
	private String person;
}
