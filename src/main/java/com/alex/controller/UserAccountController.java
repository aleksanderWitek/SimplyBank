package com.alex.controller;

import com.alex.dto.Password;
import com.alex.dto.UserAccount;
import com.alex.exception.AccessDeniedRuntimeException;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.service.IUserAccountService;
import com.alex.service.UserOwnershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "api/user_account", produces = "application/json; charset=UTF-8")
public class UserAccountController {

    private final IUserAccountService userAccountService;
    private final UserOwnershipService ownershipService;

    public UserAccountController(IUserAccountService userAccountService,
                                 UserOwnershipService ownershipService) {
        this.userAccountService = userAccountService;
        this.ownershipService = ownershipService;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserAccount> findUserAccountById(@PathVariable("id") Long id, Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (!currentUser.getId().equals(id) && !ownershipService.isAdmin(currentUser)) {
            throw new AccessDeniedRuntimeException("You can only view your own account");
        }

        UserAccount userAccount = userAccountService.findById(id).orElseThrow(
                () -> new UserAccountNotFoundRuntimeException("There is no User with provided id:" + id));
        return ResponseEntity.ok(userAccount);
    }

    @GetMapping
    public ResponseEntity<List<UserAccount>> findAllUserAccounts() {
        // Security: restricted to ADMIN via SecurityConfig
        List<UserAccount> userAccounts = userAccountService.findAll();
        return ResponseEntity.ok(userAccounts);
    }

    @PutMapping(path = "/{id}/password", consumes = "application/json")
    public ResponseEntity<Void> updatePassword(@PathVariable("id") Long id,
                                               @RequestBody Password password,
                                               Principal principal) {
        UserAccount currentUser = ownershipService.resolveCurrentUser(principal);
        if (!currentUser.getId().equals(id) && !ownershipService.isAdmin(currentUser)) {
            throw new AccessDeniedRuntimeException("You can only change your own password");
        }

        userAccountService.updatePassword(id, password);
        return ResponseEntity.noContent().build();
    }
}
