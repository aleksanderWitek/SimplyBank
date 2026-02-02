package main.java.com.alex.service;

import main.java.com.alex.dto.Employee;
import main.java.com.alex.dto.Password;

import java.util.List;
import java.util.Optional;

public interface IEmployeeService {
    Employee save(Employee employee);
    void updateById(Long id, Employee employee);
    Optional<Employee> findById(Long id);
    List<Employee> findAll();
    void deleteById(Long id);
    void updatePassword(Long employeeId, Password password);
}
