package main.java.com.alex.repository;

import main.java.com.alex.dto.Account;

import java.util.List;
import java.util.Optional;

public interface IAccountRepository {
    Account save(Account account);
    Optional<Account> findById(Long id);
    List<Account> findAll();
    void removeById(Account account);
}
