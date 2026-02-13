package com.alex.controller;

import com.alex.dto.Employee;
import com.alex.dto.Password;
import com.alex.exception.EmployeeNotFoundRuntimeException;
import com.alex.service.IEmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/employee", produces = "application/json; charset=UTF-8")
public class EmployeeController {

    private final IEmployeeService employeeService;

    public EmployeeController(IEmployeeService employeeService) {
        this.employeeService = employeeService;
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

    @PutMapping(path = "/{employee_id}/password", consumes = "application/json")
    public ResponseEntity<Void> updateEmployeePassword(@PathVariable("employee_id") Long employeeId,
                                                       @RequestBody Password password) {
        employeeService.updatePassword(employeeId, password);
        return ResponseEntity.noContent().build();
    }
}
