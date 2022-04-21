package com.y3technologies.masters.exception;

public class PermissionDeniedException extends TransactionException {

    public PermissionDeniedException() {
        super("exception.forbidden");
    }
}
