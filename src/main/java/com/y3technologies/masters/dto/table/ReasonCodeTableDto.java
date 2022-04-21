package com.y3technologies.masters.dto.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;

/**
 * @author Shao Hui
 * @since 2020-02-10
 */
@Getter
@Setter
public class ReasonCodeTableDto extends BaseTableDto{

	private String reasonCode;
	
	private String reasonDescription;
	
	private String category;

	private List<String> categories;
	
	private String usage;

	private List<String> usages;
	
	private String inducedBy;

	private Boolean isDefault;
	


}