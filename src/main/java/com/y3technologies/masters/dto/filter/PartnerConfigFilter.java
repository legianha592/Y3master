package com.y3technologies.masters.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PartnerConfigFilter extends ListingFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long partnerId;
	
	private Boolean activeInd;

	private String configCode;


}
