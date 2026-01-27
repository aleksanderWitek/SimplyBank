package main.java.com.alex.exception;

public class BankAccountNotFoundRuntimeException extends RuntimeException {
    public BankAccountNotFoundRuntimeException(String message) {
        super(message);
    }
}
