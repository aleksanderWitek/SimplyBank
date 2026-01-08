package main.java.com.alex;

import main.java.com.alex.dto.Account;
import main.java.com.alex.dto.Client;
import main.java.com.alex.dto.ClientDetails;
import main.java.com.alex.dto.OrderDetails;
import main.java.com.alex.service.IAccountService;
import main.java.com.alex.service.IClientDetailsService;
import main.java.com.alex.service.IClientService;
import main.java.com.alex.service.IOrderDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/")
public class ApplicationApi {

    private final IClientService clientService;
    private final IClientDetailsService clientDetailsService;
    private final IOrderDetailsService orderDetailsService;
    private final IAccountService accountService;

    public ApplicationApi(IClientService clientService, IClientDetailsService clientDetailsService,
                          IOrderDetailsService orderDetailsService, IAccountService accountService) {
        this.clientService = clientService;
        this.clientDetailsService = clientDetailsService;
        this.orderDetailsService = orderDetailsService;
        this.accountService = accountService;
    }

    @GetMapping(path = "client/get/{id}", produces = "application/json; charset=UTF-8")
    public Client getClient(@PathVariable("id") Long id) {
        return clientService.findById(id).orElseThrow(() -> new RuntimeException("There is no Client with id:" + id));
    }

    @GetMapping(path = "client/find_all", produces = "application/json; charset=UTF-8")
    public List<Client> findAllClients() {
        return clientService.findAll();
    }

    @DeleteMapping(path = "client/delete/{id}")
    public void deleteClient(@PathVariable("id") Long id) {
        clientService.deleteById(id);
    }

    @PostMapping(path = "client/save", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public Client saveClient(@RequestBody ClientDetails clientDetails){
        return clientDetailsService.save(clientDetails);
    }

    @GetMapping(path = "account/get/{id}", produces = "application/json; charset=UTF-8")
    public Account getAccount(@PathVariable("id") Long id) {
        return accountService.findById(id).orElseThrow(() -> new RuntimeException("There is no Account with id:" + id));
    }

    @GetMapping(path = "account/find_all", produces = "application/json; charset=UTF-8")
    public List<Account> findAllAccounts() {
        return accountService.findAll();
    }

    @DeleteMapping(path = "account/delete/{id}")
    public void deleteAccount(@PathVariable("id") Long id) {
        accountService.deleteById(id);
    }

    @PostMapping(path = "account/save/{clientId}", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public Account saveAccount(@RequestBody Account accountDetails,
                               @PathVariable("clientId") Long clientId) {
        return accountService.save(accountDetails, clientId);
    }

    @GetMapping(path = "order_details/get/{id}", produces = "application/json; charset=UTF-8")
    public OrderDetails getOrderDetails(@PathVariable("id") Long id) {
        return orderDetailsService.findById(id).orElseThrow(() -> new RuntimeException("There is no OrderDetails with id:" + id));
    }

    @GetMapping(path = "order_details/find_all", produces = "application/json; charset=UTF-8")
    public List<OrderDetails> findAllOrderDetails() {
        return orderDetailsService.findAll();
    }

    @DeleteMapping(path = "order_details/delete/{id}")
    public void deleteOrderDetails(@PathVariable("id") Long id) {
        orderDetailsService.deleteById(id);
    }

    @PostMapping(path = "order_details/save/{clientId}", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public OrderDetails saveOrderDetails(@RequestBody OrderDetails orderDetails,
                                         @PathVariable("clientId") Long clientId) {
        return orderDetailsService.save(orderDetails.getItem(), clientId);
    }
}
