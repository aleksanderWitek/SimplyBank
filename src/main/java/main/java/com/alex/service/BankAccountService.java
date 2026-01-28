package main.java.com.alex.service;

import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.repository.IBankAccountRepository;
import main.java.com.alex.service.validation.BankAccountValidation;
import main.java.com.alex.service.validation.IdValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService implements IBankAccountService{

    private final IBankAccountRepository bankAccountRepository;

    public BankAccountService(IBankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional
    @Override
    public BankAccount save(BankAccount bankAccount) {
        BankAccountValidation.ensureBankAccountPresent(bankAccount);

        Long id = bankAccountRepository.save(bankAccount);
        return new BankAccount(id , bankAccount.getNumber(), bankAccount.getAccountType(), bankAccount.getCurrency(),
                bankAccount.getBalance(), bankAccount.getCreateDate());
    }

    @Transactional
    @Override
    public void updateBalanceById(Long id, BankAccount bankAccount) {
        IdValidation.ensureIdPresent(id);
        BankAccountValidation.ensureBankAccountPresent(bankAccount);

        bankAccountRepository.updateBalanceById(id, bankAccount);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<BankAccount> findById(Long id) {
        IdValidation.ensureIdPresent(id);

        return bankAccountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        IdValidation.ensureIdPresent(id);

        bankAccountRepository.deleteById(id);
    }
}
