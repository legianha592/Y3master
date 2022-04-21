/**
 * 
 */
package com.y3technologies.masters.dto.comm;

import com.y3technologies.masters.dto.BaseDto;
import lombok.Data;

import java.io.Serializable;

/**
 * @author beekhon.org
 *
 */
@Data
public class AddrContactDTO extends BaseDto {

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
}
