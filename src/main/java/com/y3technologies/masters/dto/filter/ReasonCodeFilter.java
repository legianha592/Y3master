package com.y3technologies.masters.dto.filter;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class ReasonCodeFilter extends ListingFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long tenantId;

	private String reasonCode;

	private String category;

	private String usage;

	private Boolean activeInd;
	
	private List<Long> reasonCodeIdList;

	private String reasonDescription;

	private String inducedBy;

	public ReasonCodeFilter(Long tenantId, List<Long> reasonCodeIdList) {
		this.tenantId = tenantId;
		this.reasonCodeIdList = reasonCodeIdList;
	}

}
