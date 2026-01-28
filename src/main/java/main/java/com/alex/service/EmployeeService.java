package main.java.com.alex.service;

import main.java.com.alex.dto.Employee;
import main.java.com.alex.repository.IEmployeeRepository;
import main.java.com.alex.service.validation.EmployeeValidation;
import main.java.com.alex.service.validation.IdValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        EmployeeValidation.ensureEmployeePresent(employee);

        Employee employeeWithCreateDate = new Employee(employee.getFirstName(), employee.getLastName(), LocalDateTime.now());
        Long id = employeeRepository.save(employeeWithCreateDate);
        return new Employee(id, employeeWithCreateDate.getFirstName(), employeeWithCreateDate.getLastName(),
                employeeWithCreateDate.getCreateDate());
    }

    @Transactional
    @Override
    public void updateById(Long id, Employee employee) {
        IdValidation.ensureIdPresent(id);
        EmployeeValidation.ensureEmployeePresent(employee);

        employeeRepository.updateById(id, employee);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Employee> findById(Long id) {
        IdValidation.ensureIdPresent(id);

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
        IdValidation.ensureIdPresent(id);

        employeeRepository.deleteById(id);
    }
}
