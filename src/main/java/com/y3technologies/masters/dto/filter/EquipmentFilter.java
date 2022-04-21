package com.y3technologies.masters.dto.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
public class EquipmentFilter extends ListingFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    private String unitAidc1;
    private String unitAidc2;
    private String unitType;
    private String equipmentType;
}
