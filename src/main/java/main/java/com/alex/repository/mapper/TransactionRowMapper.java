package main.java.com.alex.repository.mapper;

import main.java.com.alex.BankAccountType;
import main.java.com.alex.Currency;
import main.java.com.alex.TransactionType;
import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.dto.Transaction;
import main.java.com.alex.exception.SQLRuntimeException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TransactionRowMapper implements RowMapper<Transaction> {

    @Override
    public Transaction mapRow(ResultSet rs, int rowNum) {
        try {
            String transactionTypeInput = rs.getString("transaction_type");
            TransactionType transactionType = TransactionType.valueOf(transactionTypeInput.toUpperCase());

            String currencyInput = rs.getString("currency");
            Currency currency = Currency.valueOf(currencyInput.toUpperCase());

            BankAccount bankAccountFrom = mapBankAccountFrom(rs);
            BankAccount bankAccountTo = mapBankAccountTo(rs);

            Timestamp createDate = rs.getTimestamp("create_date");
            Timestamp deleteDate = rs.getTimestamp("delete_date");

            return new Transaction(
                    rs.getLong("id"),
                    transactionType,
                    currency,
                    rs.getBigDecimal("amount"),
                    bankAccountFrom,
                    bankAccountTo,
                    rs.getString("description"),
                    createDate.toLocalDateTime(),
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception: " + e.getMessage());
        }
    }

    private BankAccount mapBankAccountFrom(ResultSet rs) {
        try {
            Long id = rs.getLong("baf_id");
            if (id == 0 || rs.wasNull()) {
                return null;
            }

            String accountTypeInput = rs.getString("baf_account_type");
            BankAccountType bankAccountType = BankAccountType.valueOf(accountTypeInput.toUpperCase());
            String currencyInput = rs.getString("baf_currency");
            Currency currency = Currency.valueOf(currencyInput.toUpperCase());

            Timestamp createDate = rs.getTimestamp("baf_create_date");
            Timestamp modifyDate = rs.getTimestamp("baf_modify_date");
            Timestamp deleteDate = rs.getTimestamp("baf_delete_date");

            return new BankAccount(
                    id,
                    rs.getString("baf_number"),
                    bankAccountType,
                    currency,
                    rs.getBigDecimal("baf_balance"),
                    createDate.toLocalDateTime(),
                    modifyDate != null ? modifyDate.toLocalDateTime() : null,
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception: " + e.getMessage());
        }
    }

    private BankAccount mapBankAccountTo(ResultSet rs) {
        try {
            Long id = rs.getLong("bat_id");
            if (id == 0 || rs.wasNull()) {
                return null;
            }

            String accountTypeInput = rs.getString("bat_account_type");
            BankAccountType bankAccountType = BankAccountType.valueOf(accountTypeInput.toUpperCase());
            String currencyInput = rs.getString("bat_currency");
            Currency currency = Currency.valueOf(currencyInput.toUpperCase());

            Timestamp createDate = rs.getTimestamp("bat_create_date");
            Timestamp modifyDate = rs.getTimestamp("bat_modify_date");
            Timestamp deleteDate = rs.getTimestamp("bat_delete_date");

            return new BankAccount(
                    id,
                    rs.getString("bat_number"),
                    bankAccountType,
                    currency,
                    rs.getBigDecimal("bat_balance"),
                    createDate.toLocalDateTime(),
                    modifyDate != null ? modifyDate.toLocalDateTime() : null,
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception: " + e.getMessage());
        }
    }
}



