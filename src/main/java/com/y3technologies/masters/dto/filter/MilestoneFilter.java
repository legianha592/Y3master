package com.y3technologies.masters.dto.filter;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MilestoneFilter extends ListingFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long tenantId;

	private String milestoneCode;
	
	private Boolean active = true;

	private Boolean isMilestoneUpdate = Boolean.FALSE;

	private String customerDescription;

	private String milestoneCategory;

	private String milestoneDescription;

	private Boolean isInternal;
}
