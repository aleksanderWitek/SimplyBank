package com.alex.repository;

import com.alex.dto.Transaction;
import com.alex.exception.DataAccessRuntimeException;
import com.alex.repository.mapper.TransactionRowMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TransactionRepository implements ITransactionRepository{

    private final JdbcTemplate jdbcTemplate;
    private final ICommonJdbcRepository commonJdbcRepository;

    public TransactionRepository(JdbcTemplate jdbcTemplate, ICommonJdbcRepository commonJdbcRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonJdbcRepository = commonJdbcRepository;
    }

    @Override
    public Long save(Transaction transaction) {
        String query = """
                INSERT INTO\s
                transaction(transaction_type, currency, amount, bank_account_id_from, bank_account_id_to, description,\s
                create_date)\s
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.update(query, transaction.getTransactionType(), transaction.getCurrency(),
                    transaction.getAmount(), transaction.getBankAccountFrom(), transaction.getBankAccountTo(),
                    transaction.getDescription(), transaction.getCreateDate());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String query = """
                SELECT\s
                    t.id,\s
                    t.transaction_type,\s
                    t.currency,\s
                    t.amount,\s
                    t.description,\s
                    t.create_date,\s
                    t.modify_date,\s
                    t.delete_date,\s
                    baf.id AS baf_id,\s
                    baf.number AS baf_number,\s
                    baf.account_type AS baf_account_type,\s
                    baf.currency AS baf_currency,\s
                    baf.balance AS baf_balance,\s
                    baf.create_date AS baf_create_date,\s
                    baf.modify_date AS baf_modify_date,\s
                    baf.delete_date AS baf_delete_date,\s
                    bat.id AS bat_id,\s
                    bat.number AS bat_number,\s
                    bat.account_type AS bat_account_type,\s
                    bat.currency AS bat_currency,\s
                    bat.balance AS bat_balance,\s
                    bat.create_date AS bat_create_date,\s
                    bat.modify_date AS bat_modify_date,\s
                    bat.delete_date AS bat_delete_date\s
                FROM transaction AS t\s
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id\s
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id\s
                WHERE t.id = ? AND t.delete_date IS NULL
               """;
        try {
            Transaction transaction = jdbcTemplate.queryForObject(query, new TransactionRowMapper(), id);
            return Optional.ofNullable(transaction);
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Transaction> findAll() {
        String query = """
                SELECT\s
                    t.id,\s
                    t.transaction_type,\s
                    t.currency,\s
                    t.amount,\s
                    t.description,\s
                    t.create_date,\s
                    t.modify_date,\s
                    t.delete_date,\s
                    baf.id AS baf_id,\s
                    baf.number AS baf_number,\s
                    baf.account_type AS baf_account_type,\s
                    baf.currency AS baf_currency,\s
                    baf.balance AS baf_balance,\s
                    baf.create_date AS baf_create_date,\s
                    baf.modify_date AS baf_modify_date,\s
                    baf.delete_date AS baf_delete_date,\s
                    bat.id AS bat_id,\s
                    bat.number AS bat_number,\s
                    bat.account_type AS bat_account_type,\s
                    bat.currency AS bat_currency,\s
                    bat.balance AS bat_balance,\s
                    bat.create_date AS bat_create_date,\s
                    bat.modify_date AS bat_modify_date,\s
                    bat.delete_date AS bat_delete_date\s
                FROM transaction AS t\s
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id\s
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id\s
                WHERE t.delete_date IS NULL
               """;
        try {
            return jdbcTemplate.query(query, new TransactionRowMapper());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Transaction> findTransactionsByBankAccountFromId(Long bankAccountFromId) {
        String query = """
                SELECT\s
                    t.id,\s
                    t.transaction_type,\s
                    t.currency,\s
                    t.amount,\s
                    t.description,\s
                    t.create_date,\s
                    t.modify_date,\s
                    t.delete_date,\s
                    baf.id AS baf_id,\s
                    baf.number AS baf_number,\s
                    baf.account_type AS baf_account_type,\s
                    baf.currency AS baf_currency,\s
                    baf.balance AS baf_balance,\s
                    baf.create_date AS baf_create_date,\s
                    baf.modify_date AS baf_modify_date,\s
                    baf.delete_date AS baf_delete_date,\s
                    bat.id AS bat_id,\s
                    bat.number AS bat_number,\s
                    bat.account_type AS bat_account_type,\s
                    bat.currency AS bat_currency,\s
                    bat.balance AS bat_balance,\s
                    bat.create_date AS bat_create_date,\s
                    bat.modify_date AS bat_modify_date,\s
                    bat.delete_date AS bat_delete_date\s
                FROM transaction AS t\s
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id\s
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id\s
                WHERE t.bank_account_id_from = ? AND t.delete_date IS NULL
               """;
        try {
            return jdbcTemplate.query(query, new TransactionRowMapper(), bankAccountFromId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Transaction> findTransactionsByBankAccountToId(Long bankAccountToId) {
        String query = """
                SELECT\s
                    t.id,\s
                    t.transaction_type,\s
                    t.currency,\s
                    t.amount,\s
                    t.description,\s
                    t.create_date,\s
                    t.modify_date,\s
                    t.delete_date,\s
                    baf.id AS baf_id,\s
                    baf.number AS baf_number,\s
                    baf.account_type AS baf_account_type,\s
                    baf.currency AS baf_currency,\s
                    baf.balance AS baf_balance,\s
                    baf.create_date AS baf_create_date,\s
                    baf.modify_date AS baf_modify_date,\s
                    baf.delete_date AS baf_delete_date,\s
                    bat.id AS bat_id,\s
                    bat.number AS bat_number,\s
                    bat.account_type AS bat_account_type,\s
                    bat.currency AS bat_currency,\s
                    bat.balance AS bat_balance,\s
                    bat.create_date AS bat_create_date,\s
                    bat.modify_date AS bat_modify_date,\s
                    bat.delete_date AS bat_delete_date\s
                FROM transaction AS t\s
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id\s
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id\s
                WHERE t.bank_account_id_to = ? AND t.delete_date IS NULL
               """;
        try {
            return jdbcTemplate.query(query, new TransactionRowMapper(), bankAccountToId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Transaction> findTransactionsBetweenBankAccounts(Long bankAccountFromId, Long bankAccountToId) {
        String query = """
                SELECT\s
                    t.id,\s
                    t.transaction_type,\s
                    t.currency,\s
                    t.amount,\s
                    t.description,\s
                    t.create_date,\s
                    t.modify_date,\s
                    t.delete_date,\s
                    baf.id AS baf_id,\s
                    baf.number AS baf_number,\s
                    baf.account_type AS baf_account_type,\s
                    baf.currency AS baf_currency,\s
                    baf.balance AS baf_balance,\s
                    baf.create_date AS baf_create_date,\s
                    baf.modify_date AS baf_modify_date,\s
                    baf.delete_date AS baf_delete_date,\s
                    bat.id AS bat_id,\s
                    bat.number AS bat_number,\s
                    bat.account_type AS bat_account_type,\s
                    bat.currency AS bat_currency,\s
                    bat.balance AS bat_balance,\s
                    bat.create_date AS bat_create_date,\s
                    bat.modify_date AS bat_modify_date,\s
                    bat.delete_date AS bat_delete_date\s
                FROM transaction AS t\s
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id\s
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id\s
                WHERE t.bank_account_id_from = ?\s
                  AND t.bank_account_id_to = ?\s
                  AND t.delete_date IS NULL
               """;
        try {
            return jdbcTemplate.query(query, new TransactionRowMapper(), bankAccountFromId, bankAccountToId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }
}
