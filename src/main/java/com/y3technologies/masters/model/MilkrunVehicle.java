package com.y3technologies.masters.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "MILKRUN_VEHICLE")
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_MILKRUN_VEHICLE", allocationSize = 1)
public class MilkrunVehicle extends BaseEntity {

	@Column(name = "TENANT_ID")
	private Long tenantId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMER_ID")
	@ToString.Exclude
	private Partners customer;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VEHICLE_ID")
	@ToString.Exclude
	private Vehicle vehicle;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DRIVER_ID")
	@ToString.Exclude
	private Driver driver;

	@JsonBackReference
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "milkrunVehicle", orphanRemoval = true)
	@ToString.Exclude
	private Set<MilkrunTrip> milkrunTrips = new HashSet<MilkrunTrip>();

	public void addTripLocation(MilkrunTrip milkrunTrip) {
		milkrunTrip.setMilkrunVehicle(this);
		this.milkrunTrips.add(milkrunTrip);
	}
}
