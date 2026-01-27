package main.java.com.alex.service;

import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.repository.IUserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService implements IUserAccountService{

    private final IUserAccountRepository userAccountRepository;

    public UserAccountService(IUserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    @Override
    public UserAccount save(UserAccount userAccount) {
        Long id = userAccountRepository.save(userAccount);
        return new UserAccount(id, userAccount.getLogin(), userAccount.getPassword(), userAccount.getRole(),
                userAccount.getCreateDate());
    }

    @Transactional
    @Override
    public void updatePassword(Long id, UserAccount userAccount) {
        userAccountRepository.updatePassword(id, userAccount);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserAccount> findById(Long id) {
        return userAccountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }
}
