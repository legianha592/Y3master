package com.y3technologies.masters.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import lombok.Data;

@Data
public class ValidateErrorMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;
    private String errorValue;

    public static List<ValidateErrorMsg> getErrorMessages(BindingResult result) {
		List<FieldError> fieldErrors = result.getFieldErrors();
		List<ValidateErrorMsg> errorMsgs = new ArrayList<>();
		for (FieldError fieldError : fieldErrors) {
			String field = fieldError.getField();
			String defaultMessage = fieldError.getDefaultMessage();
			ValidateErrorMsg errorMsg = new ValidateErrorMsg();
			errorMsg.setField(field);
			errorMsg.setErrorValue(defaultMessage);
			errorMsgs.add(errorMsg);
		}
		return errorMsgs;
	}
}
