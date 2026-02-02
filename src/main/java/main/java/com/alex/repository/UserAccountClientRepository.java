package main.java.com.alex.repository;

import main.java.com.alex.exception.DataAccessRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

public class UserAccountClientRepository implements IUserAccountClientRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserAccountClientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void linkUserAccountToClient(Long userAccountId, Long clientId) {
        String query = """
                INSERT INTO\s
                user_account_client(user_account_id, client_id, create_date)\s
                VALUES (?, ?, ?)
                """;
        try {
            jdbcTemplate.update(query, userAccountId, clientId, LocalDateTime.now());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }

    @Override
    public void unlinkUserAccountFromClient(Long userAccountId, Long clientId) {
        String query = """
                UPDATE user_account_client\s
                SET delete_date = ?\s
                WHERE user_account_id = ? AND client_id = ? AND delete_date IS NULL
                """;
        try {
            jdbcTemplate.update(query, LocalDateTime.now(), userAccountId, clientId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }

    @Override
    public Optional<Long> findUserAccountIdByClientId(Long clientId) {
        String query = """
                SELECT user_account_id\s
                FROM user_account_client\s
                WHERE client_id = ?
                """;
        try {
            Long userAccountId = jdbcTemplate.queryForObject(query, Long.class, clientId);
            return Optional.ofNullable(userAccountId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }
}
