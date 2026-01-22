package main.java.com.alex.mapper;

import main.java.com.alex.Currency;
import main.java.com.alex.TransactionType;
import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.dto.Transaction;
import main.java.com.alex.exception.SQLRuntimeException;
import main.java.com.alex.validation.BankAccountValidation;
import main.java.com.alex.validation.CurrencyValidation;
import main.java.com.alex.validation.DateValidation;
import main.java.com.alex.validation.TransactionValidation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TransactionRowMapper implements RowMapper<Transaction> {

    @Override
    public Transaction mapRow(ResultSet rs, int rowNum) {
        try {
            String transactionTypeStr = rs.getString("transaction_type");
            TransactionValidation.validateIfTransactionTypeIsCorrect(transactionTypeStr, "Empty value from database for: transaction_type");
            TransactionType transactionType = TransactionType.valueOf(transactionTypeStr.toUpperCase());

            String currencyStr = rs.getString("currency");
            CurrencyValidation.validateIfCurrencyIsCorrect(currencyStr, "Empty value from database for: currency");
            Currency currency = Currency.valueOf(currencyStr.toUpperCase());

            Long bankAccountIdFrom = rs.getLong("bank_account_id_from");
            Long bankAccountIdTo = rs.getLong("bank_account_id_to");

            BankAccountPair bankAccounts = validateAndCreateBankAccounts(
                    transactionType,
                    bankAccountIdFrom,
                    bankAccountIdTo
            );

            Timestamp createDate = rs.getTimestamp("create_date");
            DateValidation.validateIfDateIsCorrect(createDate, "Empty value from database for: create_date");

            Timestamp modifyDate = rs.getTimestamp("modify_date");
            Timestamp deleteDate = rs.getTimestamp("delete_date");

            return new Transaction(
                    rs.getLong("id"),
                    transactionType,
                    currency,
                    rs.getBigDecimal("amount"),
                    bankAccounts.from(),
                    bankAccounts.to(),
                    rs.getString("description"),
                    createDate.toLocalDateTime(),
                    modifyDate != null ? modifyDate.toLocalDateTime() : null,
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (IllegalArgumentException e) {
            throw new SQLRuntimeException("Invalid transaction type in database: " + e.getMessage());
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception: " + e.getMessage());
        }
    }

    private BankAccountPair validateAndCreateBankAccounts(
            TransactionType transactionType,
            Long bankAccountIdFrom,
            Long bankAccountIdTo) {

        BankAccount bankAccountFrom = null;
        BankAccount bankAccountTo = null;

        switch (transactionType) {
            case TRANSFER:
                BankAccountValidation.ensureBankAccountPresent(bankAccountIdFrom, "TRANSFER transaction must have bank_account_id_from");
                BankAccountValidation.ensureBankAccountPresent(bankAccountIdTo, "TRANSFER transaction must have bank_account_id_to");
                TransactionValidation.validateIfBankAccountsAreTheSameForTransaction(bankAccountIdFrom, bankAccountIdTo);
                //todo after creating BankAccountService use findById()
                bankAccountFrom = new BankAccount();
                bankAccountTo = new BankAccount();
                break;

            case DEPOSIT:
                BankAccountValidation.ensureBankAccountNotPresent(bankAccountIdFrom, "DEPOSIT transaction must not have bank_account_id_from");
                BankAccountValidation.ensureBankAccountPresent(bankAccountIdTo, "DEPOSIT transaction must have bank_account_id_to");
                //todo after creating BankAccountService use findById()
                bankAccountTo = new BankAccount();
                break;

            case WITHDRAWAL:
                BankAccountValidation.ensureBankAccountPresent(bankAccountIdFrom, "WITHDRAWAL transaction must have bank_account_id_from");
                BankAccountValidation.ensureBankAccountNotPresent(bankAccountIdTo, "WITHDRAWAL transaction must not have bank_account_id_to");
                //todo after creating BankAccountService use findById()
                bankAccountFrom = new BankAccount();
                break;
        }

        return new BankAccountPair(bankAccountFrom, bankAccountTo);
    }

    private record BankAccountPair(BankAccount from, BankAccount to) {}
}



