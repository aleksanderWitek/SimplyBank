package main.java.com.alex.service;

import main.java.com.alex.BankAccountType;
import main.java.com.alex.Currency;
import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.dto.Client;
import main.java.com.alex.exception.ClientNotFoundRuntimeException;
import main.java.com.alex.exception.IllegalStateRuntimeException;
import main.java.com.alex.repository.IBankAccountClientRepository;
import main.java.com.alex.repository.IBankAccountRepository;
import main.java.com.alex.service.validation.BankAccountValidation;
import main.java.com.alex.service.validation.CurrencyValidation;
import main.java.com.alex.service.validation.IdValidation;
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
        Client client = clientService.findById(clientId).orElseThrow(() ->
                new ClientNotFoundRuntimeException("There is no Client with id:" + clientId));
        client.addBankAccount(saveBankAccount);
        return saveBankAccount;
    }

    @Transactional
    @Override
    public void updateBalance(BankAccount bankAccount) {
        BankAccountValidation.ensureBankAccountPresent(bankAccount);
        bankAccountRepository.updateBalance(bankAccount);
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
