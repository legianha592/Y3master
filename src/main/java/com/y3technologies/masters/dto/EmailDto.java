package com.y3technologies.masters.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author beekhon.ong
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class EmailDto extends BaseDto{

	private String email;
}