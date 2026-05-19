package com.alex.service;

import com.alex.dto.BankAccount;
import com.alex.dto.ClientProfile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IBankAccountService {
    BankAccount save(Long clientId, String bankAccountType, String bankAccountCurrency);
    Optional<BankAccount> findById(Long id);
    Optional<BankAccount> findByNumber(String number);
    Optional<BankAccount> findByIdForUpdate(Long id);
    void addToBalance(Long id, BigDecimal amount);
    void subtractFromBalance(Long id, BigDecimal amount);
    List<BankAccount> findAll();
    List<BankAccount> findByClientId(Long clientId);
    List<ClientProfile> findOwnersByBankAccountId(Long bankAccountId);
    void deleteById(Long id);
    String generateUniqueBankAccountNumber();
}
