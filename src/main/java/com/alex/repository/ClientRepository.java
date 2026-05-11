package com.alex.repository;

import com.alex.dto.Client;
import com.alex.dto.ClientProfile;
import com.alex.exception.ClientNotFoundRuntimeException;
import com.alex.repository.mapper.ClientProfileRowMapper;
import com.alex.repository.mapper.ClientRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ClientRepository implements IClientRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ICommonJdbcRepository commonJdbcRepository;

    public ClientRepository(JdbcTemplate jdbcTemplate, ICommonJdbcRepository commonJdbcRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonJdbcRepository = commonJdbcRepository;
    }

    @Override
    public Long save(Client client) {
        String query = """
                INSERT INTO
                client(first_name, last_name, city, street, house_number, identification_number, create_date)
                VALUES(?, ?, ?, ?, ?, ?, ?)
               """;
        jdbcTemplate.update(query, client.getFirstName(), client.getLastName(), client.getCity(),
                client.getStreet(), client.getHouseNumber(), client.getIdentificationNumber(), client.getCreateDate());
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<Client> findById(Long id) {
        String query = """
                 SELECT id,
                 first_name,
                 last_name,
                 city,
                 street,
                 house_number,
                 identification_number,
                 create_date,
                 modify_date,
                 delete_date
                 FROM client
                 WHERE id = ? AND delete_date IS NULL
               """;
        List<Client> results = jdbcTemplate.query(query, new ClientRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public List<Client> findAll() {
        String query = """
                SELECT id,
                first_name,
                last_name,
                city,
                street,
                house_number,
                identification_number,
                create_date,
                modify_date,
                delete_date
                FROM client
                WHERE delete_date IS NULL
                ORDER BY id
                """;
        return jdbcTemplate.query(query, new ClientRowMapper());
    }

    @Override
    public void updateById(Long id, Client client) {

        String query = """
                UPDATE client
                SET first_name = ?,
                last_name = ?,
                city = ?,
                street = ?,
                house_number = ?,
                identification_number = ?,
                modify_date = ?
                WHERE id = ? AND delete_date IS NULL
               """;
        int rowAffected = jdbcTemplate.update(query,
                client.getFirstName(),
                client.getLastName(),
                client.getCity(),
                client.getStreet(),
                client.getHouseNumber(),
                client.getIdentificationNumber(),
                client.getModifyDate(),
                id);
        if(rowAffected == 0) {
            throw new ClientNotFoundRuntimeException("There is no Client with provided id = " + id);
        }
    }

    @Override
    public Optional<ClientProfile> findProfileByUserAccountId(Long userAccountId) {
        String query = """
                SELECT c.id            AS client_id,
                       c.first_name,
                       c.last_name,
                       c.city,
                       c.street,
                       c.house_number,
                       c.identification_number,
                       c.create_date  AS client_create_date,
                       c.modify_date  AS client_modify_date,
                       ua.id          AS user_account_id,
                       ua.login,
                       ua.role,
                       ua.create_date AS account_create_date
                FROM client c
                JOIN user_account_client uac ON uac.client_id = c.id
                JOIN user_account ua ON ua.id = uac.user_account_id
                WHERE ua.id = ?
                  AND c.delete_date IS NULL
                  AND ua.delete_date IS NULL
                """;
        List<ClientProfile> results = jdbcTemplate.query(query, new ClientProfileRowMapper(), userAccountId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public Optional<ClientProfile> findProfileById(Long clientId) {
        String query = """
                SELECT c.id            AS client_id,
                       c.first_name,
                       c.last_name,
                       c.city,
                       c.street,
                       c.house_number,
                       c.identification_number,
                       c.create_date  AS client_create_date,
                       c.modify_date  AS client_modify_date,
                       ua.id          AS user_account_id,
                       ua.login,
                       ua.role,
                       ua.create_date AS account_create_date
                FROM client c
                JOIN user_account_client uac ON uac.client_id = c.id
                JOIN user_account ua ON ua.id = uac.user_account_id
                WHERE c.id = ?
                  AND c.delete_date IS NULL
                  AND ua.delete_date IS NULL
                """;
        List<ClientProfile> results = jdbcTemplate.query(query, new ClientProfileRowMapper(), clientId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public void deleteById(Long id) {
        // ClientService.deleteById verifies existence via userAccountClientRepository before this call.
        String query = """
                UPDATE client
                SET delete_date = ?
                WHERE id = ? AND delete_date IS NULL
               """;
        jdbcTemplate.update(query, LocalDateTime.now(), id);
    }
}
