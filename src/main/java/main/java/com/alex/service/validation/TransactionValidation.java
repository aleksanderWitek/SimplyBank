package main.java.com.alex.service.validation;

import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.exception.IllegalArgumentRuntimeException;
import main.java.com.alex.exception.IllegalStateRuntimeException;
import main.java.com.alex.exception.SQLRuntimeException;

import java.math.BigDecimal;

public class TransactionValidation {

    public static void validateIfBankAccountsAreTheSameForTransaction(Long bankAccountIdFrom,
                                                                      Long bankAccountIdTo) {
        if (bankAccountIdFrom.equals(bankAccountIdTo)) {
            throw new SQLRuntimeException("Transaction cannot have same from and to accounts");
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
}
