package com.y3technologies.masters.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Shao Hui
 * @since 2019-10-31
 */
@Entity
@Getter
@Setter
@Table(name = "VEHICLE", uniqueConstraints = {@UniqueConstraint(name = "UNQ_VEHICLE_1", columnNames = {"VEHICLE_REG_NUMBER","TENANT_ID"})})
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_VEHICLE", allocationSize = 1)
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class Vehicle extends BaseEntity {

	@NotEmpty(message = "{notBlank.message}")
	@Column(name = "VEHICLE_REG_NUMBER")
	private String vehicleRegNumber;

	@NotEmpty(message = "{notBlank.message}")
	@Column(name = "VEHICLE_TYPE")
	private String vehicleType;

	@Column(name = "AVAILABLE")
	private Boolean available;

	@Column(name = "LICENCE_TYPE_REQUIRED")
	private String licenceTypeRequired;

	@Column(name = "TENANT_ID")
	private Long tenantId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEFAULT_DRIVER_ID")
	private Driver defaultDriver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASSET_DEFAULT_LOC_ID")
	private Location assetDefaultLoc;

	@Column(name = "WT")
	private BigDecimal wt;

	@Column(name = "VOL")
	private BigDecimal vol;

	@Column(name = "PKGS")
	private Integer pkgs;

	@Column(name = "COST_PERKM")
	private BigDecimal costPerKm;

	@OneToOne
	@JoinColumn(name = "TRANSPORTER_ID")
	@ToString.Exclude
	private Partners partners;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assetDefaultLoc == null||assetDefaultLoc.getId()==null) ? 0 : assetDefaultLoc.getId().hashCode());
		result = prime * result + ((available == null) ? 0 : available.hashCode());
		result = prime * result + ((defaultDriver == null||defaultDriver.getId()==null) ? 0 : defaultDriver.getId().hashCode());
		result = prime * result + ((licenceTypeRequired == null) ? 0 : licenceTypeRequired.hashCode());
		result = prime * result + ((pkgs == null) ? 0 : pkgs.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		result = prime * result + ((vehicleRegNumber == null) ? 0 : vehicleRegNumber.hashCode());
		result = prime * result + ((vehicleType == null) ? 0 : vehicleType.hashCode());
		result = prime * result + ((vol == null) ? 0 : vol.hashCode());
		result = prime * result + ((wt == null) ? 0 : wt.hashCode());
		return result;
	}
	
	

}
