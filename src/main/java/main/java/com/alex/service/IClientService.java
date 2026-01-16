package main.java.com.alex.service;

import main.java.com.alex.dto.Client;

import java.util.List;
import java.util.Optional;

public interface IClientService {
    Client save();
    void update(Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void deleteById(Long id);
}
