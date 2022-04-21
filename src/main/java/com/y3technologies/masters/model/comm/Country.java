package com.y3technologies.masters.model.comm;

import lombok.Data;

/**
 * @author beekhon.ong
 */
@Data
public class Country {

    private Long id;

    private String countryShortName;

    private String countryFullName;

    private String countryIsdCode;

}
