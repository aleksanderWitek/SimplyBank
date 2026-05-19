package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.Client;
import com.alex.dto.ClientCreationResponse;
import com.alex.dto.ClientProfile;
import com.alex.dto.UserAccount;
import com.alex.exception.ClientNotFoundRuntimeException;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IBankAccountClientRepository;
import com.alex.repository.IClientRepository;
import com.alex.repository.IUserAccountClientRepository;
import com.alex.repository.IUserAccountRepository;
import com.alex.service.validation.ClientValidation;
import com.alex.service.validation.IdValidation;
import com.alex.service.validation.PasswordValidation;
import com.alex.service.validation.UserAccountValidation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService implements IClientService {

    private final IClientRepository clientRepository;
    private final IUserAccountClientRepository userAccountClientRepository;
    private final IBankAccountClientRepository bankAccountClientRepository;
    private final IUserAccountService userAccountService;
    private final IUserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(IClientRepository clientRepository,
                         IUserAccountClientRepository userAccountClientRepository,
                         IBankAccountClientRepository bankAccountClientRepository,
                         IUserAccountService userAccountService,
                         IUserAccountRepository userAccountRepository,
                         PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.userAccountClientRepository = userAccountClientRepository;
        this.bankAccountClientRepository = bankAccountClientRepository;
        this.userAccountService = userAccountService;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public ClientCreationResponse save(Client client) {
        ClientValidation.ensureClientPresent(client);
        UserAccountValidation.ensureFirstNamePresent(client.getFirstName());
        UserAccountValidation.ensureLastNamePresent(client.getLastName());

        Client clientWithCreateDate = new Client(client.getFirstName(), client.getLastName(), client.getCity(),
                client.getStreet(), client.getHouseNumber(), client.getIdentificationNumber(), LocalDateTime.now());
        Long id = clientRepository.save(clientWithCreateDate);
        Client savedClient = new Client(id, clientWithCreateDate.getFirstName(), clientWithCreateDate.getLastName(),
                clientWithCreateDate.getCity(), clientWithCreateDate.getStreet(), clientWithCreateDate.getHouseNumber(),
                clientWithCreateDate.getIdentificationNumber(), clientWithCreateDate.getCreateDate());
        UserAccount userAccount = userAccountService.save(client.getFirstName(), client.getLastName(),
                UserAccountRole.CLIENT);
        userAccountClientRepository.linkUserAccountToClient(userAccount.getId(), id);
        return new ClientCreationResponse(savedClient, userAccount.getLogin(), userAccount.getPassword());
    }

    @Transactional
    @Override
    public void updateById(Long id, Client client) {
        IdValidation.ensureIdPresent(id);
        ClientValidation.ensureClientPresent(client);
        UserAccountValidation.ensureFirstNamePresent(client.getFirstName());
        UserAccountValidation.ensureLastNamePresent(client.getLastName());
        clientRepository.updateById(id, client);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Client> findById(Long id) {
        IdValidation.ensureIdPresent(id);
        return clientRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        IdValidation.ensureIdPresent(id);
        Long userAccountId = userAccountClientRepository.findUserAccountIdByClientId(id)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException("There is no User Account linked to Client with id:" + id));
        List<Long> bankAccountsId = bankAccountClientRepository.findBankAccountsIdLinkedToClientByClientId(id);
        bankAccountsId.forEach(bankAccountId -> bankAccountClientRepository.unlinkBankAccountToClient(bankAccountId, id));
        userAccountClientRepository.unlinkUserAccountFromClient(userAccountId, id);
        userAccountService.deleteById(userAccountId);
        clientRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteOwnAccount(Principal principal, String currentPassword) {
        UserAccount userAccount = userAccountRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException(
                        "There is no User Account for login:" + principal.getName()));
        PasswordValidation.authenticatePassword(currentPassword, userAccount.getPassword(), passwordEncoder);

        Long clientId = userAccountClientRepository.findClientIdByUserAccountId(userAccount.getId())
                .orElseThrow(() -> new ClientNotFoundRuntimeException(
                        "There is no Client linked to User Account with id:" + userAccount.getId()));
        deleteById(clientId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ClientProfile> findProfileByUserAccountId(Long userAccountId) {
        IdValidation.ensureIdPresent(userAccountId);
        return clientRepository.findProfileByUserAccountId(userAccountId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ClientProfile> findProfileById(Long clientId) {
        IdValidation.ensureIdPresent(clientId);
        return clientRepository.findProfileById(clientId);
    }

}
