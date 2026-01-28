package main.java.com.alex.repository;

import main.java.com.alex.dto.Transaction;
import main.java.com.alex.exception.DataAccessRuntimeException;
import main.java.com.alex.repository.mapper.TransactionRowMapper;
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
                SELECT t.id,\s
                t.transaction_type,\s
                t.currency,\s
                t.amount,\s
                baf.number,\s
                bat.number,\s
                t.description,\s
                t.create_date\s
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
                SELECT t.id,\s
                t.transaction_type,\s
                t.currency,\s
                t.amount,\s
                baf.number,\s
                bat.number,\s
                t.description,\s
                t.create_date\s
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
}
