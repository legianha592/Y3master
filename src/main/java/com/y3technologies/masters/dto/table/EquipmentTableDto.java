package com.y3technologies.masters.dto.table;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Shao Hui
 * @since 2020-02-10
 */

@Getter
@Setter
public class EquipmentTableDto extends BaseTableDto{
	
	private String unitAidc1;
	private String unitAidc2;
	private String equipmentType;
	private String unitType;

	private BigDecimal tareWeight;

	private BigDecimal maxWeight;

	private BigDecimal volumn;

	private String unitOwner;

	private String remark;

	private Long partnerId;

    public String getEquipmentTypeSort() {
		if (Objects.isNull(this.equipmentType)) {
			return "";
		}
		return this.equipmentType.toLowerCase();
    }

	public String getUnitTypeSort() {
		if (Objects.isNull(this.unitType)) {
			return "";
		}
		return this.unitType.toLowerCase();
	}
}