package com.alex.repository;

import com.alex.dto.BankAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IBankAccountRepository {
    Long save(BankAccount bankAccount);
    Optional<BankAccount> findById(Long id);
    Optional<BankAccount> findByIdForUpdate(Long id);
    List<BankAccount> findAll();
    void updateBalance(BankAccount bankAccount);
    void addToBalance(Long id, BigDecimal amount);
    void subtractFromBalance(Long id, BigDecimal amount);
    void deleteById(Long id);
    boolean existsByNumber(String number);
}
