package com.alex.exception;

public class UserAccountNotFoundRuntimeException extends RuntimeException {
    public UserAccountNotFoundRuntimeException(String message) {
        super(message);
    }
}
