package main.java.com.alex.controller;

import main.java.com.alex.dto.BankAccount;
import main.java.com.alex.dto.SaveBankAccountRequest;
import main.java.com.alex.exception.BankAccountNotFoundRuntimeException;
import main.java.com.alex.service.IBankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/bank_account", produces = "application/json; charset=UTF-8")
public class BankAccountController {

    private final IBankAccountService bankAccountService;

    public BankAccountController(IBankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<BankAccount> saveBankAccount(@RequestBody SaveBankAccountRequest request) {
        BankAccount bankAccount = bankAccountService.save(
                request.getClientId(), request.getBankAccountType(), request.getBankAccountCurrency());
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<BankAccount> findBankAccountById(@PathVariable("id") Long id) {
        BankAccount bankAccount = bankAccountService.findById(id).orElseThrow(
                () -> new BankAccountNotFoundRuntimeException("There is no bank account with provided id:" + id));
        return ResponseEntity.ok(bankAccount);
    }

    @GetMapping
    public ResponseEntity<List<BankAccount>> findAllBankAccounts() {
        List<BankAccount> bankAccounts = bankAccountService.findAll();
        return ResponseEntity.ok(bankAccounts);
    }
}
