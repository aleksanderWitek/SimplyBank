package com.alex.repository;

import com.alex.dto.BankAccount;
import com.alex.repository.mapper.BankAccountRowMapper;
import com.alex.exception.BankAccountNotFoundRuntimeException;
import com.alex.exception.DataAccessRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class BankAccountRepository implements IBankAccountRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ICommonJdbcRepository commonJdbcRepository;

    public BankAccountRepository(JdbcTemplate jdbcTemplate, ICommonJdbcRepository commonJdbcRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonJdbcRepository = commonJdbcRepository;
    }

    @Override
    public Long save(BankAccount bankAccount) {
        String query = """
                INSERT INTO
                bank_account(number, account_type, currency, balance, create_date)
                VALUES(?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.update(query, bankAccount.getNumber(), bankAccount.getAccountType(), bankAccount.getCurrency(),
                    bankAccount.getBalance(), bankAccount.getCreateDate());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<BankAccount> findById(Long id) {
        String query = """
                SELECT ba.id,
                ba.number,
                ba.account_type,
                ba.currency,
                ba.balance,
                ba.create_date
                FROM bank_account AS ba
                WHERE ba.id = ? AND ba.delete_date IS NULL
                """;
        try {
            List<BankAccount> results = jdbcTemplate.query(query, new BankAccountRowMapper(), id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<BankAccount> findAll() {
        String query = """
                SELECT ba.id,
                ba.number,
                ba.account_type,
                ba.currency,
                ba.balance,
                ba.create_date
                FROM bank_account AS ba
                WHERE ba.delete_date IS NULL
                """;
        try {
            return jdbcTemplate.query(query, new BankAccountRowMapper());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void updateBalance(BankAccount bankAccount) {
        String query = """
                UPDATE bank_account
                SET balance = ?,
                modify_date = ?
                WHERE id = ? AND delete_date IS NULL
                """;
        try {
            int rowAffected = jdbcTemplate.update(query,
                    bankAccount.getBalance(),
                    bankAccount.getModifyDate(),
                    bankAccount.getId());
            if(rowAffected == 0) {
                throw new BankAccountNotFoundRuntimeException("There is no Bank Account with provided id = " + bankAccount.getId());
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        String query = """
                UPDATE bank_account
                SET delete_date = ?
                WHERE id = ? AND delete_date IS NULL
                """;
        try {
            int rowAffected = jdbcTemplate.update(query,
                    LocalDateTime.now(),
                    id);
            if(rowAffected == 0) {
                throw new BankAccountNotFoundRuntimeException("There is no Bank Account with provided id = " + id);
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByNumber(String number) {
        String query = """
            SELECT COUNT(*)
            FROM bank_account
            WHERE number = ? AND delete_date IS NULL
           """;
        try {
            Integer count = jdbcTemplate.queryForObject(query, Integer.class, number);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }
}
