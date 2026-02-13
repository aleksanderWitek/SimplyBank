package com.alex.repository;

import com.alex.dto.Transaction;

import java.util.List;
import java.util.Optional;

public interface ITransactionRepository {
    Long save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll();
    List<Transaction> findTransactionsByBankAccountFromId(Long bankAccountFromId);
    List<Transaction> findTransactionsByBankAccountToId(Long bankAccountToId);
    List<Transaction> findTransactionsBetweenBankAccounts(Long bankAccountFromId, Long bankAccountToId);
}
