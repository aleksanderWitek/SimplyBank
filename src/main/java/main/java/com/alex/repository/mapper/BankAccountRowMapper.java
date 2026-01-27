package main.java.com.alex.repository.mapper;

import main.java.com.alex.BankAccountType;
import main.java.com.alex.Currency;
import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.exception.SQLRuntimeException;
import main.java.com.alex.service.validation.BankAccountValidation;
import main.java.com.alex.service.validation.CurrencyValidation;
import main.java.com.alex.service.validation.DateValidation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class BankAccountRowMapper implements RowMapper<BankAccount> {
    @Override
    public BankAccount mapRow(ResultSet rs, int rowNum) {
        try {
            String bankAccountTypeInput = rs.getString("account_type");
            //todo move validation to service. Validate only input data
            BankAccountValidation.validateIfBankAccountTypeIsCorrect(bankAccountTypeInput,
                    "Empty or invalid value from database for: account_type = " + bankAccountTypeInput);
            BankAccountType bankAccountType = BankAccountType.valueOf(bankAccountTypeInput.toUpperCase());

            String currencyInput = rs.getString("currency");
            CurrencyValidation.validateIfCurrencyIsCorrect(currencyInput,
                    "Empty or invalid value from database for: currency = " + currencyInput);
            Currency currency = Currency.valueOf(currencyInput.toUpperCase());

            Timestamp createDate = rs.getTimestamp("create_date");
            DateValidation.validateIfDateIsCorrect(createDate, "Empty value from database for: create_date");

            Timestamp modifyDate = rs.getTimestamp("modify_date");
            Timestamp deleteDate = rs.getTimestamp("delete_date");

            return new BankAccount(
                    rs.getLong("id"),
                    rs.getString("number"),
                    bankAccountType,
                    currency,
                    rs.getBigDecimal("balance"),
                    createDate.toLocalDateTime(),
                    modifyDate != null ? modifyDate.toLocalDateTime() : null,
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception." + e.getMessage());
        }
    }
}
