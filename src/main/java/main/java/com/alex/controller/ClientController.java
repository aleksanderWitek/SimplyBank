package main.java.com.alex.controller;

import main.java.com.alex.dto.Client;
import main.java.com.alex.dto.Password;
import main.java.com.alex.exception.ClientNotFoundRuntimeException;
import main.java.com.alex.service.IClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/client", produces = "application/json; charset=UTF-8")
public class ClientController {

    private final IClientService clientService;

    public ClientController(IClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Client> saveClient(@RequestBody Client client) {
        Client clientResult = clientService.save(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientResult);
    }

    @PutMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<Void> updateClient(@PathVariable("id") Long id, @RequestBody Client client) {
        clientService.updateById(id, client);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Client> findClientById(@PathVariable("id") Long id) {
        Client client = clientService.findById(id).orElseThrow(
                () -> new ClientNotFoundRuntimeException("There is no Client with provided id:" + id));
        return ResponseEntity.ok(client);
    }

    @GetMapping
    public ResponseEntity<List<Client>> findAllClients() {
        List<Client> clients = clientService.findAll();
        return ResponseEntity.ok(clients);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteClientById(@PathVariable("id") Long id) {
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{client_id}/password", consumes = "application/json")
    public ResponseEntity<Void> updateClientPassword(@PathVariable("client_id") Long clientId,
                                                     @RequestBody Password password) {
        clientService.updatePassword(clientId, password);
        return ResponseEntity.noContent().build();
    }
}
