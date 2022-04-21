package com.y3technologies.masters.dto.comm;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author beekhon.ong
 */
@Setter
@Getter
public class CountryDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotNull
	private Long id;
	
	@NotBlank
	private String countryShortName;

	@NotBlank
	private String countryFullName;
	
	@NotBlank
	private String countryIsdCode;
	
	private boolean isActive;
}
