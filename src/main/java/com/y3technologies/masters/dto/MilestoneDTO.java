package com.y3technologies.masters.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Setter
@Getter
public class MilestoneDTO extends BaseDto {

	private static final long serialVersionUID = 1L;
	
	private Long id;

	private Long tenantId;

	@NotBlank
	private String milestoneCode;

	private String milestoneCodeDescription;

	@NotBlank
	private String milestoneDescription;
	
	private String customerDescription;

	private String milestoneGroup;

	// tag1|tag2|tag3
//	@NotBlank
	private String milestoneCategory;

	private Boolean isInternal = true;

	private Integer sequence;

	private Boolean isDefault = Boolean.FALSE;

	private String external;

	private Integer hashcode;

	@NotEmpty
	@Size(min = 1)
	@Valid
	private List<MilestoneStatusConfigDTO> milestoneStatusConfigDtoList;

	private String enableMilestoneStatus;

	public void setIsInternal (String external) {
		if (external == null){
			this.isInternal = true;
		}
		else if (external.equalsIgnoreCase("yes")){
			this.isInternal = false;
		}
		else if (external.equalsIgnoreCase("no")) {
			this.isInternal = true;
		}
	}
}
