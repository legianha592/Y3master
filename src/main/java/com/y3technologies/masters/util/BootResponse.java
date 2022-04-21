package com.y3technologies.masters.util;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class BootResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object data;
	private String result;
	private String message;
	private Integer code;

	public BootResponse() {
		this.result = ConstantVar.SUCCESS;
	}
	
	public BootResponse(Integer code) {
		this.result = ConstantVar.FAILURE;
		this.code = code;
		this.message = ConstantVar.ErrorMap.get(code);
	}
	
	public BootResponse(Exception e) {
		this.result = ConstantVar.FAILURE;
		this.code = -99;
		this.message = e.getMessage();
		if(e.getCause() != null) {
			this.message  = this.message +";"+ e.getCause().getCause().getMessage();
		}
	}

	public BootResponse(String errorMessage) {
		this.result = ConstantVar.FAILURE;
		this.message = errorMessage;
	}

	public BootResponse(List<ValidateErrorMsg> errorMessages) {
		this.result = ConstantVar.FAILURE;
		this.code = ConstantVar.VALID_CODE;
		this.data = errorMessages;
	}

}