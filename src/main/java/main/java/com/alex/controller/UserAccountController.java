package main.java.com.alex.controller;

import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.UserAccountNotFoundRuntimeException;
import main.java.com.alex.service.IUserAccountService;
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
}
