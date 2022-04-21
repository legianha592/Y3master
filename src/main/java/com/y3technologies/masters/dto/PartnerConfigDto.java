package com.y3technologies.masters.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PartnersDto
 *
 * @author Su Xia
 * @since 2019/10/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerConfigDto extends BaseDto{

	private Long id;

	private Boolean activeInd = Boolean.TRUE;

	@NotEmpty(message = "Partner Config Value {notBlank.message}")
	@Schema(required = true)
	private String value;

	private String description;

	@NotNull(message = "Config Code Id {notBlank.message}")
	@Schema(required = true)
	private Long configCodeId;
	private String configCode;
	private String configDescription;

	@NotNull(message = "Partner Id {notBlank.message}")
	@Schema(required = true)
	private Long partnerId;

	private String partnerCode;
	private String partnerDescription;
	
	private Long version;

}