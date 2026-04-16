package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.Employee;
import com.alex.dto.EmployeeProfile;
import com.alex.dto.UserAccount;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IEmployeeRepository;
import com.alex.repository.IUserAccountEmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private IEmployeeRepository employeeRepository;
    @Mock private IUserAccountEmployeeRepository userAccountEmployeeRepository;
    @Mock private IUserAccountService userAccountService;

    @InjectMocks private EmployeeService service;

    private static UserAccount newUser(Long id) {
        return new UserAccount(id, "bobjon", "pwd", UserAccountRole.EMPLOYEE, LocalDateTime.now());
    }

    // save ----------------------------------------------------------------------------------------

    @Test
    void save_nullEmployee_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.save(null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Employee is null");
    }

    @Test
    void save_nullFirstName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.save(new Employee(null, "Jones", null)))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void save_nullLastName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.save(new Employee("Bob", null, null)))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void save_valid_persistsEmployeeCreatesUserAndLinks() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(42L);
        when(userAccountService.save("Bob", "Jones", UserAccountRole.EMPLOYEE)).thenReturn(newUser(99L));

        Employee result = service.save(new Employee("Bob", "Jones", null));

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getFirstName()).isEqualTo("Bob");
        assertThat(result.getLastName()).isEqualTo("Jones");
        verify(userAccountEmployeeRepository).linkUserAccountToEmployee(99L, 42L);
    }

    // updateById ----------------------------------------------------------------------------------

    @Test
    void updateById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.updateById(null, new Employee("A", "B", null)))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void updateById_nullEmployee_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.updateById(1L, null))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void updateById_nullFirstName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.updateById(1L, new Employee(null, "B", null)))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void updateById_nullLastName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.updateById(1L, new Employee("A", null, null)))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void updateById_valid_delegatesToRepository() {
        Employee employee = new Employee("A", "B", null);
        service.updateById(1L, employee);
        verify(employeeRepository).updateById(1L, employee);
    }

    // findById ------------------------------------------------------------------------------------

    @Test
    void findById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findById_delegatesToRepository() {
        Employee employee = new Employee(1L, "A", "B", LocalDateTime.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThat(service.findById(1L)).contains(employee);
    }

    // findAll -------------------------------------------------------------------------------------

    @Test
    void findAll_delegatesToRepository() {
        List<Employee> employees = List.of();
        when(employeeRepository.findAll()).thenReturn(employees);
        assertThat(service.findAll()).isSameAs(employees);
    }

    // deleteById ----------------------------------------------------------------------------------

    @Test
    void deleteById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.deleteById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void deleteById_noLinkedUserAccount_throwsUserAccountNotFoundRuntimeException() {
        when(userAccountEmployeeRepository.findUserAccountIdByEmployeeId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById(1L))
                .isInstanceOf(UserAccountNotFoundRuntimeException.class)
                .hasMessageContaining("There is no Employee with provided id:1");

        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    void deleteById_happyPath_unlinksAndDeletes() {
        when(userAccountEmployeeRepository.findUserAccountIdByEmployeeId(1L)).thenReturn(Optional.of(99L));

        service.deleteById(1L);

        verify(userAccountEmployeeRepository).unlinkUserAccountFromEmployee(99L, 1L);
        verify(userAccountService).deleteById(99L);
        verify(employeeRepository).deleteById(1L);
    }

    // findProfileByUserAccountId ------------------------------------------------------------------

    @Test
    void findProfileByUserAccountId_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findProfileByUserAccountId(null))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findProfileByUserAccountId_delegatesToRepository() {
        EmployeeProfile profile = new EmployeeProfile(1L, "A", "B", LocalDateTime.now(), null,
                2L, "a", "EMPLOYEE", LocalDateTime.now());
        when(employeeRepository.findProfileByUserAccountId(2L)).thenReturn(Optional.of(profile));
        assertThat(service.findProfileByUserAccountId(2L)).contains(profile);
    }
}
