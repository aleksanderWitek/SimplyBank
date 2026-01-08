package main.java.com.alex.service;

import main.java.com.alex.dto.Client;
import main.java.com.alex.dto.OrderDetails;
import main.java.com.alex.repository.IOrderDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderDetailsService implements IOrderDetailsService{

    private final IOrderDetailsRepository orderDetailsRepository;
    private final IClientService clientService;

    public OrderDetailsService(IOrderDetailsRepository orderDetailsRepository, IClientService clientService) {
        this.orderDetailsRepository = orderDetailsRepository;
        this.clientService = clientService;
    }

    @Transactional
    @Override
    public OrderDetails save(String item, Long clientId) {
        Client client = clientService.findById(clientId)
                .orElseThrow(() -> new RuntimeException("There is no Client with that id:" + clientId));
        OrderDetails orderDetails = orderDetailsRepository.save(new OrderDetails(item, client));
        client.addOrder(orderDetails);
        clientService.update(client);
        return orderDetails;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrderDetails> findById(Long id) {
        return orderDetailsRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderDetails> findAll() {
        return orderDetailsRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        OrderDetails orderDetails = findById(id).orElseThrow(() -> new RuntimeException("There is no OrderDetails with that id:" + id));
        Client client = orderDetails.getClient();
        client.removeOrder(orderDetails);
        clientService.update(client);
        orderDetailsRepository.deleteById(orderDetails);
    }
}
