package com.alex.controller;

import com.alex.UserAccountRole;
import com.alex.dto.ClientProfile;
import com.alex.dto.EmployeeProfile;
import com.alex.dto.UserAccount;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IUserAccountRepository;
import com.alex.service.IClientService;
import com.alex.service.IEmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/auth", produces = "application/json; charset=UTF-8")
public class AuthController {

    private final IUserAccountRepository userAccountRepository;
    private final IClientService clientService;
    private final IEmployeeService employeeService;

    public AuthController(IUserAccountRepository userAccountRepository,
                          IClientService clientService,
                          IEmployeeService employeeService) {
        this.userAccountRepository = userAccountRepository;
        this.clientService = clientService;
        this.employeeService = employeeService;
    }

    @GetMapping(path = "/me")
    public ResponseEntity<Map<String, Object>> me(Principal principal) {
        UserAccount userAccount = userAccountRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException(
                        "User account not found for login: " + principal.getName()));

        Map<String, Object> response = new HashMap<>();
        response.put("id", userAccount.getId());
        response.put("login", userAccount.getLogin());
        response.put("role", userAccount.getRole().name());

        if (userAccount.getRole() == UserAccountRole.CLIENT) {
            Optional<ClientProfile> profile = clientService.findProfileByUserAccountId(userAccount.getId());
            profile.ifPresent(p -> {
                response.put("firstName", p.getFirstName());
                response.put("lastName", p.getLastName());
            });
        } else {
            Optional<EmployeeProfile> profile = employeeService.findProfileByUserAccountId(userAccount.getId());
            profile.ifPresent(p -> {
                response.put("firstName", p.getFirstName());
                response.put("lastName", p.getLastName());
            });
        }

        return ResponseEntity.ok(response);
    }
}
