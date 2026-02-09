package main.java.com.alex.service;

import main.java.com.alex.Currency;
import main.java.com.alex.TransactionType;
import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.dto.Transaction;
import main.java.com.alex.exception.BankAccountNotFoundRuntimeException;
import main.java.com.alex.repository.ITransactionRepository;
import main.java.com.alex.service.validation.CurrencyValidation;
import main.java.com.alex.service.validation.IdValidation;
import main.java.com.alex.service.validation.TransactionValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService implements ITransactionService {

    private final ITransactionRepository transactionRepository;
    private final IBankAccountService bankAccountService;

    public TransactionService(ITransactionRepository transactionRepository,
                              IBankAccountService bankAccountService) {
        this.transactionRepository = transactionRepository;
        this.bankAccountService = bankAccountService;
    }

    @Transactional
    @Override
    public Transaction transfer(Long bankAccountFromId, Long bankAccountToId,
                                BigDecimal amount, String currency, String description) {
        IdValidation.ensureIdPresent(bankAccountFromId);
        IdValidation.ensureIdPresent(bankAccountToId);
        TransactionValidation.validateIfBankAccountsAreTheSameForTransaction(bankAccountFromId, bankAccountToId);
        CurrencyValidation.validateIfCurrencyIsCorrect(currency, "Invalid or not supported currency value");
        TransactionValidation.validateAmount(amount);

        BankAccount from = bankAccountService.findById(bankAccountFromId)
                .orElseThrow(() -> new BankAccountNotFoundRuntimeException(
                        "Bank account (from) not found with id: " + bankAccountFromId));
        BankAccount to = bankAccountService.findById(bankAccountToId)
                .orElseThrow(() -> new BankAccountNotFoundRuntimeException(
                        "Bank account (to) not found with id: " + bankAccountToId));

        TransactionValidation.validateSufficientBalance(from, amount);

        BankAccount updatedFrom = new BankAccount(from.getId(), from.getNumber(), from.getAccountType(),
                from.getCurrency(), from.getBalance().subtract(amount),
                from.getCreateDate(), LocalDateTime.now(), from.getDeleteDate());
        BankAccount updatedTo = new BankAccount(to.getId(), to.getNumber(), to.getAccountType(),
                to.getCurrency(), to.getBalance().add(amount),
                to.getCreateDate(), LocalDateTime.now(), to.getDeleteDate());

        bankAccountService.updateBalance(updatedFrom);
        bankAccountService.updateBalance(updatedTo);

        Transaction transaction = new Transaction(TransactionType.TRANSFER,
                Currency.valueOf(currency.toUpperCase()), amount, from, to,
                description, LocalDateTime.now());

        Long id = transactionRepository.save(transaction);
        return new Transaction(id, transaction.getTransactionType(), transaction.getCurrency(),
                transaction.getAmount(), transaction.getBankAccountFrom(), transaction.getBankAccountTo(),
                transaction.getDescription(), transaction.getCreateDate());
    }

    @Transactional
    @Override
    public Transaction deposit(Long bankAccountToId, BigDecimal amount,
                               String currency, String description) {
        IdValidation.ensureIdPresent(bankAccountToId);
        CurrencyValidation.validateIfCurrencyIsCorrect(currency, "Invalid or not supported currency value");
        TransactionValidation.validateAmount(amount);

        BankAccount to = bankAccountService.findById(bankAccountToId)
                .orElseThrow(() -> new BankAccountNotFoundRuntimeException(
                        "Bank account not found with id: " + bankAccountToId));

        BankAccount updatedTo = new BankAccount(to.getId(), to.getNumber(), to.getAccountType(),
                to.getCurrency(), to.getBalance().add(amount),
                to.getCreateDate(), LocalDateTime.now(), to.getDeleteDate());
        bankAccountService.updateBalance(updatedTo);

        Transaction transaction = new Transaction(TransactionType.DEPOSIT,
                Currency.valueOf(currency.toUpperCase()), amount, null, to,
                description, LocalDateTime.now());

        Long id = transactionRepository.save(transaction);
        return new Transaction(id, transaction.getTransactionType(), transaction.getCurrency(),
                transaction.getAmount(), transaction.getBankAccountFrom(), transaction.getBankAccountTo(),
                transaction.getDescription(), transaction.getCreateDate());
    }

    @Transactional
    @Override
    public Transaction withdraw(Long bankAccountFromId, BigDecimal amount,
                                String currency, String description) {
        IdValidation.ensureIdPresent(bankAccountFromId);
        CurrencyValidation.validateIfCurrencyIsCorrect(currency, "Invalid or not supported currency value");
        TransactionValidation.validateAmount(amount);

        BankAccount from = bankAccountService.findById(bankAccountFromId)
                .orElseThrow(() -> new BankAccountNotFoundRuntimeException(
                        "Bank account not found with id: " + bankAccountFromId));

        TransactionValidation.validateSufficientBalance(from, amount);

        BankAccount updatedFrom = new BankAccount(from.getId(), from.getNumber(), from.getAccountType(),
                from.getCurrency(), from.getBalance().subtract(amount),
                from.getCreateDate(), LocalDateTime.now(), from.getDeleteDate());
        bankAccountService.updateBalance(updatedFrom);

        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL,
                Currency.valueOf(currency.toUpperCase()), amount, from, null,
                description, LocalDateTime.now());

        Long id = transactionRepository.save(transaction);
        return new Transaction(id, transaction.getTransactionType(), transaction.getCurrency(),
                transaction.getAmount(), transaction.getBankAccountFrom(), transaction.getBankAccountTo(),
                transaction.getDescription(), transaction.getCreateDate());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Transaction> findById(Long id) {
        IdValidation.ensureIdPresent(id);
        return transactionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Transaction> findTransactionsByBankAccountFromId(Long bankAccountFromId) {
        IdValidation.ensureIdPresent(bankAccountFromId);
        return transactionRepository.findTransactionsByBankAccountFromId(bankAccountFromId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Transaction> findTransactionsByBankAccountToId(Long bankAccountToId) {
        IdValidation.ensureIdPresent(bankAccountToId);
        return transactionRepository.findTransactionsByBankAccountToId(bankAccountToId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Transaction> findTransactionsBetweenBankAccounts(Long bankAccountFromId, Long bankAccountToId) {
        IdValidation.ensureIdPresent(bankAccountFromId);
        IdValidation.ensureIdPresent(bankAccountToId);
        return transactionRepository.findTransactionsBetweenBankAccounts(bankAccountFromId, bankAccountToId);
    }
}
