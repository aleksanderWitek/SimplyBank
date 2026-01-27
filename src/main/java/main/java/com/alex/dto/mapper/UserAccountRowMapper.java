package main.java.com.alex.dto.mapper;

import main.java.com.alex.UserAccountRole;
import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.SQLRuntimeException;
import main.java.com.alex.service.validation.DateValidation;
import main.java.com.alex.service.validation.UserAccountValidation;
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
            UserAccountValidation.validateIfUserAccountRoleIsCorrect(userAccountRoleInput,
                    "Empty or invalid value from database for: role = " + userAccountRoleInput);
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
