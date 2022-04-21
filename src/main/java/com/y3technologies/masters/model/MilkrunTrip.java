package com.y3technologies.masters.model;

import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "MILKRUN_TRIP")
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_MILKRUN_TRIP", allocationSize = 1)
public class MilkrunTrip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "MILKRUN_VEHICLE_ID")
    @ToString.Exclude
    private MilkrunVehicle milkrunVehicle;

    @Column(name = "TRIP_SEQUENCE")
    private int tripSequence;

    @Column(name = "VIST_SEQUENCE")
    private int visitSequence;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID")
    @ToString.Exclude
    private Location location;

    @Column(name = "START_TIME")
    private LocalTime startTime;

    @Column(name = "END_TIME")
    private LocalTime endTime;

    @Column(name = "TPT_REQUEST_ACTIVITY")
    private String TPTRequestActivity;

    @Column(name = "DAY_OF_WEEK")
    private String dayOfWeek;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((TPTRequestActivity == null) ? 0 : TPTRequestActivity.hashCode());
		result = prime * result + ((dayOfWeek == null) ? 0 : dayOfWeek.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + tripSequence;
		result = prime * result + visitSequence;
		return result;
	}
    
}
