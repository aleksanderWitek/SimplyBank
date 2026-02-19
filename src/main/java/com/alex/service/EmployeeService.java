package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.Employee;
import com.alex.dto.Password;
import com.alex.dto.UserAccount;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IEmployeeRepository;
import com.alex.repository.IUserAccountEmployeeRepository;
import com.alex.service.validation.EmployeeValidation;
import com.alex.service.validation.IdValidation;
import com.alex.service.validation.UserAccountValidation;
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
        UserAccountValidation.ensureFirstNamePresent(employee.getFirstName());
        UserAccountValidation.ensureLastNamePresent(employee.getLastName());

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
        UserAccountValidation.ensureFirstNamePresent(employee.getFirstName());
        UserAccountValidation.ensureLastNamePresent(employee.getLastName());

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

    @Transactional
    @Override
    public void updatePassword(Long employeeId, Password password) {
        IdValidation.ensureIdPresent(employeeId);
        Long userAccountId = userAccountEmployeeRepository.findUserAccountIdByEmployeeId(employeeId)
                .orElseThrow(() -> new UserAccountNotFoundRuntimeException("There is no Employee with provided id:" + employeeId));

        userAccountService.updatePassword(userAccountId, password);
    }
}
