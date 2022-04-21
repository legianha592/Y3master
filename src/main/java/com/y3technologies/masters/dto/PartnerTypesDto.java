package com.y3technologies.masters.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PartnerTypesDto
 *
 * @author Su Xia
 * @since 2019/10/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerTypesDto extends BaseDto{

    private Boolean activeInd = Boolean.TRUE;

    private Long partnerTypeId;

    private String partnerType;

    private Long version;


}