package main.java.com.alex;

import main.java.com.alex.dto.Client;
import main.java.com.alex.service.IClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/")
public class ApplicationApi {

    private final IClientService clientService;

    public ApplicationApi(IClientService clientService) {
        this.clientService = clientService;
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
}
