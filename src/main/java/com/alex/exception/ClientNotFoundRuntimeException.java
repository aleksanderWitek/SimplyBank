package com.alex.exception;

public class ClientNotFoundRuntimeException extends RuntimeException {
    public ClientNotFoundRuntimeException(String message) {
        super(message);
    }
}
