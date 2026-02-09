package com.alex.exception;

public class EmployeeNotFoundRuntimeException extends RuntimeException {
    public EmployeeNotFoundRuntimeException(String message) {
        super(message);
    }
}
