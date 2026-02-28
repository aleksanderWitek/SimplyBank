package com.alex.service.validation;

import com.alex.dto.BankAccount;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.IllegalStateRuntimeException;

import java.math.BigDecimal;

public class TransactionValidation {

    public static void validateDescriptionLength(String description) {
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentRuntimeException("Description must not exceed 255 characters");
        }
    }

    public static void validateIfBankAccountsAreTheSameForTransaction(Long bankAccountIdFrom,
                                                                      Long bankAccountIdTo) {
        if (bankAccountIdFrom.equals(bankAccountIdTo)) {
            throw new IllegalArgumentRuntimeException("Transaction cannot have same from and to accounts");
        }
    }

    public static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentRuntimeException("Amount must be greater than zero");
        }
    }

    public static void validateSufficientBalance(BankAccount bankAccount, BigDecimal amount) {
        if (bankAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateRuntimeException("Insufficient balance on account: " + bankAccount.getNumber());
        }
    }

    public static void validateCurrencyMatch(BankAccount account, String currency) {
        if (currency == null || account.getCurrency() == null) {
            return;
        }
        if (!account.getCurrency().name().equalsIgnoreCase(currency)) {
            throw new IllegalArgumentRuntimeException(
                    "Currency mismatch: transaction currency " + currency.toUpperCase()
                    + " does not match account currency " + account.getCurrency().name());
        }
    }
}
