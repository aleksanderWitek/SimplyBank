package com.alex.repository.mapper;

import com.alex.UserAccountRole;
import com.alex.dto.UserAccount;
import com.alex.exception.SQLRuntimeException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserAccountRowMapper implements RowMapper<UserAccount> {
    @Override
    public UserAccount mapRow(ResultSet rs, int rowNum) {
        try {
            UserAccountRole userAccountRole = UserAccountRole.valueOf(rs.getString("role").toUpperCase());

            Timestamp modifyDate = rs.getTimestamp("modify_date");
            Timestamp deleteDate = rs.getTimestamp("delete_date");
            return new UserAccount(
                    rs.getLong("id"),
                    rs.getString("login"),
                    rs.getString("password"),
                    userAccountRole,
                    rs.getTimestamp("create_date").toLocalDateTime(),
                    modifyDate != null ? modifyDate.toLocalDateTime() : null,
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception." + e.getMessage());
        }
    }
}
