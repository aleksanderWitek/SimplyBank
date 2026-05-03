package com.alex;

import com.alex.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Shared base for all controller-level integration tests.
 *
 * <p>Each test class:
 * <ul>
 *     <li>runs with the "test" profile (MySQL {@code simplybank_test} database, see
 *     {@code application-test.properties});</li>
 *     <li>is wrapped in a rollback-only transaction — every test starts from an empty schema;</li>
 *     <li>uses {@link JdbcTemplate} for direct row inserts (no repository beans), so the tests
 *     exercise the same SQL as production without depending on service-layer behaviour for setup;</li>
 *     <li>generates real JWTs via the autowired {@link JwtService} so authenticated calls go through
 *     the full {@code JwtAuthenticationFilter} chain.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("DELETE FROM transaction");
        jdbcTemplate.execute("DELETE FROM bank_account_client");
        jdbcTemplate.execute("DELETE FROM bank_account");
        jdbcTemplate.execute("DELETE FROM user_account_employee");
        jdbcTemplate.execute("DELETE FROM user_account_client");
        jdbcTemplate.execute("DELETE FROM employee");
        jdbcTemplate.execute("DELETE FROM client");
        jdbcTemplate.execute("DELETE FROM user_account");
        jdbcTemplate.execute("ALTER TABLE transaction AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE bank_account_client AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE bank_account AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE user_account_employee AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE user_account_client AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE employee AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE client AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE user_account AUTO_INCREMENT = 1");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    protected String generateToken(String login, String role) {
        return jwtService.generateToken(login, role);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected void insertUserAccount(Long id, String login, String rawPassword, String role) {
        jdbcTemplate.update(
                "INSERT INTO user_account (id, login, password, role, create_date) VALUES (?, ?, ?, ?, ?)",
                id, login, passwordEncoder.encode(rawPassword), role, Timestamp.valueOf(LocalDateTime.now()));
    }

    protected void softDeleteUserAccount(Long id) {
        jdbcTemplate.update(
                "UPDATE user_account SET delete_date = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now()), id);
    }

    protected void insertClient(Long id, String firstName, String lastName,
                                String city, String street, String houseNumber,
                                String identificationNumber) {
        jdbcTemplate.update(
                "INSERT INTO client (id, first_name, last_name, city, street, house_number, identification_number, create_date)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, firstName, lastName, city, street, houseNumber, identificationNumber,
                Timestamp.valueOf(LocalDateTime.now()));
    }

    protected void insertEmployee(Long id, String firstName, String lastName) {
        jdbcTemplate.update(
                "INSERT INTO employee (id, first_name, last_name, create_date) VALUES (?, ?, ?, ?)",
                id, firstName, lastName, Timestamp.valueOf(LocalDateTime.now()));
    }

    protected void insertBankAccount(Long id, String number, String accountType,
                                     String currency, BigDecimal balance) {
        jdbcTemplate.update(
                "INSERT INTO bank_account (id, number, account_type, currency, balance, create_date)"
                        + " VALUES (?, ?, ?, ?, ?, ?)",
                id, number, accountType, currency, balance, Timestamp.valueOf(LocalDateTime.now()));
    }

    protected void softDeleteBankAccount(Long id) {
        jdbcTemplate.update(
                "UPDATE bank_account SET delete_date = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now()), id);
    }

    protected void linkUserAccountToClient(Long userAccountId, Long clientId) {
        jdbcTemplate.update(
                "INSERT INTO user_account_client (user_account_id, client_id, create_date) VALUES (?, ?, ?)",
                userAccountId, clientId, Timestamp.valueOf(LocalDateTime.now()));
    }

    protected void linkUserAccountToEmployee(Long userAccountId, Long employeeId) {
        jdbcTemplate.update(
                "INSERT INTO user_account_employee (user_account_id, employee_id, create_date) VALUES (?, ?, ?)",
                userAccountId, employeeId, Timestamp.valueOf(LocalDateTime.now()));
    }

    protected void linkBankAccountToClient(Long bankAccountId, Long clientId) {
        jdbcTemplate.update(
                "INSERT INTO bank_account_client (bank_account_id, client_id, create_date) VALUES (?, ?, ?)",
                bankAccountId, clientId, Timestamp.valueOf(LocalDateTime.now()));
    }

    protected BigDecimal balanceOf(Long bankAccountId) {
        return jdbcTemplate.queryForObject(
                "SELECT balance FROM bank_account WHERE id = ?", BigDecimal.class, bankAccountId);
    }
}
