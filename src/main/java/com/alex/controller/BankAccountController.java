package com.alex.controller;

import com.alex.dto.BankAccount;
import com.alex.dto.ClientProfile;
import com.alex.dto.SaveBankAccountRequest;
import com.alex.dto.UserAccount;
import com.alex.exception.AccessDeniedRuntimeException;
import com.alex.exception.BankAccountNotFoundRuntimeException;
import com.alex.service.IBankAccountService;
import com.alex.service.IClientService;
import com.alex.service.UserOwnershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "api/bank_account", produces = "application/json; charset=UTF-8")
public class BankAccountController {

    private final IBankAccountService bankAccountService;
    private final UserOwnershipService ownershipService;
    private final IClientService clientService;

    public BankAccountController(IBankAccountService bankAccountService,
                                 UserOwnershipService ownershipService,
                                 IClientService clientService) {
        this.bankAccountService = bankAccountService;
        this.ownershipService = ownershipService;
        this.clientService = clientService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<BankAccount> saveBankAccount(@RequestBody SaveBankAccountRequest request,
                                                       Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);

        if (!ownershipService.isClient(currentUser)) {
            throw new AccessDeniedRuntimeException(
                    "Only clients can open bank accounts");
        }

        Long clientId = clientService.findProfileByUserAccountId(currentUser.getId())
                .map(ClientProfile::getClientId)
                .orElseThrow(() -> new AccessDeniedRuntimeException(
                        "Current user is not linked to a client profile"));

        BankAccount bankAccount = bankAccountService.save(
                clientId, request.getBankAccountType(), request.getBankAccountCurrency());
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

    @GetMapping(path = "/by-number/{number}")
    public ResponseEntity<BankAccount> findBankAccountByNumber(@PathVariable("number") String number,
                                                               Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)) {
            throw new AccessDeniedRuntimeException("Only staff can search bank accounts by number");
        }

        BankAccount bankAccount = bankAccountService.findByNumber(number).orElseThrow(
                () -> new BankAccountNotFoundRuntimeException(
                        "There is no bank account with provided number:" + number));
        return ResponseEntity.ok(bankAccount);
    }

    @GetMapping(path = "/by-number/{number}/currency")
    public ResponseEntity<Map<String, Object>> findBankAccountCurrencyByNumber(@PathVariable("number") String number,
                                                                               Principal principal) {
        ownershipService.resolveCurrentUser(principal);

        BankAccount bankAccount = bankAccountService.findByNumber(number).orElseThrow(
                () -> new BankAccountNotFoundRuntimeException(
                        "There is no bank account with provided number:" + number));
        return ResponseEntity.ok(Map.of("currency", bankAccount.getCurrency()));
    }

    @GetMapping(path = "/by-client/{clientId}")
    public ResponseEntity<List<BankAccount>> findBankAccountsByClientId(@PathVariable("clientId") Long clientId,
                                                                       Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)) {
            throw new AccessDeniedRuntimeException("Only staff can list bank accounts by client id");
        }

        List<BankAccount> bankAccounts = bankAccountService.findByClientId(clientId);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping(path = "/{id}/owners")
    public ResponseEntity<List<ClientProfile>> findOwnersOfBankAccount(@PathVariable("id") Long id,
                                                                      Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (ownershipService.isClient(currentUser)) {
            throw new AccessDeniedRuntimeException("Only staff can view bank account owners");
        }

        List<ClientProfile> owners = bankAccountService.findOwnersByBankAccountId(id);
        return ResponseEntity.ok(owners);
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
