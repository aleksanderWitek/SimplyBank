package com.alex.service;

import com.alex.dto.Client;
import com.alex.dto.Password;

import java.util.List;
import java.util.Optional;

public interface IClientService {
    Client save(Client client);
    void updateById(Long id, Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void deleteById(Long id);
    void updatePassword(Long clientId, Password password);
}
