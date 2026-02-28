package com.alex.service;

import com.alex.dto.BankAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IBankAccountService {
    BankAccount save(Long clientId, String bankAccountType, String bankAccountCurrency);
    void updateBalance(BankAccount bankAccount);
    Optional<BankAccount> findById(Long id);
    Optional<BankAccount> findByIdForUpdate(Long id);
    void addToBalance(Long id, BigDecimal amount);
    void subtractFromBalance(Long id, BigDecimal amount);
    List<BankAccount> findAll();
    void deleteById(Long id);
    String generateUniqueBankAccountNumber();
}
