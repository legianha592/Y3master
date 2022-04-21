package com.y3technologies.masters.exception;

public class TransactionException extends RuntimeException {

	private static final long serialVersionUID = 2162914839192302185L;
	private Object[] arguments;

    public TransactionException(String resourceKey) {
        super(resourceKey);
    }

    public TransactionException(String resourceKey, Object[] arguments) {
        super(resourceKey);
        this.arguments = arguments;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
