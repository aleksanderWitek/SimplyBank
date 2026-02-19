package com.alex.repository.mapper;

import com.alex.BankAccountType;
import com.alex.Currency;
import com.alex.dto.BankAccount;
import com.alex.exception.SQLRuntimeException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class BankAccountRowMapper implements RowMapper<BankAccount> {
    @Override
    public BankAccount mapRow(ResultSet rs, int rowNum) {
        try {
            BankAccountType bankAccountType = BankAccountType.valueOf(rs.getString("account_type").toUpperCase());
            Currency currency = Currency.valueOf(rs.getString("currency").toUpperCase());

            Timestamp modifyDate = rs.getTimestamp("modify_date");
            Timestamp deleteDate = rs.getTimestamp("delete_date");

            return new BankAccount(
                    rs.getLong("id"),
                    rs.getString("number"),
                    bankAccountType,
                    currency,
                    rs.getBigDecimal("balance"),
                    rs.getTimestamp("create_date").toLocalDateTime(),
                    modifyDate != null ? modifyDate.toLocalDateTime() : null,
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception." + e.getMessage());
        }
    }
}
