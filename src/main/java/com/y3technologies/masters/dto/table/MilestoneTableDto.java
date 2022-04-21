package com.y3technologies.masters.dto.table;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Shao Hui
 * @since 2020-02-10
 */
@Getter
@Setter
public class MilestoneTableDto extends BaseTableDto{
	
	
	private String milestoneCode;

	private String milestoneCodeDescription;
	
	private String milestoneDescription;
	
	private String milestoneCategory;
	
	private String customerDescription;
	
	private Boolean isInternal;

	private Boolean isDefault;
}