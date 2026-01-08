package main.java.com.alex.service;

import jakarta.transaction.Transactional;
import main.java.com.alex.dto.Client;
import main.java.com.alex.dto.ClientDetails;
import main.java.com.alex.repository.IClientDetailsRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientDetailsService implements IClientDetailsService {

    private final IClientDetailsRepository clientDetailsRepository;
    private final IClientService clientService;

    public ClientDetailsService(IClientDetailsRepository clientDetailsRepository, IClientService clientService) {
        this.clientDetailsRepository = clientDetailsRepository;
        this.clientService = clientService;
    }

    @Transactional
    @Override
    public Client save(ClientDetails clientDetails) {
        ClientDetails createdClientDetails = clientDetailsRepository.save(clientDetails);
        return clientService.save(createdClientDetails);
    }
}
