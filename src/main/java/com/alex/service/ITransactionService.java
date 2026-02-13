package com.alex.service;

import com.alex.dto.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ITransactionService {
    Transaction transfer(Long bankAccountFromId, Long bankAccountToId, BigDecimal amount, String currency, String description);
    Transaction deposit(Long bankAccountToId, BigDecimal amount, String currency, String description);
    Transaction withdraw(Long bankAccountFromId, BigDecimal amount, String currency, String description);
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll();
    List<Transaction> findTransactionsByBankAccountFromId(Long bankAccountFromId);
    List<Transaction> findTransactionsByBankAccountToId(Long bankAccountToId);
    List<Transaction> findTransactionsBetweenBankAccounts(Long bankAccountFromId, Long bankAccountToId);
}
