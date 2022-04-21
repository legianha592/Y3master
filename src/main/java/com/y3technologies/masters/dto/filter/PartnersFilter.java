package com.y3technologies.masters.dto.filter;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartnersFilter extends ListingFilter implements Serializable{

	private static final long serialVersionUID = 1L;
	private List<Long> partnerIdList;
	private Long tenantId;
	private String name;
	private List<String> partnerNames;
	private String partnerType;
	private Boolean activeInd;
	private String partnerCode;
	private Long customerId;
	private String customerName;
	private List<Long> customerIdList;
	private Boolean isCreateCustomer = Boolean.FALSE;
	private String exacName;

	public PartnersFilter(Long tenandId, String name, String partnerType) {
		this.tenantId = tenandId;
		this.name = name;
		this.partnerType = partnerType;
	}

	public PartnersFilter(Long tenantId, List<Long> partnerIdList, String partnerType) {
		this.tenantId = tenantId;
		this.partnerIdList = partnerIdList;
		this.partnerType = partnerType;
	}

	public PartnersFilter(Long tenantId, int pageNo, int pageSize) {
		this.setTenantId(tenantId);
		this.setPageNo(pageNo);
		this.setPageSize(pageSize);
	}

	public PartnersFilter(Long tenantId, String name, Long customerId) {
		this.setTenantId(tenantId);
		this.setName(name);
		this.setCustomerId(customerId);
	}

}
