package main.java.com.alex.repository;

import main.java.com.alex.dto.Client;

import java.util.List;
import java.util.Optional;

public interface IClientRepository {

    Long save(Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void update(Long id, Client client);
    void deleteById(Long id);
}
