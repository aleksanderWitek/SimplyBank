package main.java.com.alex.service;

import main.java.com.alex.dto.UserAccount;

import java.util.List;
import java.util.Optional;

public interface IUserAccountService {
    UserAccount save(UserAccount userAccount);
    void updatePassword(Long id, UserAccount userAccount);
    Optional<UserAccount> findById(Long id);
    List<UserAccount> findAll();
}
