package com.y3technologies.masters.exception;

import java.util.Map;

public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = -6418576759740040734L;
	private Map<String, Object[]> validationExceptions = new java.util.HashMap<String, Object[]>();

    public Map<String, Object[]> getValidationExceptions() {
        return validationExceptions;
    }

    public void setValidationExceptions(Map<String, Object[]> validationExceptions) {
        this.validationExceptions = validationExceptions;
    }

    public void addValidationException(String resourceKey) {
        this.validationExceptions.put(resourceKey, null);
    }

    public void addValidationException(String resourceKey, Object[] arguments) {
        this.validationExceptions.put(resourceKey, arguments);
    }

    public Boolean hasError() {
        return validationExceptions.size() > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

}
