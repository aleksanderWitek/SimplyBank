package main.java.com.alex.repository;

import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.DataAccessRuntimeException;
import main.java.com.alex.exception.UserAccountNotFoundRuntimeException;
import main.java.com.alex.repository.mapper.UserAccountRowMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class UserAccountRepository implements IUserAccountRepository{

    private final JdbcTemplate jdbcTemplate;
    private final ICommonJdbcRepository commonJdbcRepository;

    public UserAccountRepository(JdbcTemplate jdbcTemplate, ICommonJdbcRepository commonJdbcRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonJdbcRepository = commonJdbcRepository;
    }

    @Override
    public Long save(UserAccount userAccount) {
        String query = """
                INSERT INTO\s
                user_account(login, password, role, create_date)\s
                VALUES(?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.update(query, userAccount.getLogin(), userAccount.getPassword(), userAccount.getRole(), userAccount.getCreateDate());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        String query = """
                SELECT ua.id,\s
                ua.login,\s
                ua.password,\s
                ua.role,\s
                ua.create_date,\s
                ua.modify_date\s
                FROM user_account AS ua\s
                WHERE ua.id = ? AND ua.delete_date IS NULL
                """;
        try {
            UserAccount userAccount = jdbcTemplate.queryForObject(query, new UserAccountRowMapper(), id);
            return Optional.ofNullable(userAccount);
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<UserAccount> findAll() {
        String query = """
                SELECT ua.id,\s
                ua.login,\s
                ua.password,\s
                ua.role,\s
                ua.create_date,\s
                ua.modify_date\s
                FROM user_account AS ua\s
                WHERE ua.delete_date IS NULL
                """;
        try {
            return jdbcTemplate.query(query, new UserAccountRowMapper());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void updatePassword(Long id, String encodedNewPassword) {
        String query = """
                UPDATE user_account\s
                SET password = ?,\s
                modify_date = ?\s
                WHERE id = ? AND delete_date IS NULL
               """;
        try {
            int rowAffected = jdbcTemplate.update(query,
                    encodedNewPassword,
                    LocalDateTime.now(),
                    id);
            if(rowAffected == 0) {
                throw new UserAccountNotFoundRuntimeException("There is no User Account with provided id = " + id);
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        String query = """
                UPDATE user_account\s
                SET delete_date = ?\s
                WHERE id = ? AND delete_date IS NULL
               """;
        try {
            int rowAffected = jdbcTemplate.update(query,
                    LocalDateTime.now(),
                    id);
            if(rowAffected == 0) {
                throw new UserAccountNotFoundRuntimeException("There is no User Account with provided id = " + id);
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }
}
