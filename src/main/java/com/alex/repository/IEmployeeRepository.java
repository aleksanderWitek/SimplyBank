package com.alex.repository;

import com.alex.dto.Employee;

import java.util.List;
import java.util.Optional;

public interface IEmployeeRepository {

    Long save(Employee employee);
    Optional<Employee> findById(Long id);
    List<Employee> findAll();
    void updateById(Long id, Employee employee);
    void deleteById(Long id);
}
