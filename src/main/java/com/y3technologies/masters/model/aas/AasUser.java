package com.y3technologies.masters.model.aas;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AasUser implements Serializable {
	
	

	private static final long serialVersionUID = 1L;

	private String email;

	private String firstName;

	private String lastName;

	private Long phoneCountryId;

	private String phone;

	private String password;

	private Long countryId;

	private String timezone;

	private String languageCode;

	private String userType;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
	private boolean isVerified;

	private String imageName;
}
