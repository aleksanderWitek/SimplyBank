package main.java.com.alex.service;

import main.java.com.alex.dto.Account;

import java.util.List;
import java.util.Optional;

public interface IAccountService {

    Account save(Account account, Long clientId);
    Optional<Account> findById(Long id);
    List<Account> findAll();
    void deleteById(Long id);
}
