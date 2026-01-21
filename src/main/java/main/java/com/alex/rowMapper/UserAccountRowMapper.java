package main.java.com.alex.rowMapper;

import main.java.com.alex.UserAccountRole;
import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.NullPointerRuntimeException;
import main.java.com.alex.exception.SQLRuntimeException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserAccountRowMapper implements RowMapper<UserAccount> {
    @Override
    public UserAccount mapRow(ResultSet rs, int rowNum) {
        try {
            UserAccountRole role = switch (rs.getString("role").toUpperCase()) {
                case "ADMIN" -> UserAccountRole.ADMIN;
                case "EMPLOYEE" -> UserAccountRole.EMPLOYEE;
                case "CLIENT" -> UserAccountRole.CLIENT;
                default -> throw new SQLRuntimeException("Unknown role: " + rs.getString("role"));
            };
            Timestamp createDate = rs.getTimestamp("create_date");
            if(createDate == null) {
                throw new NullPointerRuntimeException("Empty value from database for: create_date");
            }
            Timestamp modifyDate = rs.getTimestamp("modify_date");
            Timestamp deleteDate = rs.getTimestamp("delete_date");
            return new UserAccount(
                    rs.getLong("id"),
                    rs.getString("login"),
                    rs.getString("password"),
                    role,
                    createDate.toLocalDateTime(),
                    modifyDate != null ? modifyDate.toLocalDateTime() : null,
                    deleteDate != null ? deleteDate.toLocalDateTime() : null
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception." + e.getMessage());
        }
    }
}
