package main.java.com.alex.repository;

import java.util.Optional;

public interface IUserAccountClientRepository {
    void linkUserAccountToClient(Long userAccountId, Long clientId);
    void unlinkUserAccountFromClient(Long userAccountId, Long clientId);
    Optional<Long> findUserAccountIdByClientId(Long clientId);
}
