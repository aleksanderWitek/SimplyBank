package com.alex.repository;

import com.alex.dto.UserAccount;
import com.alex.repository.mapper.UserAccountRowMapper;
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
                INSERT INTO
                user_account(login, password, role, create_date)
                VALUES(?, ?, ?, ?)
                """;
        jdbcTemplate.update(query, userAccount.getLogin(), userAccount.getPassword(), userAccount.getRole().name(), userAccount.getCreateDate());
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        String query = """
                SELECT ua.id,
                ua.login,
                ua.password,
                ua.role,
                ua.create_date,
                ua.modify_date,
                ua.delete_date
                FROM user_account AS ua
                WHERE ua.id = ? AND ua.delete_date IS NULL
                """;
        List<UserAccount> results = jdbcTemplate.query(query, new UserAccountRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public Optional<UserAccount> findByLogin(String login) {
        String query = """
                SELECT ua.id,
                ua.login,
                ua.password,
                ua.role,
                ua.create_date,
                ua.modify_date,
                ua.delete_date
                FROM user_account AS ua
                WHERE ua.login = ? AND ua.delete_date IS NULL
                """;
        List<UserAccount> results = jdbcTemplate.query(query, new UserAccountRowMapper(), login);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public List<UserAccount> findAll() {
        String query = """
                SELECT ua.id,
                ua.login,
                ua.password,
                ua.role,
                ua.create_date,
                ua.modify_date,
                ua.delete_date
                FROM user_account AS ua
                WHERE ua.delete_date IS NULL
                """;
        return jdbcTemplate.query(query, new UserAccountRowMapper());
    }

    @Override
    public void updatePassword(Long id, String encodedNewPassword) {
        // UserAccountService.updatePassword/resetPassword call findById().orElseThrow before this method.
        String query = """
                UPDATE user_account
                SET password = ?,
                modify_date = ?
                WHERE id = ? AND delete_date IS NULL
               """;
        jdbcTemplate.update(query, encodedNewPassword, LocalDateTime.now(), id);
    }

    @Override
    public void deleteById(Long id) {
        // Invoked from Client/Employee deletion flows which already verified existence; no direct controller endpoint exposes this.
        String query = """
                UPDATE user_account
                SET delete_date = ?
                WHERE id = ? AND delete_date IS NULL
               """;
        jdbcTemplate.update(query, LocalDateTime.now(), id);
    }
}
