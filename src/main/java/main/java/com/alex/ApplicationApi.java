package main.java.com.alex;

import main.java.com.alex.dto.Client;
import main.java.com.alex.dto.Employee;
import main.java.com.alex.dto.Password;
import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.ClientNotFoundRuntimeException;
import main.java.com.alex.exception.EmployeeNotFoundRuntimeException;
import main.java.com.alex.exception.UserAccountNotFoundRuntimeException;
import main.java.com.alex.service.IClientService;
import main.java.com.alex.service.IEmployeeService;
import main.java.com.alex.service.IUserAccountService;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/")
public class ApplicationApi {

    private final IClientService clientService;
    private final IEmployeeService employeeService;
    private final IUserAccountService userAccountService;

    public ApplicationApi(IClientService clientService, IEmployeeService employeeService,
                          IUserAccountService userAccountService) {
        this.clientService = clientService;
        this.employeeService = employeeService;
        this.userAccountService = userAccountService;
    }

    @PostMapping(path = "client/save", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Client> saveClient(@RequestBody Client client) {
        Client clientResult = clientService.save(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientResult);
    }

    @PutMapping(path = "client/update/{id}", consumes = "application/json")
    public ResponseEntity<Void> updateClient(@PathVariable("id") Long id, @RequestBody Client client) {
        clientService.updateById(id, client);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "client/get/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Client> findClientById(@PathVariable("id") Long id) {
        Client client = clientService.findById(id).orElseThrow(
                () -> new ClientNotFoundRuntimeException("There is no Client with provided id:" + id));
        return ResponseEntity.ok(client);
    }

    @GetMapping(path = "client/find_all", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Client>> findAllClients() {
        List<Client> clients = clientService.findAll();
        return ResponseEntity.ok(clients);
    }

    @DeleteMapping(path = "client/delete/{id}")
    public ResponseEntity<Void> deleteClientById(@PathVariable("id") Long id) {
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "client/update_password/{client_id}", consumes = "application/json")
    public ResponseEntity<Void> updateClientPassword(@PathVariable("client_id") Long clientId,
                                                     @RequestBody Password password) {
        clientService.updatePassword(clientId, password);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "employee/save", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Employee> saveEmployee(@RequestBody Employee employee) {
        Employee employeeResult = employeeService.save(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeResult);
    }

    @PutMapping(path = "employee/update/{id}", consumes = "application/json")
    public ResponseEntity<Void> updateEmployee(@PathVariable("id") Long id, @RequestBody Employee employee) {
        employeeService.updateById(id, employee);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "employee/get/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Employee> findEmployeeById(@PathVariable("id") Long id) {
        Employee employee = employeeService.findById(id).orElseThrow(
                () -> new EmployeeNotFoundRuntimeException("There is no Employee with provided id:" + id));
        return ResponseEntity.ok(employee);
    }

    @GetMapping(path = "employee/find_all", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Employee>> findAllEmployees() {
        List<Employee> employees = employeeService.findAll();
        return ResponseEntity.ok(employees);
    }

    @DeleteMapping(path = "employee/delete/{id}")
    public ResponseEntity<Void> deleteEmployeeById(@PathVariable("id") Long id) {
        employeeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "employee/update_password/{employee_id}", consumes = "application/json")
    public ResponseEntity<Void> updateEmployeePassword(@PathVariable("employee_id") Long employeeId,
                                                     @RequestBody Password password) {
        employeeService.updatePassword(employeeId, password);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "user_account/get/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<UserAccount> findUserAccountById(@PathVariable("id") Long id) {
        UserAccount userAccount = userAccountService.findById(id).orElseThrow(
                () -> new UserAccountNotFoundRuntimeException("There is no User with provided id:" + id));
        return ResponseEntity.ok(userAccount);
    }

    @GetMapping(path = "user_account/find_all", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<UserAccount>> findAllUserAccounts() {
        List<UserAccount> userAccounts = userAccountService.findAll();
        return ResponseEntity.ok(userAccounts);
    }
}
