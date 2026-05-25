package com.alex.repository;

import com.alex.dto.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ITransactionRepository {
    Long save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll();
    List<Transaction> findTransactionsByBankAccountFromId(Long bankAccountFromId);
    List<Transaction> findTransactionsByBankAccountToId(Long bankAccountToId);
    List<Transaction> findTransactionsBetweenBankAccounts(Long bankAccountFromId, Long bankAccountToId);
    BigDecimal sumDepositsForAccountsBetween(Set<Long> bankAccountIds, LocalDateTime from, LocalDateTime toExclusive);
}
