package com.y3technologies.masters.dto;

import com.querydsl.core.annotations.QueryInit;
import com.y3technologies.masters.model.comm.AddrDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
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
@AllArgsConstructor
@NoArgsConstructor
public class TransporterDto extends CustomerDto {

	@NotNull(message = "Customer Id {notBlank.message}")
	@Schema(required = true)
	private List<Long> customerIdList;

	private List<CusTomerRetrieveInf> cusTomerRetrieveInfs;

	@QueryInit("customers.partnerName")
	private String customerName;

	@NotEmpty(message = "Unit {notBlank.message}")
	@Schema(required = true)
	private String unit;

	@NotEmpty(message = "Street {notBlank.message}")
	@Schema(required = true)
	private String street;

	private String street2;

	private String city;

	private String state;

	@NotEmpty(message = "Street {notBlank.message}")
	@Schema(required = true)
	private String zipCode;

	@Schema(required = true)
	@NotEmpty(message = "Country {notBlank.message}")
	private String countryShortName;

	@NotNull(message = "Location Id {notBlank.message}")
	@Schema(required = true)
	private Long locationId;

	private String locationName;

	private AddrDTO addr;

	private String countryFullName;
}