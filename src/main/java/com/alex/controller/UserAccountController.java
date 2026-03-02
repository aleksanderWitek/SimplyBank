package com.alex.controller;

import com.alex.dto.Password;
import com.alex.dto.UserAccount;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.service.IUserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/user_account", produces = "application/json; charset=UTF-8")
public class UserAccountController {

    private final IUserAccountService userAccountService;

    public UserAccountController(IUserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserAccount> findUserAccountById(@PathVariable("id") Long id) {
        UserAccount userAccount = userAccountService.findById(id).orElseThrow(
                () -> new UserAccountNotFoundRuntimeException("There is no User with provided id:" + id));
        return ResponseEntity.ok(userAccount);
    }

    @GetMapping
    public ResponseEntity<List<UserAccount>> findAllUserAccounts() {
        List<UserAccount> userAccounts = userAccountService.findAll();
        return ResponseEntity.ok(userAccounts);
    }

    @PutMapping(path = "/{id}/password", consumes = "application/json")
    public ResponseEntity<Void> updatePassword(@PathVariable("id") Long id,
                                               @RequestBody Password password) {
        userAccountService.updatePassword(id, password);
        return ResponseEntity.noContent().build();
    }
}
