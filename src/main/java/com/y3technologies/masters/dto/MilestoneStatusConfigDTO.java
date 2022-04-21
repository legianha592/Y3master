package com.y3technologies.masters.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MilestoneStatusConfigDTO extends BaseDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	@NotNull
	private Long lookupId;
	
	private String lookupCode;
	
	private Boolean activated = false;

	protected Long version;
}
