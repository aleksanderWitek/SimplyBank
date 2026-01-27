package main.java.com.alex.repository;

import main.java.com.alex.dto.Employee;
import main.java.com.alex.dto.mapper.EmployeeRowMapper;
import main.java.com.alex.exception.DataAccessRuntimeException;
import main.java.com.alex.exception.EmployeeNotFoundRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
                INSERT INTO\s
                employee(first_name, last_name, create_date)\s
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
                SELECT e.id,\s
                e.first_name,\s
                e.last_name,\s
                e.create_date,\s
                e.modify_date,\s
                ua.login,\s
                ua.role\s
                FROM employee AS e\s
                LEFT JOIN user_account_employee AS uae ON e.id = uae.employee_id\s
                LEFT JOIN user_account AS ua ON uae.user_account_id = ua.id\s
                WHERE e.id = ? AND e.delete_date IS NULL
                """;
        try {
            Employee employee = jdbcTemplate.queryForObject(query, new EmployeeRowMapper(), id);
            return Optional.ofNullable(employee);
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Employee> findAll() {
        String query = """
                SELECT e.id,\s
                e.first_name,\s
                e.last_name,\s
                e.create_date,\s
                e.modify_date,\s
                ua.login,\s
                ua.role\s
                FROM employee AS e\s
                LEFT JOIN user_account_employee AS uae ON e.id = uae.employee_id\s
                LEFT JOIN user_account AS ua ON uae.user_account_id = ua.id\s
                WHERE e.delete_date IS NULL\s
                ORDER BY e.id
                """;
        try {
            return jdbcTemplate.query(query, new EmployeeRowMapper());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void update(Long id, Employee employee) {
        String query = """
                UPDATE employee\s
                SET first_name = ?,\s
                last_name = ?,\s
                modify_date = ?\s
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
                UPDATE employee\s
                SET delete_date = ?\s
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
