package main.java.com.alex.controller;

import main.java.com.alex.dto.DepositRequest;
import main.java.com.alex.dto.Transaction;
import main.java.com.alex.dto.TransferRequest;
import main.java.com.alex.dto.WithdrawRequest;
import main.java.com.alex.exception.TransactionNotFoundRuntimeException;
import main.java.com.alex.service.ITransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/transaction", produces = "application/json; charset=UTF-8")
public class TransactionController {

    private final ITransactionService transactionService;

    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(path = "/transfer", consumes = "application/json")
    public ResponseEntity<Transaction> transfer(@RequestBody TransferRequest request) {
        Transaction transaction = transactionService.transfer(
                request.getBankAccountFromId(), request.getBankAccountToId(),
                request.getAmount(), request.getCurrency(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping(path = "/deposit", consumes = "application/json")
    public ResponseEntity<Transaction> deposit(@RequestBody DepositRequest request) {
        Transaction transaction = transactionService.deposit(
                request.getBankAccountToId(), request.getAmount(),
                request.getCurrency(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping(path = "/withdraw", consumes = "application/json")
    public ResponseEntity<Transaction> withdraw(@RequestBody WithdrawRequest request) {
        Transaction transaction = transactionService.withdraw(
                request.getBankAccountFromId(), request.getAmount(),
                request.getCurrency(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Transaction> findTransactionById(@PathVariable("id") Long id) {
        Transaction transaction = transactionService.findById(id).orElseThrow(
                () -> new TransactionNotFoundRuntimeException("There is no transaction with provided id:" + id));
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAllTransactions() {
        List<Transaction> transactions = transactionService.findAll();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping(path = "/bank_account_from/{bank_account_from_id}")
    public ResponseEntity<List<Transaction>> findTransactionsByBankAccountFromId(
            @PathVariable("bank_account_from_id") Long bankAccountFromId) {
        List<Transaction> transactions = transactionService.findTransactionsByBankAccountFromId(bankAccountFromId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping(path = "/bank_account_to/{bank_account_to_id}")
    public ResponseEntity<List<Transaction>> findTransactionsByBankAccountToId(
            @PathVariable("bank_account_to_id") Long bankAccountToId) {
        List<Transaction> transactions = transactionService.findTransactionsByBankAccountToId(bankAccountToId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping(path = "/between_bank_accounts")
    public ResponseEntity<List<Transaction>> findTransactionsBetweenBankAccounts(
            @RequestParam(value = "bank_account_from_id") Long bankAccountFromId,
            @RequestParam(value = "bank_account_to_id") Long bankAccountToId) {
        List<Transaction> transactions = transactionService.findTransactionsBetweenBankAccounts(
                bankAccountFromId, bankAccountToId);
        return ResponseEntity.ok(transactions);
    }
}
