package main.java.com.alex.repository;

import main.java.com.alex.exception.DataAccessRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BankAccountClientRepository implements IBankAccountClientRepository {

    private final JdbcTemplate jdbcTemplate;

    public BankAccountClientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void linkBankAccountToClient(Long bankAccountId, Long clientId) {
        String query = """
                INSERT INTO\s
                bank_account_client(bank_account_id, client_id, create_date)\s
                VALUES (?, ?, ?)
                """;
        try {
            jdbcTemplate.update(query, bankAccountId, clientId, LocalDateTime.now());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }

    @Override
    public void unlinkBankAccountToClient(Long bankAccountId, Long clientId) {
        String query = """
                UPDATE bank_account_client\s
                SET delete_date = ?\s
                WHERE bank_account_id = ? AND\s
                client_id = ? AND\s
                delete_date IS NULL
                """;
        try {
            jdbcTemplate.update(query, LocalDateTime.now(), bankAccountId, clientId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }

    @Override
    public List<Long> findBankAccountsIdLinkedToClientByClientId(Long clientId) {
        String query = """
                SELECT bank_account_id\s
                FROM bank_account_client\s
                WHERE client_id = ? AND delete_date IS NULL
                """;
        try {
            return jdbcTemplate.queryForList(query, Long.class, clientId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }

    @Override
    public List<Long> findClientsIdLinkedToBankAccountByBankAccountId(Long bankAccountId) {
        String query = """
                SELECT client_id\s
                FROM bank_account_client\s
                WHERE bank_account_id = ? AND delete_date IS NULL
                """;
        try {
            return jdbcTemplate.queryForList(query, Long.class, bankAccountId);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
    }
}
