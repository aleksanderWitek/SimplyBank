package com.alex.repository;

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
                INSERT INTO
                bank_account_client(bank_account_id, client_id, create_date)
                VALUES (?, ?, ?)
                """;
        jdbcTemplate.update(query, bankAccountId, clientId, LocalDateTime.now());
    }

    @Override
    public void unlinkBankAccountToClient(Long bankAccountId, Long clientId) {
        String query = """
                UPDATE bank_account_client
                SET delete_date = ?
                WHERE bank_account_id = ? AND
                client_id = ? AND
                delete_date IS NULL
                """;
        jdbcTemplate.update(query, LocalDateTime.now(), bankAccountId, clientId);
    }

    @Override
    public List<Long> findBankAccountsIdLinkedToClientByClientId(Long clientId) {
        String query = """
                SELECT bank_account_id
                FROM bank_account_client
                WHERE client_id = ? AND delete_date IS NULL
                """;
        return jdbcTemplate.queryForList(query, Long.class, clientId);
    }

    @Override
    public int countActiveBankAccountsByClientIdForUpdate(Long clientId) {
        String query = """
                SELECT COUNT(*)
                FROM bank_account_client
                WHERE client_id = ? AND delete_date IS NULL
                FOR UPDATE
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, clientId);
        return count != null ? count : 0;
    }

    @Override
    public List<Long> findClientsIdLinkedToBankAccountByBankAccountId(Long bankAccountId) {
        String query = """
                SELECT client_id
                FROM bank_account_client
                WHERE bank_account_id = ? AND delete_date IS NULL
                """;
        return jdbcTemplate.queryForList(query, Long.class, bankAccountId);
    }
}
