package main.java.com.alex.service;

import main.java.com.alex.UserAccountRole;
import main.java.com.alex.dto.Password;
import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.UserAccountNotFoundRuntimeException;
import main.java.com.alex.repository.IUserAccountRepository;
import main.java.com.alex.service.validation.IdValidation;
import main.java.com.alex.service.validation.PasswordValidation;
import main.java.com.alex.service.validation.UserAccountValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService implements IUserAccountService{

    private final IUserAccountRepository userAccountRepository;
    private final IUserAccountProcessingService userAccountProcessingService;

    public UserAccountService(IUserAccountRepository userAccountRepository,
                              IUserAccountProcessingService userAccountProcessingService) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountProcessingService = userAccountProcessingService;
    }

    @Transactional
    @Override
    public UserAccount save(String firstName, String lastName, UserAccountRole role) {
        UserAccountValidation.ensureFirstNamePresent(firstName);
        UserAccountValidation.ensureLastNamePresent(lastName);
        UserAccountValidation.ensureUserAccountRoleIsCorrect(role.toString());

        String login = userAccountProcessingService.generateLogin(firstName, lastName);
        String password = userAccountProcessingService.generatePassword();

        UserAccount userAccount = new UserAccount(login, password, role, LocalDateTime.now());
        Long id = userAccountRepository.save(userAccount);
        return new UserAccount(id, userAccount.getLogin(), userAccount.getPassword(), userAccount.getRole(),
                userAccount.getCreateDate());
    }

    @Transactional
    @Override
    public void updatePassword(Long userAccountId, Password password) {
        PasswordValidation.ensurePasswordMeetsRequirements(password.getNewPassword());

        UserAccount userAccount = findById(userAccountId)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException("There is no User Account with provided id:" + userAccountId));
        PasswordValidation.ensureProvidedPasswordIsDifferentFromExistingPassword(password.getNewPassword(),
                userAccount.getPassword());

        userAccountRepository.updatePassword(userAccountId, password);
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
}
