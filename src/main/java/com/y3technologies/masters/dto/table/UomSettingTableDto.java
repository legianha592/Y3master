package com.y3technologies.masters.dto.table;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author Shao Hui
 * @since 2020-03-3
 */
@Getter
@Setter
public class UomSettingTableDto extends BaseTableDto {
	
	private String uom;
	private String description;
	private String uomGroup;

    public String getUomGroupSort() {
    	if (Objects.isNull(this.uomGroup)){
    		return "";
		}
    	return this.uomGroup.toLowerCase();
    }
}