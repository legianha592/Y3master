package com.y3technologies.masters.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MilkrunVehicleDto extends BaseDto {

	@NotNull(message = "tenantId {notBlank.message}")
	private Long tenantId;
	@NotNull(message = "customerId {notBlank.message}")
	private Long customerId;
	@NotNull(message = "vehicleId {notBlank.message}")
	private Long vehicleId;
	@NotNull(message = "driverId {notBlank.message}")
	private Long driverId;
	@Valid
	private List<MilkrunTripDto> tripDtolist;

}
