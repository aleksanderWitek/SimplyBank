package com.alex.controller;

import com.alex.dto.BankAccount;
import com.alex.dto.SaveBankAccountRequest;
import com.alex.dto.UserAccount;
import com.alex.exception.AccessDeniedRuntimeException;
import com.alex.exception.BankAccountNotFoundRuntimeException;
import com.alex.service.IBankAccountService;
import com.alex.service.UserOwnershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "api/bank_account", produces = "application/json; charset=UTF-8")
public class BankAccountController {

    private final IBankAccountService bankAccountService;
    private final UserOwnershipService ownershipService;

    public BankAccountController(IBankAccountService bankAccountService,
                                 UserOwnershipService ownershipService) {
        this.bankAccountService = bankAccountService;
        this.ownershipService = ownershipService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<BankAccount> saveBankAccount(@RequestBody SaveBankAccountRequest request) {
        BankAccount bankAccount = bankAccountService.save(
                request.getClientId(), request.getBankAccountType(), request.getBankAccountCurrency());
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<BankAccount> findBankAccountById(@PathVariable("id") Long id, Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser) && !ownershipService.ownsBankAccount(currentUser, id)) {
            throw new AccessDeniedRuntimeException("You do not have access to this bank account");
        }

        BankAccount bankAccount = bankAccountService.findById(id).orElseThrow(
                () -> new BankAccountNotFoundRuntimeException("There is no bank account with provided id:" + id));
        return ResponseEntity.ok(bankAccount);
    }

    @GetMapping
    public ResponseEntity<List<BankAccount>> findAllBankAccounts(Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);

        if (ownershipService.isClient(currentUser)) {
            Set<Long> ownedIds = ownershipService.getOwnedBankAccountIds(currentUser);
            List<BankAccount> allAccounts = bankAccountService.findAll();
            List<BankAccount> filtered = allAccounts.stream()
                    .filter(ba -> ownedIds.contains(ba.getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        }

        List<BankAccount> bankAccounts = bankAccountService.findAll();
        return ResponseEntity.ok(bankAccounts);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteBankAccountById(@PathVariable("id") Long id) {
        bankAccountService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
