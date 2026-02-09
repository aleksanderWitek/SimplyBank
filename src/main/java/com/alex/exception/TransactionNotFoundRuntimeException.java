package com.alex.exception;

public class TransactionNotFoundRuntimeException extends RuntimeException {
    public TransactionNotFoundRuntimeException(String message) {
        super(message);
    }
}
