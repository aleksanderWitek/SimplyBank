package com.alex.repository;

import com.alex.exception.DataAccessRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class UserAccountEmployeeRepository implements IUserAccountEmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserAccountEmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void linkUserAccountToEmployee(Long userAccountId, Long employeeId) {
        String query = """
                INSERT INTO
                user_account_employee(user_account_id, employee_id, create_date)
                VALUES (?, ?, ?)
                """;
        try {
            jdbcTemplate.update(query, userAccountId, employeeId, LocalDateTime.now());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }

    @Override
    public void unlinkUserAccountFromEmployee(Long userAccountId, Long employeeId) {
        String query = """
                UPDATE user_account_employee
                SET delete_date = ?
                WHERE user_account_id = ? AND
                employee_id = ? AND
                delete_date IS NULL
                """;
        try {
            jdbcTemplate.update(query, LocalDateTime.now(), userAccountId, employeeId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }

    @Override
    public Optional<Long> findUserAccountIdByEmployeeId(Long employeeId) {
        String query = """
                SELECT user_account_id
                FROM user_account_employee
                WHERE employee_id = ? AND delete_date IS NULL
                """;
        try {
            List<Long> results = jdbcTemplate.queryForList(query, Long.class, employeeId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }
}
