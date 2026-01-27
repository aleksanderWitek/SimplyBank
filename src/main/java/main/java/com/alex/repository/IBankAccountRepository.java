package main.java.com.alex.repository;

import main.java.com.alex.dto.BankAccount;

import java.util.List;
import java.util.Optional;

public interface IBankAccountRepository {
    Long save(BankAccount bankAccount);
    Optional<BankAccount> findById(Long id);
    List<BankAccount> findAll();
    void update(Long id, BankAccount bankAccount);
    void deleteById(Long id);
}
