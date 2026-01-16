package main.java.com.alex.service;

import main.java.com.alex.dto.Employee;

import java.util.List;
import java.util.Optional;

public interface IEmployee {
    Employee save(Employee employee);
    void update(Long id, Employee employee);
    Optional<Employee> findById(Long id);
    List<Employee> findAll();
    void deleteById(Long id);
}
