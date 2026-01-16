package main.java.com.alex.service;

import main.java.com.alex.dto.Transaction;

import java.util.List;
import java.util.Optional;

public interface ITransaction {
    Transaction save(Transaction transaction);
    void update(Long id, Transaction transaction);
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll();
    void deleteById(Long id);
}
