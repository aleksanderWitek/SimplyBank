package main.java.com.alex.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import main.java.com.alex.dto.Client;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClientRepository implements IClientRepository{

    private final EntityManager entityManager;

    public ClientRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Client save(Client client) {
        if(client.getId() == null) {
            entityManager.persist(client);
            return client;
        } else {
            return entityManager.merge(client);
        }
    }

    @Override
    public Optional<Client> findById(Long id) {
        Client client = entityManager.find(Client.class, id);
        return Optional.ofNullable(client);
    }

    @Override
    public List<Client> findAll() {
        TypedQuery<Client> query = entityManager.createQuery(
                "SELECT client FROM Client client", Client.class);
        return query.getResultList();
    }

    @Override
    public void deleteById(Client client) {
        entityManager.remove(client);

        //todo is below more efficient and better or code above is more better
//        int deletedCount = entityManager.createQuery(
//                        "DELETE FROM Client c WHERE c.id = :id")
//                .setParameter("id", id)
//                .executeUpdate();
//
//        if (deletedCount == 0) {
//            throw new RuntimeException("There is no Client with id: " + id);
//        }
    }
}
