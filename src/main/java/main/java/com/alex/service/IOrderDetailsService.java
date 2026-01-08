package main.java.com.alex.service;

import main.java.com.alex.dto.OrderDetails;

import java.util.List;
import java.util.Optional;

public interface IOrderDetailsService {

    OrderDetails save(String item, Long clientId);
    Optional<OrderDetails> findById(Long id);
    List<OrderDetails> findAll();
    void deleteById(Long id);
}
