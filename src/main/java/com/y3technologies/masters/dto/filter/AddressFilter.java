package com.y3technologies.masters.dto.filter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.model.comm.AddrDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AddressFilter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<Long> addressIdList;

	private Boolean isORExpression;

	private String unit;

	private String street;

	private String street2;

	private String city;

	private String state;

	private String zipCode;

	private String countryShortName;

	public AddressFilter(String unit, String street, String street2, String city, String state, String zipCode, String countryShortName) {
		this.unit = unit;
		this.street = street;
		this.street2 = street2;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
		this.countryShortName = countryShortName;
	}

	public AddressFilter(String unit, String street, String street2, Boolean isORExpression) {
		this.unit = unit;
		this.street = street;
		this.street2 = street2;
		this.isORExpression = isORExpression;
	}

	public AddressFilter(List<Long> addressIdList) {
		this.addressIdList = addressIdList;
	}
}
