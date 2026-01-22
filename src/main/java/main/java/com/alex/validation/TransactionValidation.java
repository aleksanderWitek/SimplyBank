package main.java.com.alex.validation;

import main.java.com.alex.exception.NullPointerRuntimeException;
import main.java.com.alex.exception.SQLRuntimeException;

public class TransactionValidation {

    public static void validateIfBankAccountsAreTheSameForTransaction(Long bankAccountIdFrom,
                                                                      Long bankAccountIdTo) {
        if (bankAccountIdFrom.equals(bankAccountIdTo)) {
            throw new SQLRuntimeException("Transaction cannot have same from and to accounts");
        }
    }

    public static void validateIfTransactionTypeIsCorrect(String transactionType, String message){
        if (transactionType == null || transactionType.isBlank()) {
            throw new NullPointerRuntimeException(message);
        }
    }
}
