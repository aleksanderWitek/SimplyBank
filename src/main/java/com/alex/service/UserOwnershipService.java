package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.ClientProfile;
import com.alex.dto.UserAccount;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IBankAccountClientRepository;
import com.alex.repository.IUserAccountRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserOwnershipService {

    private final IUserAccountRepository userAccountRepository;
    private final IClientService clientService;
    private final IBankAccountClientRepository bankAccountClientRepository;

    public UserOwnershipService(IUserAccountRepository userAccountRepository,
                                IClientService clientService,
                                IBankAccountClientRepository bankAccountClientRepository) {
        this.userAccountRepository = userAccountRepository;
        this.clientService = clientService;
        this.bankAccountClientRepository = bankAccountClientRepository;
    }

    public UserAccount resolveCurrentUser(Principal principal) {
        return userAccountRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException(
                        "User account not found for login: " + principal.getName()));
    }

    public boolean isClient(UserAccount userAccount) {
        return userAccount.getRole() == UserAccountRole.CLIENT;
    }

    public boolean isAdmin(UserAccount userAccount) {
        return userAccount.getRole() == UserAccountRole.ADMIN;
    }

    public Set<Long> getOwnedBankAccountIds(UserAccount userAccount) {
        if (!isClient(userAccount)) {
            return Collections.emptySet();
        }

        return clientService.findProfileByUserAccountId(userAccount.getId())
                .map(ClientProfile::getClientId)
                .map(clientId -> new HashSet<>(
                        bankAccountClientRepository.findBankAccountsIdLinkedToClientByClientId(clientId)))
                .orElse(Collections.emptySet());
    }

    public boolean ownsBankAccount(UserAccount userAccount, Long bankAccountId) {
        return getOwnedBankAccountIds(userAccount).contains(bankAccountId);
    }
}
