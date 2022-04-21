package com.y3technologies.masters.dto.filter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


@Setter
@Getter
public class LocationFilter extends ListingFilter implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean activeInd;
	private Long tenantId;

	private String locCode;

	private String locName;

	private List<String> locNames;

	private String address;

	private String zipCode;

	private String locationTag;

	private List<Long> ids;

	private Boolean isAddrAndContact = Boolean.TRUE;
}
