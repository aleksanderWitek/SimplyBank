package main.java.com.alex.service.validation;

import main.java.com.alex.BankAccountType;
import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.exception.IllegalArgumentRuntimeException;
import main.java.com.alex.exception.IllegalStateRuntimeException;
import main.java.com.alex.exception.NullPointerRuntimeException;
import main.java.com.alex.exception.SQLRuntimeException;

public class BankAccountValidation {

    public static void ensureBankAccountPresent(BankAccount bankAccount) {
        if (bankAccount == null) {
            throw new NullPointerRuntimeException("Bank Account is null");
        }
    }

    public static void ensureBankAccountNotPresent(Long bankAccountId, String message) {
        if (bankAccountId != null) {
            throw new SQLRuntimeException(message);
        }
    }

    public static void validateIfBankAccountTypeIsCorrect(String bankAccountType, String message) {
        if (bankAccountType == null || bankAccountType.isBlank()) {
            throw new NullPointerRuntimeException(message);
        }

        try {
            BankAccountType.valueOf(bankAccountType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentRuntimeException(message + ": " + bankAccountType);
        }
    }
}
