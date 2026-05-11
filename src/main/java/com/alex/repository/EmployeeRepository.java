package com.alex.repository;

import com.alex.dto.Employee;
import com.alex.dto.EmployeeProfile;
import com.alex.repository.mapper.EmployeeProfileRowMapper;
import com.alex.repository.mapper.EmployeeRowMapper;
import com.alex.exception.EmployeeNotFoundRuntimeException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class EmployeeRepository implements IEmployeeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ICommonJdbcRepository commonJdbcRepository;

    public EmployeeRepository(JdbcTemplate jdbcTemplate, ICommonJdbcRepository commonJdbcRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonJdbcRepository = commonJdbcRepository;
    }

    @Override
    public Long save(Employee employee) {
        String query = """
                INSERT INTO
                employee(first_name, last_name, create_date)
                VALUES(?, ?, ?)
                """;
        jdbcTemplate.update(query, employee.getFirstName(), employee.getLastName(), employee.getCreateDate());
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<Employee> findById(Long id) {
        String query = """
                SELECT id, first_name, last_name, create_date, modify_date, delete_date
                FROM employee
                WHERE id = ? AND delete_date IS NULL
                """;
        List<Employee> results = jdbcTemplate.query(query, new EmployeeRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public List<Employee> findAll() {
        String query = """
                SELECT id, first_name, last_name, create_date, modify_date, delete_date
                FROM employee
                WHERE delete_date IS NULL
                ORDER BY id
                """;
        return jdbcTemplate.query(query, new EmployeeRowMapper());
    }

    @Override
    public void updateById(Long id, Employee employee) {
        String query = """
                UPDATE employee
                SET first_name = ?,
                last_name = ?,
                modify_date = ?
                WHERE id = ? AND delete_date IS NULL
               """;
        int rowAffected = jdbcTemplate.update(query,
                employee.getFirstName(),
                employee.getLastName(),
                employee.getModifyDate(),
                id);
        if(rowAffected == 0) {
            throw new EmployeeNotFoundRuntimeException("There is no Employee with provided id = " + id);
        }
    }

    @Override
    public void deleteById(Long id) {
        // EmployeeService.deleteById verifies existence via userAccountEmployeeRepository before this call.
        String query = """
                UPDATE employee
                SET delete_date = ?
                WHERE id = ? AND delete_date IS NULL
               """;
        jdbcTemplate.update(query, LocalDateTime.now(), id);
    }

    @Override
    public Optional<EmployeeProfile> findProfileByUserAccountId(Long userAccountId) {
        String query = """
                SELECT e.id            AS employee_id,
                       e.first_name,
                       e.last_name,
                       e.create_date   AS employee_create_date,
                       e.modify_date   AS employee_modify_date,
                       ua.id           AS user_account_id,
                       ua.login,
                       ua.role,
                       ua.create_date  AS account_create_date
                FROM employee e
                JOIN user_account_employee uae ON uae.employee_id = e.id
                JOIN user_account ua ON ua.id = uae.user_account_id
                WHERE ua.id = ?
                  AND e.delete_date IS NULL
                  AND ua.delete_date IS NULL
                  AND uae.delete_date IS NULL
                """;
        List<EmployeeProfile> results = jdbcTemplate.query(query, new EmployeeProfileRowMapper(), userAccountId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public Optional<EmployeeProfile> findProfileById(Long employeeId) {
        String query = """
                SELECT e.id            AS employee_id,
                       e.first_name,
                       e.last_name,
                       e.create_date   AS employee_create_date,
                       e.modify_date   AS employee_modify_date,
                       ua.id           AS user_account_id,
                       ua.login,
                       ua.role,
                       ua.create_date  AS account_create_date
                FROM employee e
                JOIN user_account_employee uae ON uae.employee_id = e.id
                JOIN user_account ua ON ua.id = uae.user_account_id
                WHERE e.id = ?
                  AND e.delete_date IS NULL
                  AND ua.delete_date IS NULL
                  AND uae.delete_date IS NULL
                """;
        List<EmployeeProfile> results = jdbcTemplate.query(query, new EmployeeProfileRowMapper(), employeeId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }
}
