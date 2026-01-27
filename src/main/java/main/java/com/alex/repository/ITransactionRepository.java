package main.java.com.alex.repository;

import main.java.com.alex.dto.Transaction;

import java.util.List;
import java.util.Optional;

public interface ITransactionRepository {
    Long save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll();
}
