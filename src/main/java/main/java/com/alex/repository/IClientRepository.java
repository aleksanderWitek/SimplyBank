package main.java.com.alex.repository;

import main.java.com.alex.dto.Client;

import java.util.List;
import java.util.Optional;

public interface IClientRepository {

    Client save(Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void deleteById(Client client);
}
