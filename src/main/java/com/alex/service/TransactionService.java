package com.alex.service;

import com.alex.Currency;
import com.alex.TransactionType;
import com.alex.dto.BankAccount;
import com.alex.dto.Transaction;
import com.alex.exception.BankAccountNotFoundRuntimeException;
import com.alex.repository.ITransactionRepository;
import com.alex.service.validation.CurrencyValidation;
import com.alex.service.validation.IdValidation;
import com.alex.service.validation.TransactionValidation;
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
        validateTransferInputData(bankAccountFromId, bankAccountToId, amount, currency, description);

        BankAccount bankAccountFrom = bankAccountService.findById(bankAccountFromId)
                .orElseThrow(() -> new BankAccountNotFoundRuntimeException(
                        "Bank account (from) not found with id: " + bankAccountFromId));
        BankAccount bankAccountTo = bankAccountService.findById(bankAccountToId)
                .orElseThrow(() -> new BankAccountNotFoundRuntimeException(
                        "Bank account (to) not found with id: " + bankAccountToId));

        TransactionValidation.validateSufficientBalance(bankAccountFrom, amount);

        BankAccount updatedBankAccountFrom = new BankAccount(bankAccountFrom.getId(), bankAccountFrom.getNumber(), bankAccountFrom.getAccountType(),
                bankAccountFrom.getCurrency(), bankAccountFrom.getBalance().subtract(amount),
                bankAccountFrom.getCreateDate(), LocalDateTime.now(), bankAccountFrom.getDeleteDate());
        BankAccount updatedBankAccountTo = new BankAccount(bankAccountTo.getId(), bankAccountTo.getNumber(), bankAccountTo.getAccountType(),
                bankAccountTo.getCurrency(), bankAccountTo.getBalance().add(amount),
                bankAccountTo.getCreateDate(), LocalDateTime.now(), bankAccountTo.getDeleteDate());

        bankAccountService.updateBalance(updatedBankAccountFrom);
        bankAccountService.updateBalance(updatedBankAccountTo);

        Transaction transaction = new Transaction(TransactionType.TRANSFER,
                Currency.valueOf(currency.toUpperCase()), amount, bankAccountFrom, bankAccountTo,
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
        validateInputData(bankAccountToId, amount, currency, description);

        BankAccount bankAccountTo = bankAccountService.findById(bankAccountToId)
                .orElseThrow(() -> new BankAccountNotFoundRuntimeException(
                        "Bank account not found with id: " + bankAccountToId));

        BankAccount updatedBankAccountTo = new BankAccount(bankAccountTo.getId(), bankAccountTo.getNumber(),
                bankAccountTo.getAccountType(), bankAccountTo.getCurrency(), bankAccountTo.getBalance().add(amount),
                bankAccountTo.getCreateDate(), LocalDateTime.now(), bankAccountTo.getDeleteDate());
        bankAccountService.updateBalance(updatedBankAccountTo);

        Transaction transaction = new Transaction(TransactionType.DEPOSIT,
                Currency.valueOf(currency.toUpperCase()), amount, null, bankAccountTo,
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
        validateInputData(bankAccountFromId, amount, currency, description);

        BankAccount bankAccountFrom = bankAccountService.findById(bankAccountFromId)
                .orElseThrow(() -> new BankAccountNotFoundRuntimeException(
                        "Bank account not found with id: " + bankAccountFromId));

        TransactionValidation.validateSufficientBalance(bankAccountFrom, amount);

        BankAccount updatedBankAccountFrom = new BankAccount(bankAccountFrom.getId(), bankAccountFrom.getNumber(),
                bankAccountFrom.getAccountType(), bankAccountFrom.getCurrency(),
                bankAccountFrom.getBalance().subtract(amount), bankAccountFrom.getCreateDate(), LocalDateTime.now(),
                bankAccountFrom.getDeleteDate());
        bankAccountService.updateBalance(updatedBankAccountFrom);

        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL,
                Currency.valueOf(currency.toUpperCase()), amount, bankAccountFrom, null,
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

    private void validateTransferInputData(Long bankAccountFromId, Long bankAccountToId,
                                           BigDecimal amount, String currency, String description) {
        IdValidation.ensureIdPresent(bankAccountFromId);
        IdValidation.ensureIdPresent(bankAccountToId);
        TransactionValidation.validateIfBankAccountsAreTheSameForTransaction(bankAccountFromId, bankAccountToId);
        CurrencyValidation.validateIfCurrencyIsCorrect(currency, "Invalid or not supported currency value");
        TransactionValidation.validateAmount(amount);
        TransactionValidation.validateDescriptionLength(description);
    }

    private void validateInputData(Long bankAccountId, BigDecimal amount, String currency, String description) {
        IdValidation.ensureIdPresent(bankAccountId);
        CurrencyValidation.validateIfCurrencyIsCorrect(currency, "Invalid or not supported currency value");
        TransactionValidation.validateAmount(amount);
        TransactionValidation.validateDescriptionLength(description);
    }
}
