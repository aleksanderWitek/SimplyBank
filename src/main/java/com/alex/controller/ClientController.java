package com.alex.controller;

import com.alex.dto.Client;
import com.alex.dto.ClientCreationResponse;
import com.alex.dto.ClientProfile;
import com.alex.exception.ClientNotFoundRuntimeException;
import com.alex.service.IClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/client", produces = "application/json; charset=UTF-8")
public class ClientController {

    private final IClientService clientService;

    public ClientController(IClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<ClientCreationResponse> saveClient(@RequestBody Client client) {
        ClientCreationResponse response = clientService.save(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<Void> updateClient(@PathVariable("id") Long id, @RequestBody Client client) {
        clientService.updateById(id, client);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/profile")
    public ResponseEntity<ClientProfile> findClientProfile(@RequestParam("userAccountId") Long userAccountId) {
        ClientProfile profile = clientService.findProfileByUserAccountId(userAccountId)
                .orElseThrow(() -> new ClientNotFoundRuntimeException(
                        "There is no Client profile for userAccountId: " + userAccountId));
        return ResponseEntity.ok(profile);
    }

    @GetMapping(path = "/{id}/profile")
    public ResponseEntity<ClientProfile> findClientProfileById(@PathVariable("id") Long id) {
        ClientProfile profile = clientService.findProfileById(id)
                .orElseThrow(() -> new ClientNotFoundRuntimeException(
                        "There is no Client profile for id: " + id));
        return ResponseEntity.ok(profile);
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

    @PostMapping(path = "/profile/delete", consumes = "application/json")
    public ResponseEntity<Void> deleteOwnAccount(@RequestBody Map<String, String> body, Principal principal) {
        clientService.deleteOwnAccount(principal, body.get("currentPassword"));
        return ResponseEntity.noContent().build();
    }

}
