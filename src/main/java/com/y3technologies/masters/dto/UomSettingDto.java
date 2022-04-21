package com.y3technologies.masters.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * PartnersDto
 *
 * @author Su Xia
 * @since 2019/10/29
 */
@Getter
@Setter
public class UomSettingDto extends BaseDto {
	
	private String uom;
	private String description;
	private String uomGroup;
	private String remark;
	
	private List<String> uomGroupList;
}