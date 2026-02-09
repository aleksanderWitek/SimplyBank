package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.Password;
import com.alex.dto.UserAccount;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IUserAccountRepository;
import com.alex.service.validation.IdValidation;
import com.alex.service.validation.PasswordValidation;
import com.alex.service.validation.UserAccountValidation;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService implements IUserAccountService{

    private final IUserAccountRepository userAccountRepository;
    private final IUserAccountProcessingService userAccountProcessingService;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(IUserAccountRepository userAccountRepository,
                              IUserAccountProcessingService userAccountProcessingService, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountProcessingService = userAccountProcessingService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public UserAccount save(String firstName, String lastName, UserAccountRole role) {
        UserAccountValidation.ensureFirstNamePresent(firstName);
        UserAccountValidation.ensureLastNamePresent(lastName);
        UserAccountValidation.ensureUserAccountRoleIsCorrect(role);

        String login = userAccountProcessingService.generateLogin(firstName, lastName);
        String password = userAccountProcessingService.generatePassword();
        String encodedPassword = passwordEncoder.encode(password);

        UserAccount userAccount = new UserAccount(login, encodedPassword, role, LocalDateTime.now());
        Long id = userAccountRepository.save(userAccount);
        return new UserAccount(id, userAccount.getLogin(), password, userAccount.getRole(),
                userAccount.getCreateDate());
    }

    @Transactional
    @Override
    public void updatePassword(Long userAccountId, Password password) {
        PasswordValidation.ensurePasswordMeetsRequirements(password.getNewPassword(), 12);

        UserAccount userAccount = findById(userAccountId)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException(
                        "There is no User Account with provided id:" + userAccountId));
        PasswordValidation.authenticatePassword(
                password.getCurrentPassword(),
                userAccount.getPassword(),
                passwordEncoder);
        PasswordValidation.ensureProvidedPasswordIsDifferentFromExistingPassword(
                password.getNewPassword(),
                userAccount.getPassword(),
                passwordEncoder);

        String encodedNewPassword = passwordEncoder.encode(password.getNewPassword());
        userAccountRepository.updatePassword(userAccountId, encodedNewPassword);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserAccount> findById(Long id) {
        IdValidation.ensureIdPresent(id);
        return userAccountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        IdValidation.ensureIdPresent(id);
        userAccountRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String login) {
        UserAccount userAccount = userAccountRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with login: " + login));

        return User.builder()
                .username(userAccount.getLogin())
                .password(userAccount.getPassword())
                .roles(userAccount.getRole().name())
                .build();
    }
}
