package main.java.com.alex.service;

import main.java.com.alex.dto.Client;
import main.java.com.alex.exception.NullPointerRuntimeException;
import main.java.com.alex.repository.IClientRepository;
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

    public ClientService(IClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    @Override
    public Client save(Client client) {
        ClientValidation.ensureClientPresent(client);

        Client clientWithCreateDate = new Client(client.getFirstName(), client.getLastName(), client.getCity(), client.getStreet(),
                client.getHouseNumber(), client.getIdentificationNumber(), LocalDateTime.now());
        Long id = clientRepository.save(clientWithCreateDate);
        return new Client(id, clientWithCreateDate.getFirstName(), clientWithCreateDate.getLastName(),
                clientWithCreateDate.getCity(), clientWithCreateDate.getStreet(), clientWithCreateDate.getHouseNumber(),
                clientWithCreateDate.getIdentificationNumber(), clientWithCreateDate.getCreateDate());
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
        clientRepository.deleteById(id);
    }
}
