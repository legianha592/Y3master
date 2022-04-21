package com.y3technologies.masters.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LookupDto extends BaseDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long tenantId;

	@NotEmpty(message = "lookupType {notBlank.message}")
	private String lookupType;

	@NotEmpty(message = "lookupCode {notBlank.message}")
	private String lookupCode;

	@NotEmpty(message = "lookupDescription {notBlank.message}")
	private String lookupDescription;

	private Integer seq;

}
