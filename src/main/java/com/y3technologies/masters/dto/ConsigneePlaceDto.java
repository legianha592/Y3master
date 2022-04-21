package com.y3technologies.masters.dto;

import com.y3technologies.masters.dto.comm.PartnerAddrContactDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrDTO;
import lombok.Data;


/**
 * @author Sivasankari Subramaniam
 */
@Data
public class ConsigneePlaceDto extends BaseDto {

    private Long consigneeId;

    private Long locationId;

    private String locCode;

    private PartnerAddrContactDTO partnerAddrContact;

    private PartnerAddrDTO partnerAddr;

    private String locName;

}
