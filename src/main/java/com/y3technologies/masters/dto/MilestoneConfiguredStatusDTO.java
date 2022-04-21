package com.y3technologies.masters.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MilestoneConfiguredStatusDTO extends BaseDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private String milestoneCode;
	
	private List<String> configuredStatusList;
	
}
