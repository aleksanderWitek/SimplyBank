package main.java.com.alex.repository;

import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.repository.mapper.BankAccountRowMapper;
import main.java.com.alex.exception.BankAccountNotFoundRuntimeException;
import main.java.com.alex.exception.DataAccessRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BankAccountRepository implements IBankAccountRepository{

    private final JdbcTemplate jdbcTemplate;
    private final ICommonJdbcRepository commonJdbcRepository;

    public BankAccountRepository(JdbcTemplate jdbcTemplate, ICommonJdbcRepository commonJdbcRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonJdbcRepository = commonJdbcRepository;
    }

    @Override
    public Long save(BankAccount bankAccount) {
        String query = """
                INSERT INTO\s
                bank_account(number, account_type, currency, balance, create_date)\s
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
                SELECT ba.number,\s
                ba.account_type,\s
                ba.currency,\s
                ba.balance,\s
                ba.create_date\s
                FROM bank_account AS ba\s
                WHERE ba.id = ? AND ba.delete_date IS NULL
                """;
        try {
            BankAccount bankAccount = jdbcTemplate.queryForObject(query, new BankAccountRowMapper(), id);
            return Optional.ofNullable(bankAccount);
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<BankAccount> findAll() {
        String query = """
                SELECT ba.number,\s
                ba.account_type,\s
                ba.currency,\s
                ba.balance,\s
                ba.create_date\s
                FROM bank_account AS ba\s
                WHERE ba.delete_date IS NULL
                """;
        try {
            return jdbcTemplate.query(query, new BankAccountRowMapper());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void updateBalanceById(Long id, BankAccount bankAccount) {
        String query = """
                UPDATE bank_account\s
                SET balance = ?,\s
                modify_date = ?\s
                WHERE id = ? AND delete_date IS NULL
                """;
        try {
            int rowAffected = jdbcTemplate.update(query,
                    bankAccount.getBalance(),
                    bankAccount.getModifyDate(),
                    id);
            if(rowAffected == 0) {
                throw new BankAccountNotFoundRuntimeException("There is no Bank Account with provided id = " + id);
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        String query = """
                UPDATE bank_account\s
                delete_date = ?\s
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
}
