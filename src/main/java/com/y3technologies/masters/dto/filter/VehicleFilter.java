package com.y3technologies.masters.dto.filter;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class VehicleFilter extends ListingFilter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long tenantId;
	private String vehicleRegisterNo;
	private List<Long> vehicleIdList;
	private Long transporterId;
	private Boolean activeInd;
	private Boolean assignedTransporter;
	private Long locId;
	private Long partnerId;
	private String vehicleRegNumber;
	private String vehicleType;
	private String licenceTypeRequired;
	private List<Long> transporterIdList;

	public VehicleFilter(Long tenantId, String vehicleRegisterNo) {
		this.tenantId = tenantId;
		this.vehicleRegisterNo = vehicleRegisterNo;
	}
	
	public VehicleFilter(Long tenantId, List<Long> vehicleIdList) {
		this.tenantId = tenantId;
		this.vehicleIdList = vehicleIdList;
	}
}

