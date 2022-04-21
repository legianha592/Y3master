package com.y3technologies.masters.dto.filter;

import java.io.Serializable;
import java.util.List;

import com.y3technologies.masters.dto.comm.AddrContactDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class AddrContactFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String contactNo;

	private List<Long> addressContactIdList;

	private String person;

	private String email;

	public AddrContactFilter(String contactNo, String person, String email) {
		this.contactNo = contactNo;
		this.person = person;
		this.email = email;
	}

	public AddrContactFilter(String contactNo) {
		this.contactNo = contactNo;
	}

	public AddrContactFilter(List<Long> addressContactIdList) {
		this.addressContactIdList = addressContactIdList;
	}

}
