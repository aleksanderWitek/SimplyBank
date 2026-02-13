package com.alex.repository.mapper;

import com.alex.UserAccountRole;
import com.alex.dto.UserAccount;
import com.alex.exception.SQLRuntimeException;
import com.alex.service.validation.DateValidation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserAccountRowMapper implements RowMapper<UserAccount> {
    @Override
    public UserAccount mapRow(ResultSet rs, int rowNum) {
        try {
            //todo move validation to service. Validate only input data
            String userAccountRoleInput = rs.getString("role");
            UserAccountRole userAccountRole = UserAccountRole.valueOf(userAccountRoleInput.toUpperCase());

            Timestamp createDate = rs.getTimestamp("create_date");
            DateValidation.validateIfDateIsCorrect(createDate, "Empty value from database for: create_date");

            Timestamp modifyDate = rs.getTimestamp("modify_date");
            Timestamp deleteDate = rs.getTimestamp("delete_date");
            return new UserAccount(
                    rs.getLong("id"),
                    rs.getString("login"),
                    rs.getString("password"),
                    userAccountRole,
                    createDate.toLocalDateTime(),
                    modifyDate != null ? modifyDate.toLocalDateTime() : null,
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception." + e.getMessage());
        }
    }
}
