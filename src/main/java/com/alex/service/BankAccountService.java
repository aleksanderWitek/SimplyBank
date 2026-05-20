package com.alex.service;

import com.alex.BankAccountType;
import com.alex.Currency;
import com.alex.dto.BankAccount;
import com.alex.dto.ClientProfile;
import com.alex.exception.IllegalStateRuntimeException;
import com.alex.repository.IBankAccountClientRepository;
import com.alex.repository.IBankAccountRepository;
import com.alex.service.validation.BankAccountValidation;
import com.alex.service.validation.CurrencyValidation;
import com.alex.service.validation.IdValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService implements IBankAccountService{

    private final IBankAccountRepository bankAccountRepository;
    private final IBankAccountClientRepository bankAccountClientRepository;
    private final IClientService clientService;
    private final SecureRandom secureRandom;
    private static final int MAX_GENERATION_ATTEMPTS = 100;
    public static final int MAX_BANK_ACCOUNTS_PER_CLIENT = 10;

    public BankAccountService(IBankAccountRepository bankAccountRepository,
                              IBankAccountClientRepository bankAccountClientRepository, IClientService clientService, SecureRandom secureRandom) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountClientRepository = bankAccountClientRepository;
        this.clientService = clientService;
        this.secureRandom = secureRandom;
    }

    @Transactional
    @Override
    public BankAccount save(Long clientId, String bankAccountType, String bankAccountCurrency) {
        IdValidation.ensureIdPresent(clientId);
        BankAccountValidation.validateIfBankAccountTypeIsCorrect(bankAccountType, "Invalid bank account type");
        CurrencyValidation.validateIfCurrencyIsCorrect(bankAccountCurrency, "Invalid or not supported currency value");

        int existing = bankAccountClientRepository.countActiveBankAccountsByClientIdForUpdate(clientId);
        if (existing >= MAX_BANK_ACCOUNTS_PER_CLIENT) {
            throw new IllegalStateRuntimeException(
                    "Bank account limit reached (" + MAX_BANK_ACCOUNTS_PER_CLIENT + " per client)");
        }

        BankAccountType accountType = BankAccountType.valueOf(bankAccountType);
        Currency currency = Currency.valueOf(bankAccountCurrency);
        String uniqueNumber = generateUniqueBankAccountNumber();

        BankAccount bankAccountWithCreateDate = new BankAccount(
                uniqueNumber,
                accountType,
                currency,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
        Long id = bankAccountRepository.save(bankAccountWithCreateDate);
        BankAccount saveBankAccount = new BankAccount(
                id,
                bankAccountWithCreateDate.getNumber(),
                bankAccountWithCreateDate.getAccountType(),
                bankAccountWithCreateDate.getCurrency(),
                bankAccountWithCreateDate.getBalance(),
                bankAccountWithCreateDate.getCreateDate()
        );
        bankAccountClientRepository.linkBankAccountToClient(id, clientId);
        return saveBankAccount;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<BankAccount> findById(Long id) {
        IdValidation.ensureIdPresent(id);
        return bankAccountRepository.findById(id);
    }

    @Transactional
    @Override
    public Optional<BankAccount> findByIdForUpdate(Long id) {
        IdValidation.ensureIdPresent(id);
        return bankAccountRepository.findByIdForUpdate(id);
    }

    @Transactional
    @Override
    public void addToBalance(Long id, BigDecimal amount) {
        IdValidation.ensureIdPresent(id);
        bankAccountRepository.addToBalance(id, amount);
    }

    @Transactional
    @Override
    public void subtractFromBalance(Long id, BigDecimal amount) {
        IdValidation.ensureIdPresent(id);
        bankAccountRepository.subtractFromBalance(id, amount);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<BankAccount> findByNumber(String number) {
        if (number == null || number.isBlank()) {
            return Optional.empty();
        }
        return bankAccountRepository.findByNumber(number);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankAccount> findByClientId(Long clientId) {
        IdValidation.ensureIdPresent(clientId);
        List<Long> accountIds = bankAccountClientRepository
                .findBankAccountsIdLinkedToClientByClientId(clientId);
        return accountIds.stream()
                .map(bankAccountRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientProfile> findOwnersByBankAccountId(Long bankAccountId) {
        IdValidation.ensureIdPresent(bankAccountId);
        List<Long> clientIds = bankAccountClientRepository
                .findClientsIdLinkedToBankAccountByBankAccountId(bankAccountId);
        return clientIds.stream()
                .map(clientService::findProfileById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        IdValidation.ensureIdPresent(id);
        List<Long> clientsId = bankAccountClientRepository.findClientsIdLinkedToBankAccountByBankAccountId(id);
        clientsId.forEach(clientId -> bankAccountClientRepository.unlinkBankAccountToClient(id, clientId));
        bankAccountRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public String generateUniqueBankAccountNumber() {
        int attempts = 0;

        while (attempts < MAX_GENERATION_ATTEMPTS) {
            String accountNumber = generateRandomAccountNumber();
            if (!bankAccountRepository.existsByNumber(accountNumber)) {
                return accountNumber;
            }
            attempts++;
        }
        throw new IllegalStateRuntimeException("Unable to generate unique bank account number after "
                + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private String generateRandomAccountNumber() {
        StringBuilder accountNumber = new StringBuilder();

        accountNumber.append(secureRandom.nextInt(9) + 1);
        for (int i = 0; i < 11; i++) {
            accountNumber.append(secureRandom.nextInt(10));
        }

        return accountNumber.toString();
    }
}
