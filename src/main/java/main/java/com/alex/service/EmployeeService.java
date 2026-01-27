package main.java.com.alex.service;

import main.java.com.alex.dto.Employee;
import main.java.com.alex.repository.IEmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService implements IEmployeeService{

    private final IEmployeeRepository employeeRepository;

    public EmployeeService(IEmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    @Override
    public Employee save(Employee employee) {
        Long id = employeeRepository.save(employee);
        return new Employee(id, employee.getFirstName(), employee.getLastName(), employee.getCreateDate());
    }

    @Transactional
    @Override
    public void updateById(Long id, Employee employee) {
        employeeRepository.updateById(id, employee);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        employeeRepository.deleteById(id);
    }
}
