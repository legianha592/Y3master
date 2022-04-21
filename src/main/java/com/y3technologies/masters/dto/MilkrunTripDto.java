package com.y3technologies.masters.dto;

import java.time.LocalTime;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MilkrunTripDto extends BaseDto {

	private Long milkrunVehicleId = 0l;
	@NotNull(message = "tripSequence {notBlank.message}")
	private int tripSequence;
	@NotNull(message = "visitSequence {notBlank.message}")
	private int visitSequence;
	@NotNull(message = "locationId {notBlank.message}")
	private Long locationId;
	@NotNull(message = "startTime {notBlank.message}")
	private LocalTime startTime;
	@NotNull(message = "endTime {notBlank.message}")
	private LocalTime endTime;
	@NotNull(message = "TPTRequestActivity {notBlank.message}")
	private String TPTRequestActivity;
	@NotNull(message = "dayOfWeek {notBlank.message}")
	private String dayOfWeek;

}
