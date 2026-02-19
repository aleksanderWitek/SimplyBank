package com.alex.repository;

import com.alex.exception.DataAccessRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class UserAccountClientRepository implements IUserAccountClientRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserAccountClientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void linkUserAccountToClient(Long userAccountId, Long clientId) {
        String query = """
                INSERT INTO
                user_account_client(user_account_id, client_id, create_date)
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
                UPDATE user_account_client
                SET delete_date = ?
                WHERE user_account_id = ? AND
                client_id = ? AND
                delete_date IS NULL
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
                SELECT user_account_id
                FROM user_account_client
                WHERE client_id = ? AND delete_date IS NULL
                """;
        try {
            List<Long> results = jdbcTemplate.queryForList(query, Long.class, clientId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }
}
