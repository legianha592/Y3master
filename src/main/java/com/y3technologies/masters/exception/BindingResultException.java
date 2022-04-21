package com.y3technologies.masters.exception;

import org.springframework.validation.BindingResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BindingResultException extends RuntimeException {

	private BindingResult bindingResult;

	private static final long serialVersionUID = 1L;

	public BindingResultException() {
		super();
	}

	public BindingResultException(BindingResult bindingResult) {
		super();
		this.bindingResult = bindingResult;
	}
}
