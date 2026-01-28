package main.java.com.alex;

import main.java.com.alex.dto.Client;
import main.java.com.alex.exception.ClientNotFoundRuntimeException;
import main.java.com.alex.service.IClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/")
public class ApplicationApi {

    private final IClientService clientService;

    public ApplicationApi(IClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping(path = "client/save", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Client> saveClient(@RequestBody Client client) {
        Client clientResult = clientService.save(client);
        return ResponseEntity.ok(clientResult);
    }

    @PutMapping(path = "client/update/{id}", consumes = "application/json")
    public ResponseEntity<Void> updateClient(@PathVariable("id") Long id, @RequestBody Client client) {
        clientService.updateById(id, client);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "client/get/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Client> getClient(@PathVariable("id") Long id) {
        Client client = clientService.findById(id).orElseThrow(() -> new ClientNotFoundRuntimeException("There is no Client with provided id:" + id));
        return ResponseEntity.ok(client);
    }

    @GetMapping(path = "client/find_all", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Client>> findAllClients() {
        List<Client> clients = clientService.findAll();
        return ResponseEntity.ok(clients);
    }

    @DeleteMapping(path = "client/delete/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable("id") Long id) {
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
