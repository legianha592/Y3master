package com.y3technologies.masters.dto.table;

import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Shao Hui
 * @since 2020-02-10
 */
@Getter
@Setter
public class LocationTableDto extends BaseTableDto {

	private String locCode;
	
	private String locName;

	private AddrDTO addr;

	private List<Lookup> locationTags;

	private String zipCode;


}
