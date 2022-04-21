/**
 * 
 */
package com.y3technologies.masters.dto.comm;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author beekhon.org
 *
 */
@Data
public class UpdateAddrDTO{

	private String unit;

	private String street;

	private String street2;

	private String city;

	private String state;

	private String zipCode;

	private String countryShortName;

	private BigDecimal latitude;
	private BigDecimal longitude;
}
