package com.alex.controller;

import com.alex.dto.AdminProfile;
import com.alex.dto.Employee;
import com.alex.dto.EmployeeProfile;
import com.alex.dto.UserAccount;
import com.alex.exception.EmployeeNotFoundRuntimeException;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.service.IEmployeeService;
import com.alex.service.IUserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/employee", produces = "application/json; charset=UTF-8")
public class EmployeeController {

    private final IEmployeeService employeeService;
    private final IUserAccountService userAccountService;

    public EmployeeController(IEmployeeService employeeService, IUserAccountService userAccountService) {
        this.employeeService = employeeService;
        this.userAccountService = userAccountService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Employee> saveEmployee(@RequestBody Employee employee) {
        Employee employeeResult = employeeService.save(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeResult);
    }

    @PutMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<Void> updateEmployee(@PathVariable("id") Long id, @RequestBody Employee employee) {
        employeeService.updateById(id, employee);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Employee> findEmployeeById(@PathVariable("id") Long id) {
        Employee employee = employeeService.findById(id).orElseThrow(
                () -> new EmployeeNotFoundRuntimeException("There is no Employee with provided id:" + id));
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> findAllEmployees() {
        List<Employee> employees = employeeService.findAll();
        return ResponseEntity.ok(employees);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteEmployeeById(@PathVariable("id") Long id) {
        employeeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/profile")
    public ResponseEntity<EmployeeProfile> findEmployeeProfile(@RequestParam("userAccountId") Long userAccountId) {
        EmployeeProfile profile = employeeService.findProfileByUserAccountId(userAccountId)
                .orElseThrow(() -> new EmployeeNotFoundRuntimeException(
                        "There is no Employee profile for userAccountId: " + userAccountId));
        return ResponseEntity.ok(profile);
    }

    @GetMapping(path = "/admin-profile")
    public ResponseEntity<AdminProfile> findAdminProfile(@RequestParam("userAccountId") Long userAccountId) {
        UserAccount userAccount = userAccountService.findById(userAccountId)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException(
                        "There is no User Account with provided id: " + userAccountId));
        AdminProfile profile = new AdminProfile(
                userAccount.getId(),
                userAccount.getLogin(),
                userAccount.getRole().name(),
                userAccount.getCreateDate());
        return ResponseEntity.ok(profile);
    }
}
