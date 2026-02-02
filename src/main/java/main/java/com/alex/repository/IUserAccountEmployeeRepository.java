package main.java.com.alex.repository;

import java.util.Optional;

public interface IUserAccountEmployeeRepository {
    void linkUserAccountToEmployee(Long userAccountId, Long employeeId);
    void unlinkUserAccountFromEmployee(Long userAccountId, Long employeeId);
    Optional<Long> findUserAccountIdByEmployeeId(Long employeeId);
}
