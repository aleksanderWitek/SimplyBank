package com.alex.controller;

import com.alex.dto.*;
import com.alex.exception.AccessDeniedRuntimeException;
import com.alex.exception.TransactionNotFoundRuntimeException;
import com.alex.service.ITransactionService;
import com.alex.service.UserOwnershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "api/transaction", produces = "application/json; charset=UTF-8")
public class TransactionController {

    private final ITransactionService transactionService;
    private final UserOwnershipService ownershipService;

    public TransactionController(ITransactionService transactionService,
                                 UserOwnershipService ownershipService) {
        this.transactionService = transactionService;
        this.ownershipService = ownershipService;
    }

    @PostMapping(path = "/transfer", consumes = "application/json")
    public ResponseEntity<Transaction> transfer(@RequestBody TransferRequest request, Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)
                && !ownershipService.ownsBankAccount(currentUser, request.getBankAccountFromId())) {
            throw new AccessDeniedRuntimeException("You do not have access to the source bank account");
        }

        Transaction transaction = transactionService.transfer(
                request.getBankAccountFromId(), request.getBankAccountToId(),
                request.getAmount(), request.getCurrency(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping(path = "/deposit", consumes = "application/json")
    public ResponseEntity<Transaction> deposit(@RequestBody DepositRequest request, Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)
                && !ownershipService.ownsBankAccount(currentUser, request.getBankAccountToId())) {
            throw new AccessDeniedRuntimeException("You do not have access to the target bank account");
        }

        Transaction transaction = transactionService.deposit(
                request.getBankAccountToId(), request.getAmount(),
                request.getCurrency(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping(path = "/withdraw", consumes = "application/json")
    public ResponseEntity<Transaction> withdraw(@RequestBody WithdrawRequest request, Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)
                && !ownershipService.ownsBankAccount(currentUser, request.getBankAccountFromId())) {
            throw new AccessDeniedRuntimeException("You do not have access to the source bank account");
        }

        Transaction transaction = transactionService.withdraw(
                request.getBankAccountFromId(), request.getAmount(),
                request.getCurrency(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Transaction> findTransactionById(@PathVariable("id") Long id, Principal principal) {
        Transaction transaction = transactionService.findById(id).orElseThrow(
                () -> new TransactionNotFoundRuntimeException("There is no transaction with provided id:" + id));

        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)) {
            Set<Long> ownedIds = ownershipService.getOwnedBankAccountIds(currentUser);
            boolean fromOwned = transaction.getBankAccountFrom() != null
                    && ownedIds.contains(transaction.getBankAccountFrom().getId());
            boolean toOwned = transaction.getBankAccountTo() != null
                    && ownedIds.contains(transaction.getBankAccountTo().getId());
            if (!fromOwned && !toOwned) {
                throw new AccessDeniedRuntimeException("You do not have access to this transaction");
            }
        }

        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAllTransactions() {
        // Security: restricted to EMPLOYEE/ADMIN via SecurityConfig
        List<Transaction> transactions = transactionService.findAll();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping(path = "/bank_account_from/{bank_account_from_id}")
    public ResponseEntity<List<Transaction>> findTransactionsByBankAccountFromId(
            @PathVariable("bank_account_from_id") Long bankAccountFromId, Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)
                && !ownershipService.ownsBankAccount(currentUser, bankAccountFromId)) {
            throw new AccessDeniedRuntimeException("You do not have access to this bank account");
        }

        List<Transaction> transactions = transactionService.findTransactionsByBankAccountFromId(bankAccountFromId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping(path = "/bank_account_to/{bank_account_to_id}")
    public ResponseEntity<List<Transaction>> findTransactionsByBankAccountToId(
            @PathVariable("bank_account_to_id") Long bankAccountToId, Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)
                && !ownershipService.ownsBankAccount(currentUser, bankAccountToId)) {
            throw new AccessDeniedRuntimeException("You do not have access to this bank account");
        }

        List<Transaction> transactions = transactionService.findTransactionsByBankAccountToId(bankAccountToId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping(path = "/between_bank_accounts")
    public ResponseEntity<List<Transaction>> findTransactionsBetweenBankAccounts(
            @RequestParam(value = "bank_account_from_id") Long bankAccountFromId,
            @RequestParam(value = "bank_account_to_id") Long bankAccountToId,
            Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)) {
            boolean fromOwned = ownershipService.ownsBankAccount(currentUser, bankAccountFromId);
            boolean toOwned = ownershipService.ownsBankAccount(currentUser, bankAccountToId);
            if (!fromOwned && !toOwned) {
                throw new AccessDeniedRuntimeException("You do not have access to these bank accounts");
            }
        }

        List<Transaction> transactions = transactionService.findTransactionsBetweenBankAccounts(
                bankAccountFromId, bankAccountToId);
        return ResponseEntity.ok(transactions);
    }
}
