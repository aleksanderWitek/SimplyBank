package com.alex.service;

import com.alex.dto.Client;
import com.alex.dto.ClientCreationResponse;
import com.alex.dto.ClientProfile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface IClientService {
    ClientCreationResponse save(Client client);
    void updateById(Long id, Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void deleteById(Long id);
    void deleteOwnAccount(Principal principal, String currentPassword);
    Optional<ClientProfile> findProfileByUserAccountId(Long userAccountId);
    Optional<ClientProfile> findProfileById(Long clientId);
}
