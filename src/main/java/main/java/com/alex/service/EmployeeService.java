package main.java.com.alex.service;

import main.java.com.alex.UserAccountRole;
import main.java.com.alex.dto.Employee;
import main.java.com.alex.dto.Password;
import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.UserAccountNotFoundRuntimeException;
import main.java.com.alex.repository.IEmployeeRepository;
import main.java.com.alex.repository.IUserAccountEmployeeRepository;
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
    private final IUserAccountEmployeeRepository userAccountEmployeeRepository;
    private final IUserAccountService userAccountService;

    public EmployeeService(IEmployeeRepository employeeRepository, IUserAccountEmployeeRepository userAccountEmployeeRepository, IUserAccountService userAccountService) {
        this.employeeRepository = employeeRepository;
        this.userAccountEmployeeRepository = userAccountEmployeeRepository;
        this.userAccountService = userAccountService;
    }

    @Transactional
    @Override
    public Employee save(Employee employee) {
        EmployeeValidation.ensureEmployeePresent(employee);

        Employee employeeWithCreateDate = new Employee(employee.getFirstName(), employee.getLastName(),
                LocalDateTime.now());
        Long id = employeeRepository.save(employeeWithCreateDate);
        Employee savedEmployee = new Employee(id, employeeWithCreateDate.getFirstName(), employeeWithCreateDate.getLastName(),
                employeeWithCreateDate.getCreateDate());
        UserAccount userAccount = userAccountService.save(employee.getFirstName(), employee.getLastName(),
                UserAccountRole.EMPLOYEE);
        savedEmployee.addUserAccount(userAccount);
        userAccountEmployeeRepository.linkUserAccountToEmployee(userAccount.getId(), id);
        return savedEmployee;
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
        Long userAccountId = userAccountEmployeeRepository.findUserAccountIdByEmployeeId(id)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException("There is no Employee with provided id:" + id));
        userAccountEmployeeRepository.unlinkUserAccountFromEmployee(userAccountId, id);
        userAccountService.deleteById(userAccountId);
        employeeRepository.deleteById(id);
    }

    @Override
    public void updatePassword(Long employeeId, Password password) {
        IdValidation.ensureIdPresent(employeeId);
        Long userAccountId = userAccountEmployeeRepository.findUserAccountIdByEmployeeId(employeeId)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException("There is no Employee with provided id:" + employeeId));

        userAccountService.updatePassword(userAccountId, password);
    }
}
