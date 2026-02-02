package main.java.com.alex.service;

import main.java.com.alex.dto.Employee;
import main.java.com.alex.dto.Password;
import main.java.com.alex.exception.UserAccountNotFoundRuntimeException;
import main.java.com.alex.repository.IEmployeeRepository;
import main.java.com.alex.repository.IUserAccountClientRepository;
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
    private final IUserAccountClientRepository userAccountClientRepository;
    private final IUserAccountService userAccountService;

    public EmployeeService(IEmployeeRepository employeeRepository, IUserAccountClientRepository userAccountClientRepository, IUserAccountService userAccountService) {
        this.employeeRepository = employeeRepository;
        this.userAccountClientRepository = userAccountClientRepository;
        this.userAccountService = userAccountService;
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

    @Override
    public void updatePassword(Long employeeId, Password password) {
        IdValidation.ensureIdPresent(employeeId);
        Long userAccountId = userAccountClientRepository.findUserAccountIdByClientId(employeeId)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException("There is no Client with provided id:" + employeeId));

        userAccountService.updatePassword(userAccountId, password);
    }
}
