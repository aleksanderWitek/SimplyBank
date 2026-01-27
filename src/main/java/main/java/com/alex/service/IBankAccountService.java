package main.java.com.alex.service;

import main.java.com.alex.dto.BankAccount;

import java.util.List;
import java.util.Optional;

public interface IBankAccountService {
    BankAccount save(BankAccount bankAccount);
    void updateBalanceById(Long id, BankAccount bankAccount);
    Optional<BankAccount> findById(Long id);
    List<BankAccount> findAll();
    void deleteById(Long id);
}
