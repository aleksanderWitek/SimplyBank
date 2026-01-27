package main.java.com.alex.service;

import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.repository.IBankAccountRepository;

import java.util.List;
import java.util.Optional;

public class BankAccountService implements IBankAccountService{

    private final IBankAccountRepository bankAccountRepository;

    public BankAccountService(IBankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public BankAccount save(BankAccount bankAccount) {
        Long id = bankAccountRepository.save(bankAccount);
        return new BankAccount(id , bankAccount.getNumber(), bankAccount.getAccountType(), bankAccount.getCurrency(),
                bankAccount.getBalance(), bankAccount.getCreateDate());
    }

    @Override
    public void updateBalanceById(Long id, BankAccount bankAccount) {
        bankAccountRepository.updateBalanceById(id, bankAccount);
    }

    @Override
    public Optional<BankAccount> findById(Long id) {
        return bankAccountRepository.findById(id);
    }

    @Override
    public List<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        bankAccountRepository.deleteById(id);
    }
}
