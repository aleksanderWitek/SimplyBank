package main.java.com.alex.repository;

import main.java.com.alex.exception.DataAccessRuntimeException;
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
                INSERT INTO\s
                user_account_employee(user_account_id, employee_id, create_date)\s
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
                UPDATE user_account_employee\s
                SET delete_date = ?\s
                WHERE user_account_id = ? AND\s
                employee_id = ? AND\s
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
                SELECT user_account_id\s
                FROM user_account_employee\s
                WHERE employee_id = ? AND delete_date IS NULL
                """;
        try {
            //todo check all other repositories and change it to return list too
            List<Long> results = jdbcTemplate.queryForList(query, Long.class, employeeId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }
}
