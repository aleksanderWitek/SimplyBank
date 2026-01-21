package main.java.com.alex.repository;

import main.java.com.alex.dto.Client;
import main.java.com.alex.exception.ClientNotFoundRuntimeException;
import main.java.com.alex.exception.DataAccessRuntimeException;
import main.java.com.alex.rowMapper.ClientRowMapper;
import org.springframework.dao.DataAccessException;
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
        String query = "INSERT INTO client(first_name, last_name, city, street, house_number, identification_number, " +
                "create_date)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(query, client.getFirstName(), client.getLastName(), client.getCity(),
                    client.getStreet(), client.getHouseNumber(), client.getIdentificationNumber(), client.getCreateDate());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database. " + e.getMessage());
        }
        return commonJdbcRepository.getLastInsertedId();
    }

    @Override
    public Optional<Client> findById(Long id) {
        String query = "SELECT * FROM client WHERE id = ?";
        try {
            List<Client> clients = jdbcTemplate.query(query, new ClientRowMapper(), id);
            return Optional.of(clients.getFirst());
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Client> findAll() {
        String query = "SELECT * FROM client";
        return jdbcTemplate.query(query, new ClientRowMapper());
    }

    @Override
    public void update(Long id, Client client) {

        String query = "UPDATE client " +
                "SET first_name = ?, " +
                "last_name = ?, " +
                "city = ?, " +
                "street = ?, " +
                "house_number = ?, " +
                "identification_number = ?, " +
                "create_date = ?, " +
                "modify_date = ? " +
                "WHERE id = ?";
        try {
            int rowAffected = jdbcTemplate.update(query,
                    client.getFirstName(),
                    client.getLastName(),
                    client.getCity(),
                    client.getStreet(),
                    client.getHouseNumber(),
                    client.getIdentificationNumber(),
                    client.getCreateDate(),
                    client.getModifyDate(),
                    id);
            if(rowAffected == 0) {
                throw new ClientNotFoundRuntimeException("There is no Client with provided id = " + id);
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        String query = "UPDATE client " +
                "SET delete_date = ? " +
                "WHERE id = ?";
        try {
            int rowAffected = jdbcTemplate.update(query,
                    LocalDateTime.now(),
                    id);
            if(rowAffected == 0) {
                throw new ClientNotFoundRuntimeException("There is no Client with provided id = " + id);
            }
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }
}
