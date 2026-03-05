package com.alex.exception;

public class SecurityRuntimeException extends RuntimeException {
    public SecurityRuntimeException(String message) {
        super(message);
    }

    public SecurityRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
