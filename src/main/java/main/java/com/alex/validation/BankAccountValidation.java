package main.java.com.alex.validation;

import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.exception.NullPointerRuntimeException;
import main.java.com.alex.exception.SQLRuntimeException;

public class BankAccountValidation {

    public static void ensureBankAccountPresent(Long bankAccountId, String message) {
        if (bankAccountId == null) {
            throw new NullPointerRuntimeException(message);
        }
    }

    public static void ensureBankAccountNotPresent(Long bankAccountId, String message) {
        if (bankAccountId != null) {
            throw new SQLRuntimeException(message);
        }
    }
}
