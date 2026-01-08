package main.java.com.alex.repository;

import jakarta.persistence.EntityManager;
import main.java.com.alex.dto.ClientDetails;
import org.springframework.stereotype.Component;

@Component
public class ClientDetailsRepository implements IClientDetailsRepository{

    private final EntityManager entityManager;

    public ClientDetailsRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public ClientDetails save(ClientDetails clientDetails) {
        if(clientDetails.getId() == null) {
            entityManager.persist(clientDetails);
            entityManager.flush();
            return clientDetails;
        } else {
            return entityManager.merge(clientDetails);
        }
    }
}
