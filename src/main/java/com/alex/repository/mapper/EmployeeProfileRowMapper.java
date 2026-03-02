package com.alex.repository.mapper;

import com.alex.dto.EmployeeProfile;
import com.alex.exception.SQLRuntimeException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class EmployeeProfileRowMapper implements RowMapper<EmployeeProfile> {

    @Override
    public EmployeeProfile mapRow(ResultSet rs, int rowNum) {
        try {
            Timestamp employeeModifyDate = rs.getTimestamp("employee_modify_date");
            return new EmployeeProfile(
                    rs.getLong("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getTimestamp("employee_create_date").toLocalDateTime(),
                    employeeModifyDate != null ? employeeModifyDate.toLocalDateTime() : null,
                    rs.getLong("user_account_id"),
                    rs.getString("login"),
                    rs.getString("role"),
                    rs.getTimestamp("account_create_date").toLocalDateTime()
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception." + e.getMessage());
        }
    }
}
