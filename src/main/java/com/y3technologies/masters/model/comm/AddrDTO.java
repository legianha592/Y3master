package com.y3technologies.masters.model.comm;

import java.io.Serializable;
import java.math.BigDecimal;

import com.y3technologies.masters.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

/**
 * @author beekhon.ong
 */
@Setter
@Getter
public class AddrDTO extends BaseDto {

    private String unit;

    private String street;

    private String street2;

    private String city;

    private String state;

    private String zipCode;

    private boolean isActive;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String countryShortName;
    
    private Country country;
    
    private String timezone;
}
