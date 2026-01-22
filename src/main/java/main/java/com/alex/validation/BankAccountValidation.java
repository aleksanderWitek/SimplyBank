package main.java.com.alex.validation;

import main.java.com.alex.BankAccountType;
import main.java.com.alex.UserAccountRole;
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

    public static void validateIfBankAccountTypeIsCorrect(String bankAccountType, String message){
        if (bankAccountType == null || bankAccountType.isBlank()) {
            throw new NullPointerRuntimeException(message);
        }

        try {
            BankAccountType.valueOf(bankAccountType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SQLRuntimeException(message);
        }
    }
}
