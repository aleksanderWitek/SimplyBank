package main.java.com.alex.repository;

import main.java.com.alex.dto.Password;
import main.java.com.alex.dto.UserAccount;

import java.util.List;
import java.util.Optional;

public interface IUserAccountRepository {
    Long save(UserAccount userAccount);
    Optional<UserAccount> findById(Long id);
    List<UserAccount> findAll();
    void updatePassword(Long id, Password password);
    void deleteById(Long id);
}
