package main.java.com.alex.service;

import main.java.com.alex.dto.Account;
import main.java.com.alex.dto.Client;
import main.java.com.alex.repository.IAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IClientService clientService;

    public AccountService(IAccountRepository accountRepository, IClientService clientService) {
        this.accountRepository = accountRepository;
        this.clientService = clientService;
    }

    @Transactional
    @Override
    public Account save(Account account, Long clientId) {
        Client client = clientService.findById(clientId)
                .orElseThrow(() -> new RuntimeException("There is no Client with id: " + clientId));
        Account savedAccount = accountRepository.save(account);
        client.addAccount(savedAccount);
        clientService.update(client);

        return savedAccount;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        Account account = findById(id).orElseThrow(() -> new RuntimeException("There is no Account with id:" + id));
        List<Client> clients = new ArrayList<>(account.getClients());
        clients.forEach(client -> {
                    if(client.getAccounts().contains(account)) {
                        client.removeAccount(account);
                        clientService.update(client);
                    }
                });
        accountRepository.removeById(account);
    }
}
