package com.alex.service;

import com.alex.dto.BankAccount;

import java.util.List;
import java.util.Optional;

public interface IBankAccountService {
    BankAccount save(Long clientId, String bankAccountType, String bankAccountCurrency);
    void updateBalance(BankAccount bankAccount);
    Optional<BankAccount> findById(Long id);
    List<BankAccount> findAll();
    void deleteById(Long id);
    String generateUniqueBankAccountNumber();
}
