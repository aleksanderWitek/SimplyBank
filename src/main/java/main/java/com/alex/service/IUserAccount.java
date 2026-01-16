package main.java.com.alex.service;

import main.java.com.alex.dto.UserAccount;

import java.util.List;
import java.util.Optional;

public interface IUserAccount {
    UserAccount save(UserAccount userAccount);
    void update(Long id, UserAccount userAccount);
    Optional<UserAccount> findById(Long id);
    List<UserAccount> findAll();
    void deleteById(Long id);
}
