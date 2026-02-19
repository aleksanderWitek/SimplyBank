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
                INSERT INTO
                transaction(transaction_type, currency, amount, bank_account_id_from, bank_account_id_to, description,
                create_date)
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
                SELECT
                    t.id,
                    t.transaction_type,
                    t.currency,
                    t.amount,
                    t.description,
                    t.create_date,
                    baf.id AS baf_id,
                    baf.number AS baf_number,
                    baf.account_type AS baf_account_type,
                    baf.currency AS baf_currency,
                    baf.balance AS baf_balance,
                    baf.create_date AS baf_create_date,
                    baf.modify_date AS baf_modify_date,
                    baf.delete_date AS baf_delete_date,
                    bat.id AS bat_id,
                    bat.number AS bat_number,
                    bat.account_type AS bat_account_type,
                    bat.currency AS bat_currency,
                    bat.balance AS bat_balance,
                    bat.create_date AS bat_create_date,
                    bat.modify_date AS bat_modify_date,
                    bat.delete_date AS bat_delete_date
                FROM transaction AS t
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id
                WHERE t.id = ?
               """;
        try {
            List<Transaction> results = jdbcTemplate.query(query, new TransactionRowMapper(), id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Transaction> findAll() {
        String query = """
                SELECT
                    t.id,
                    t.transaction_type,
                    t.currency,
                    t.amount,
                    t.description,
                    t.create_date,
                    baf.id AS baf_id,
                    baf.number AS baf_number,
                    baf.account_type AS baf_account_type,
                    baf.currency AS baf_currency,
                    baf.balance AS baf_balance,
                    baf.create_date AS baf_create_date,
                    baf.modify_date AS baf_modify_date,
                    baf.delete_date AS baf_delete_date,
                    bat.id AS bat_id,
                    bat.number AS bat_number,
                    bat.account_type AS bat_account_type,
                    bat.currency AS bat_currency,
                    bat.balance AS bat_balance,
                    bat.create_date AS bat_create_date,
                    bat.modify_date AS bat_modify_date,
                    bat.delete_date AS bat_delete_date
                FROM transaction AS t
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id
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
                SELECT
                    t.id,
                    t.transaction_type,
                    t.currency,
                    t.amount,
                    t.description,
                    t.create_date,
                    baf.id AS baf_id,
                    baf.number AS baf_number,
                    baf.account_type AS baf_account_type,
                    baf.currency AS baf_currency,
                    baf.balance AS baf_balance,
                    baf.create_date AS baf_create_date,
                    baf.modify_date AS baf_modify_date,
                    baf.delete_date AS baf_delete_date,
                    bat.id AS bat_id,
                    bat.number AS bat_number,
                    bat.account_type AS bat_account_type,
                    bat.currency AS bat_currency,
                    bat.balance AS bat_balance,
                    bat.create_date AS bat_create_date,
                    bat.modify_date AS bat_modify_date,
                    bat.delete_date AS bat_delete_date
                FROM transaction AS t
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id
                WHERE t.bank_account_id_from = ?
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
                SELECT
                    t.id,
                    t.transaction_type,
                    t.currency,
                    t.amount,
                    t.description,
                    t.create_date,
                    baf.id AS baf_id,
                    baf.number AS baf_number,
                    baf.account_type AS baf_account_type,
                    baf.currency AS baf_currency,
                    baf.balance AS baf_balance,
                    baf.create_date AS baf_create_date,
                    baf.modify_date AS baf_modify_date,
                    baf.delete_date AS baf_delete_date,
                    bat.id AS bat_id,
                    bat.number AS bat_number,
                    bat.account_type AS bat_account_type,
                    bat.currency AS bat_currency,
                    bat.balance AS bat_balance,
                    bat.create_date AS bat_create_date,
                    bat.modify_date AS bat_modify_date,
                    bat.delete_date AS bat_delete_date
                FROM transaction AS t
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id
                WHERE t.bank_account_id_to = ?
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
                SELECT
                    t.id,
                    t.transaction_type,
                    t.currency,
                    t.amount,
                    t.description,
                    t.create_date,
                    baf.id AS baf_id,
                    baf.number AS baf_number,
                    baf.account_type AS baf_account_type,
                    baf.currency AS baf_currency,
                    baf.balance AS baf_balance,
                    baf.create_date AS baf_create_date,
                    baf.modify_date AS baf_modify_date,
                    baf.delete_date AS baf_delete_date,
                    bat.id AS bat_id,
                    bat.number AS bat_number,
                    bat.account_type AS bat_account_type,
                    bat.currency AS bat_currency,
                    bat.balance AS bat_balance,
                    bat.create_date AS bat_create_date,
                    bat.modify_date AS bat_modify_date,
                    bat.delete_date AS bat_delete_date
                FROM transaction AS t
                LEFT JOIN bank_account AS baf ON t.bank_account_id_from = baf.id
                LEFT JOIN bank_account AS bat ON t.bank_account_id_to = bat.id
                WHERE t.bank_account_id_from = ?
                  AND t.bank_account_id_to = ?
               """;
        try {
            return jdbcTemplate.query(query, new TransactionRowMapper(), bankAccountFromId, bankAccountToId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }
}
