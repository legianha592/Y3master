package com.y3technologies.masters.dto;

import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.comm.AddrDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Sivasankari Subramaniam
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CreateLocationDto extends BaseDto {

    private String locCode;

    private String locDesc;

    @NotNull(message = "locName {notBlank.message}")
    private String locName;

    private Long tenantId;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private AddrDTO addr;

    private String locationTag;

    private String locationTagId;

    private List<CommonTag> locationTags;

    private String street;

    private String street2;

    private String city;

    private String state;

    private String zipCode;

    private String countryShortName;

    private String locationTagsString;
}
