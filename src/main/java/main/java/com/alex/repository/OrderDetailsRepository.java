package main.java.com.alex.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import main.java.com.alex.dto.OrderDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderDetailsRepository implements IOrderDetailsRepository{

    private final EntityManager entityManager;

    public OrderDetailsRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public OrderDetails save(OrderDetails orderDetails) {
        if(orderDetails.getId() == null) {
            entityManager.persist(orderDetails);
            entityManager.flush();
            return orderDetails;
        } else {
            return entityManager.merge(orderDetails);
        }
    }

    @Override
    public Optional<OrderDetails> findById(Long id) {
        OrderDetails orderDetails = entityManager.find(OrderDetails.class, id);
        return Optional.ofNullable(orderDetails);
    }

    @Override
    public List<OrderDetails> findAll() {
        TypedQuery<OrderDetails> query = entityManager.createQuery(
                "SELECT orderDetails FROM OrderDetails orderDetails", OrderDetails.class);
        return query.getResultList();
    }

    @Override
    public void deleteById(OrderDetails orderDetails) {
        entityManager.remove(orderDetails);
    }
}
