package com.y3technologies.masters.dto.filter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


@Setter
@Getter
public class UomSettingFilter extends ListingFilter implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean activeInd;
	private List<Long> uomSettingIdList;
	private String description;
	private String uom;
	private String uomGroup;
}
