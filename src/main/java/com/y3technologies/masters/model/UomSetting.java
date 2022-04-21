package com.y3technologies.masters.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "UOM_SETTING", uniqueConstraints = {@UniqueConstraint(name = "UNQ_UOM_SETTING_1", columnNames = {"UOM","UOM_GROUP"})})
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_UOM_SETTING", allocationSize = 1)
public class UomSetting extends BaseEntity {

	@NotEmpty(message = "{notBlank.message}")
	@Column(name = "UOM")
	private String uom;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "UOM_GROUP")
	private String uomGroup;

	@Column(name = "REMARK")
	private String remark;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((remark == null) ? 0 : remark.hashCode());
		result = prime * result + ((uom == null) ? 0 : uom.hashCode());
		result = prime * result + ((uomGroup == null) ? 0 : uomGroup.hashCode());
		return result;
	}

	
}
