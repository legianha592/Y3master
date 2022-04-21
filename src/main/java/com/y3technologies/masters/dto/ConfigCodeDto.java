package com.y3technologies.masters.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author beekhon.ong
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigCodeDto extends BaseDto {

	private String code;

	private String description;

	@NotEmpty(message = "Usage Level {notBlank.message}")
	private String usageLevel;

	private String configValue;
	
}