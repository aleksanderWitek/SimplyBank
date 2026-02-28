package com.alex.repository;

import com.alex.dto.Client;
import com.alex.dto.ClientProfile;

import java.util.List;
import java.util.Optional;

public interface IClientRepository {

    Long save(Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void updateById(Long id, Client client);
    void deleteById(Long id);
    Optional<ClientProfile> findProfileByUserAccountId(Long userAccountId);
}
