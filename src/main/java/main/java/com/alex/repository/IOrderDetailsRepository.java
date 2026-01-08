package main.java.com.alex.repository;

import main.java.com.alex.dto.OrderDetails;

import java.util.List;
import java.util.Optional;

public interface IOrderDetailsRepository {
    OrderDetails save(OrderDetails orderDetails);
    Optional<OrderDetails> findById(Long id);
    List<OrderDetails> findAll();
    void deleteById(OrderDetails orderDetails);
}
