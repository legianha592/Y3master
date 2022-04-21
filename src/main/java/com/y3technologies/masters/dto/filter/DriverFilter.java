package com.y3technologies.masters.dto.filter;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DriverFilter extends ListingFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long tenantId;
	private String name;
	private String email;
	private String contactNo;
	private List<Long> driverIdList;
	private List<Long> transporterIdList;
	private List<String> driverEmailList;
	private Long transporterId;
	private Boolean activeInd;
	private Boolean assignedTransporter;
	private String licenceType;
	private String mobileNumber1;
	private Long partnerId;

	public DriverFilter(Long tenantId, String name, String contactNo) {
		this.tenantId = tenantId;
		this.name = name;
		this.contactNo = contactNo;
	}

	public DriverFilter(Long tenantId, List<Long> driverIdList) {
		this.tenantId = tenantId;
		this.driverIdList = driverIdList;
	}
	
	public DriverFilter(Long tenandId, String contactNo, List<Long> driverIdList) {
		this.tenantId = tenandId;
		this.contactNo = contactNo;
		this.driverIdList = driverIdList;
	}

	public DriverFilter(List<String> driverEmailList, Long tenantId) {
		this.tenantId = tenantId;
		this.driverEmailList = driverEmailList;
	}
}
