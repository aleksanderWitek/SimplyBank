package com.alex.repository;

import com.alex.dto.Employee;
import com.alex.repository.mapper.EmployeeRowMapper;
import com.alex.exception.DataAccessRuntimeException;
import com.alex.exception.EmployeeNotFoundRuntimeException;
import org.springframework.dao.DataAccessException;
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
        try {
            jdbcTemplate.update(query, employee.getFirstName(), employee.getLastName(), employee.getCreateDate());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<Employee> findById(Long id) {
        String query = """
                SELECT id, first_name, last_name, create_date, modify_date, delete_date
                FROM employee
                WHERE id = ? AND delete_date IS NULL
                """;
        try {
            List<Employee> results = jdbcTemplate.query(query, new EmployeeRowMapper(), id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Employee> findAll() {
        String query = """
                SELECT id, first_name, last_name, create_date, modify_date, delete_date
                FROM employee
                WHERE delete_date IS NULL
                ORDER BY id
                """;
        try {
            return jdbcTemplate.query(query, new EmployeeRowMapper());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
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
        try {
            int rowAffected = jdbcTemplate.update(query,
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getModifyDate(),
                    id);
            if(rowAffected == 0) {
                throw new EmployeeNotFoundRuntimeException("There is no Employee with provided id = " + id);
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        String query = """
                UPDATE employee
                SET delete_date = ?
                WHERE id = ? AND delete_date IS NULL
               """;
        try {
            int rowAffected = jdbcTemplate.update(query,
                    LocalDateTime.now(),
                    id);
            if(rowAffected == 0) {
                throw new EmployeeNotFoundRuntimeException("There is no Employee with provided id = " + id);
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }
}
