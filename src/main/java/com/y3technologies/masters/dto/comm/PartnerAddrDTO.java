/**
 * 
 */
package com.y3technologies.masters.dto.comm;

import com.y3technologies.masters.dto.BaseDto;
import com.y3technologies.masters.model.comm.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author beekhon.org
 *
 */
@Data
public class PartnerAddrDTO extends BaseDto {
	//FOR CUSTOMER/TRANSPORTER/CONSIGNEE

	@NotNull(message = "Unit {notBlank.message}")
	@Schema(required = true)
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

	private String timezone;

	private BigDecimal latitude;
	private BigDecimal longitude;

	private Country country;
}
