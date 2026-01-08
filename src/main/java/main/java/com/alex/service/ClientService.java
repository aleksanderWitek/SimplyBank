package main.java.com.alex.service;

import main.java.com.alex.dto.Client;
import main.java.com.alex.dto.ClientDetails;
import main.java.com.alex.repository.IClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService implements IClientService {

    private final IClientRepository clientRepository;

    public ClientService(IClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    @Override
    public Client save(ClientDetails clientDetails) {
        Client client = new Client(clientDetails.getFirstName() + " " + clientDetails.getLastName(), clientDetails);
        return clientRepository.save(client);
    }

    @Transactional
    @Override
    public void update(Client client) {
        //todo create new method i repository for update to make it more readable and single responsibility think?
        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Client> findById(Long id) {
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
        Client client = findById(id).orElseThrow(() -> new RuntimeException("There is no Client with id:" + id));
        clientRepository.deleteById(client);
    }
}
