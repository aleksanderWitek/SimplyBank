package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.Password;
import com.alex.dto.UserAccount;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface IUserAccountService extends UserDetailsService {
    UserAccount save(String firstName, String lastName, UserAccountRole role);
    void updatePassword(Long userAccountId, Password password);
    Optional<UserAccount> findById(Long id);
    List<UserAccount> findAll();
    void deleteById(Long id);
}
