package com.y3technologies.masters.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * @author Sivasankari Subramaniam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlacesFilter extends ListingFilter {

    private static final long serialVersionUID = 1L;

    private Long consigneeId;
    private Long tenantId;
    private String person;
    private String mobileNumber1;
    private String email;
    private Long locationId;
    private String locCode;
    private String unit;
    private String street;
    private String street2;
    private String city;
    private String state;
    private String zipCode;
    private String countryShortName;

    public PlacesFilter(String person, String mobileNumber1, String email, Long locationId, String locName, String locCode, String unit, String street, String street2, String city, String state, String zipCode, String countryShortName) {
        this.person = person;
        this.mobileNumber1 = mobileNumber1;
        this.email = email;
        this.locationId = locationId;
        this.locCode = locCode;
        this.unit = unit;
        this.street = street;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.countryShortName = countryShortName;
    }
}