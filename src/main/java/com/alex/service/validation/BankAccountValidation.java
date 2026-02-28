package com.alex.service.validation;

import com.alex.BankAccountType;
import com.alex.dto.BankAccount;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;

public class BankAccountValidation {

    public static void ensureBankAccountPresent(BankAccount bankAccount) {
        if (bankAccount == null) {
            throw new NullPointerRuntimeException("Bank Account is null");
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
