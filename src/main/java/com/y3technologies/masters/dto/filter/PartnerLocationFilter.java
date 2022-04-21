package com.y3technologies.masters.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerLocationFilter extends ListingFilter implements Serializable{

	private static final long serialVersionUID = 1L;
	private Long tenantId;
	private Boolean activeInd;
	private Long consigneeId;
	private Long locationId;
	private Long addressId;
	private Long addressContactId;
	private String locationIdAndPartnerId;

	private List<Long> consigneeIdList;

	public PartnerLocationFilter(Long tenandId, Long consigneeId, Long locationId, Long addressId, Long addressContactId) {
		this.tenantId = tenandId;
		this.consigneeId = consigneeId;
		this.locationId = locationId;
		this.addressId = addressId;
		this.addressContactId = addressContactId;
	}

	public PartnerLocationFilter(Long tenantId, List<Long> consigneeIdList) {
		this.tenantId = tenantId;
		this.consigneeIdList = consigneeIdList;
	}
}
