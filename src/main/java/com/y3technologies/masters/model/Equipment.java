package com.y3technologies.masters.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import com.y3technologies.masters.util.ExcelColumn;

import lombok.Getter;
import lombok.Setter;

/**
 * Equipment
 *
 * @author Su Xia
 * @since 2019/11/26
 */
@Entity
@Table(name = "EQUIPMENT")
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_EQUIPMENT", allocationSize = 1)
@Getter
@Setter
public class Equipment extends BaseEntity{
	
	@Column(name = "TENANT_ID")
	private Long tenantId;
	
	//lookup
	@Column(name = "EQUIPMENT_TYPE")
	@NotEmpty(message = "{notBlank.message}")
	@ExcelColumn(value = "Equipment Type", col = 1)
	private String equipmentType;
	
	@Column(name = "UNIT_AIDC1")
	@NotEmpty(message = "{notBlank.message}")
	@ExcelColumn(value = "Unit AIDC1", col = 2)
	private String unitAidc1;
	
	@Column(name = "UNIT_AIDC2")
	@ExcelColumn(value = "Unit AIDC2", col = 3)
	private String unitAidc2;
	
	//lookup
	@Column(name = "UNIT_TYPE")
	@NotEmpty(message = "{notBlank.message}")
	@ExcelColumn(value = "Unit Type", col = 4)
	private String unitType;
	
	@Column(name = "TARE_WEIGHT")
	@ExcelColumn(value = "Tare Weight", col = 5)
	private BigDecimal tareWeight;
	
	@Column(name = "MAX_WEIGHT")
	@ExcelColumn(value = "Max Weight", col = 6)
	private BigDecimal maxWeight;
	
	@Column(name = "VOLUMN")
	@ExcelColumn(value = "Volumn", col = 7)
	private BigDecimal volumn;
	
	@Column(name = "UNIT_OWNER")
//	@ExcelColumn(value = "Unit Owner", col = 8)
	private String unitOwner;
	
	@Column(name = "REMARK")
	@ExcelColumn(value = "Remark", col = 8)
	private String remark;

	@Column(name = "PARTNER_ID")
	private Long partnerId;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((equipmentType == null) ? 0 : equipmentType.hashCode());
		result = prime * result + ((maxWeight == null) ? 0 : maxWeight.hashCode());
		result = prime * result + ((tareWeight == null) ? 0 : tareWeight.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		result = prime * result + ((unitAidc1 == null) ? 0 : unitAidc1.hashCode());
		result = prime * result + ((unitAidc2 == null) ? 0 : unitAidc2.hashCode());
		result = prime * result + ((unitOwner == null) ? 0 : unitOwner.hashCode());
		result = prime * result + ((unitType == null) ? 0 : unitType.hashCode());
		result = prime * result + ((volumn == null) ? 0 : volumn.hashCode());
		result = prime * result + ((remark == null) ? 0 : remark.hashCode());
		return result;
	}
	
}