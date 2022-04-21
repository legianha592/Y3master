package com.y3technologies.masters.dto.filter;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class LookupFilter extends ListingFilter {

	private static final long serialVersionUID = 1L;

	private Long tenantId;

	private String lookupType;

	private String lookupCode;

	private Boolean active = true;
	
	private List<Long> lookupIdList;

	public LookupFilter(List<Long> lookupIdList) {
		this.lookupIdList = lookupIdList;
	}

	private String lookupDescription;

	private List<String> lookupTypes;

	private List<String> lookupCodes;
}
