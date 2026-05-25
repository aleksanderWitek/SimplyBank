package com.alex.repository;

import com.alex.dto.Transaction;
import com.alex.repository.mapper.TransactionRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class TransactionRepository implements ITransactionRepository{

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ICommonJdbcRepository commonJdbcRepository;

    private static final String BASE_QUERY = """
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

    public TransactionRepository(JdbcTemplate jdbcTemplate,
                                 NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                 ICommonJdbcRepository commonJdbcRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
        jdbcTemplate.update(query, transaction.getTransactionType().name(), transaction.getCurrency().name(),
                transaction.getAmount(),
                transaction.getBankAccountFrom() != null ? transaction.getBankAccountFrom().getId() : null,
                transaction.getBankAccountTo() != null ? transaction.getBankAccountTo().getId() : null,
                transaction.getDescription(), transaction.getCreateDate());
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String query = BASE_QUERY + " WHERE t.id = ?";
        List<Transaction> results = jdbcTemplate.query(query, new TransactionRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public List<Transaction> findAll() {
        String query = BASE_QUERY + " ORDER BY t.create_date DESC";
        return jdbcTemplate.query(query, new TransactionRowMapper());
    }

    @Override
    public List<Transaction> findTransactionsByBankAccountFromId(Long bankAccountFromId) {
        String query = BASE_QUERY + " WHERE t.bank_account_id_from = ? ORDER BY t.create_date DESC";
        return jdbcTemplate.query(query, new TransactionRowMapper(), bankAccountFromId);
    }

    @Override
    public List<Transaction> findTransactionsByBankAccountToId(Long bankAccountToId) {
        String query = BASE_QUERY + " WHERE t.bank_account_id_to = ? ORDER BY t.create_date DESC";
        return jdbcTemplate.query(query, new TransactionRowMapper(), bankAccountToId);
    }

    @Override
    public List<Transaction> findTransactionsBetweenBankAccounts(Long bankAccountFromId, Long bankAccountToId) {
        String query = BASE_QUERY + " WHERE t.bank_account_id_from = ? AND t.bank_account_id_to = ? ORDER BY t.create_date DESC";
        return jdbcTemplate.query(query, new TransactionRowMapper(), bankAccountFromId, bankAccountToId);
    }

    @Override
    public BigDecimal sumDepositsForAccountsBetween(Set<Long> bankAccountIds,
                                                    LocalDateTime from,
                                                    LocalDateTime toExclusive) {
        if (bankAccountIds == null || bankAccountIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        String query = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transaction
                WHERE transaction_type = 'DEPOSIT'
                  AND bank_account_id_to IN (:ids)
                  AND create_date >= :from
                  AND create_date <  :toExclusive
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", bankAccountIds)
                .addValue("from", from)
                .addValue("toExclusive", toExclusive);
        BigDecimal result = namedParameterJdbcTemplate.queryForObject(query, params, BigDecimal.class);
        return result == null ? BigDecimal.ZERO : result;
    }
}
