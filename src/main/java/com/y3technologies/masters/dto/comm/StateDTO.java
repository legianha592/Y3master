package com.y3technologies.masters.dto.comm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author beekhon.ong
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StateDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;

	private String stateShortName;

	private String stateFullName;

	private boolean isActive;
	
	private String timezone;

}
