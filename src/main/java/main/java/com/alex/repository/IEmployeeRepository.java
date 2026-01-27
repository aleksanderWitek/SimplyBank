package main.java.com.alex.repository;

import main.java.com.alex.dto.Employee;

import java.util.List;
import java.util.Optional;

public interface IEmployeeRepository {

    Long save(Employee employee);
    Optional<Employee> findById(Long id);
    List<Employee> findAll();
    void update(Long id, Employee employee);
    void deleteById(Long id);
}
