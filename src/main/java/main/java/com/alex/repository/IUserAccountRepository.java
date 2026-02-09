package main.java.com.alex.repository;

import main.java.com.alex.dto.UserAccount;

import java.util.List;
import java.util.Optional;

public interface IUserAccountRepository {
    Long save(UserAccount userAccount);
    Optional<UserAccount> findById(Long id);
    Optional<UserAccount> findByLogin(String login);
    List<UserAccount> findAll();
    void updatePassword(Long id, String encodedNewPassword);
    void deleteById(Long id);
}
