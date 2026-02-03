package main.java.com.alex.service;

import main.java.com.alex.UserAccountRole;
import main.java.com.alex.dto.Client;
import main.java.com.alex.dto.Password;
import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.UserAccountNotFoundRuntimeException;
import main.java.com.alex.repository.IClientRepository;
import main.java.com.alex.repository.IUserAccountClientRepository;
import main.java.com.alex.service.validation.ClientValidation;
import main.java.com.alex.service.validation.IdValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService implements IClientService {

    private final IClientRepository clientRepository;
    private final IUserAccountClientRepository userAccountClientRepository;
    private final IUserAccountService userAccountService;

    public ClientService(IClientRepository clientRepository, IUserAccountClientRepository userAccountClientRepository, IUserAccountService userAccountService) {
        this.clientRepository = clientRepository;
        this.userAccountClientRepository = userAccountClientRepository;
        this.userAccountService = userAccountService;
    }

    @Transactional
    @Override
    public Client save(Client client) {
        ClientValidation.ensureClientPresent(client);

        Client clientWithCreateDate = new Client(client.getFirstName(), client.getLastName(), client.getCity(),
                client.getStreet(), client.getHouseNumber(), client.getIdentificationNumber(), LocalDateTime.now());
        Long id = clientRepository.save(clientWithCreateDate);
        Client savedClient = new Client(id, clientWithCreateDate.getFirstName(), clientWithCreateDate.getLastName(),
                clientWithCreateDate.getCity(), clientWithCreateDate.getStreet(), clientWithCreateDate.getHouseNumber(),
                clientWithCreateDate.getIdentificationNumber(), clientWithCreateDate.getCreateDate());
        UserAccount userAccount = userAccountService.save(client.getFirstName(), client.getLastName(),
                UserAccountRole.CLIENT);
        savedClient.addUserAccount(userAccount);
        userAccountClientRepository.linkUserAccountToClient(userAccount.getId(), id);
        return savedClient;
    }

    @Transactional
    @Override
    public void updateById(Long id, Client client) {
        IdValidation.ensureIdPresent(id);
        ClientValidation.ensureClientPresent(client);

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
        userAccountClientRepository.unlinkUserAccountFromClient(userAccountId, id);
        userAccountService.deleteById(userAccountId);
        clientRepository.deleteById(id);
    }

    @Override
    public void updatePassword(Long clientId, Password password) {
        IdValidation.ensureIdPresent(clientId);
        Long userAccountId = userAccountClientRepository.findUserAccountIdByClientId(clientId)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException("There is no Client with provided id:" + clientId));

        userAccountService.updatePassword(userAccountId, password);
    }
}
