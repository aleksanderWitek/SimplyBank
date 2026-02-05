package main.java.com.alex.repository;

import java.util.List;

public interface IBankAccountClientRepository {

    void linkBankAccountToClient(Long bankAccountId, Long clientId);
    void unlinkBankAccountToClient(Long bankAccountId, Long clientId);
    List<Long> findBankAccountsIdLinkedToClientByClientId(Long clientId);
    List<Long> findClientsIdLinkedToBankAccountByBankAccountId(Long bankAccountId);
}
