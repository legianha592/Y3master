package com.y3technologies.masters.model.comm;

import lombok.Data;

/**
 * @author beekhon.ong
 */
@Data
public class AddrContact {

    private Long id;

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
