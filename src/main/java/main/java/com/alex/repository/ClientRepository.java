package main.java.com.alex.repository;

import main.java.com.alex.dto.Client;
import main.java.com.alex.exception.ClientNotFoundRuntimeException;
import main.java.com.alex.exception.DataAccessRuntimeException;
import main.java.com.alex.repository.mapper.ClientRowMapper;
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
        String query = """
                INSERT INTO\s
                client(first_name, last_name, city, street, house_number, identification_number, create_date)\s
                VALUES(?, ?, ?, ?, ?, ?, ?)
               """;
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
        String query = """
                 SELECT c.id,\s
                 c.first_name,\s
                 c.last_name,\s
                 c.city,\s
                 c.street,\s
                 c.house_number,\s
                 c.identification_number,\s
                 c.create_date,\s
                 c.modify_date,\s
                 ua.login,\s
                 ua.role\s
                 FROM client AS c\s
                 LEFT JOIN user_account_client AS uac ON c.id = uac.client_id\s
                 LEFT JOIN user_account AS ua ON uac.user_account_id = ua.id\s
                 WHERE c.id = ? AND c.delete_date IS NULL\s
               """;
        try {
            Client client = jdbcTemplate.queryForObject(query, new ClientRowMapper(), id);
            return Optional.ofNullable(client);
        } catch (DataAccessException e){
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public List<Client> findAll() {
        String query = """
                SELECT c.id,\s
                c.first_name,\s
                c.last_name,\s
                c.city,\s
                c.street,\s
                c.house_number,\s
                c.identification_number,\s
                c.create_date,\s
                c.modify_date,\s
                ua.login,\s
                ua.role\s
                FROM client AS c\s
                LEFT JOIN user_account_client AS uac ON c.id = uac.client_id\s
                LEFT JOIN user_account AS ua ON uac.user_account_id = ua.id\s
                WHERE c.delete_date IS NULL\s
                ORDER BY c.id
                """;
        try {
            return jdbcTemplate.query(query, new ClientRowMapper());
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void update(Long id, Client client) {

        String query = """
                UPDATE client\s
                SET first_name = ?,\s
                last_name = ?,\s
                city = ?,\s
                street = ?,\s
                house_number = ?,\s
                identification_number = ?,\s
                modify_date = ?\s
                WHERE id = ? AND delete_date IS NULL
               """;
        try {
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
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        String query = """
                UPDATE client\s
                SET delete_date\s
                WHERE id = ? AND delete_date IS NULL
               """;
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
