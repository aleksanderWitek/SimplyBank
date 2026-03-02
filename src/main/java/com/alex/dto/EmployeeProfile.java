package com.alex.dto;

import java.time.LocalDateTime;

public class EmployeeProfile {

    private Long employeeId;
    private String firstName;
    private String lastName;
    private LocalDateTime employeeCreateDate;
    private LocalDateTime employeeModifyDate;
    private Long userAccountId;
    private String login;
    private String role;
    private LocalDateTime accountCreateDate;

    public EmployeeProfile(Long employeeId, String firstName, String lastName, LocalDateTime employeeCreateDate,
                           LocalDateTime employeeModifyDate, Long userAccountId, String login, String role,
                           LocalDateTime accountCreateDate) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeCreateDate = employeeCreateDate;
        this.employeeModifyDate = employeeModifyDate;
        this.userAccountId = userAccountId;
        this.login = login;
        this.role = role;
        this.accountCreateDate = accountCreateDate;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDateTime getEmployeeCreateDate() {
        return employeeCreateDate;
    }

    public LocalDateTime getEmployeeModifyDate() {
        return employeeModifyDate;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getAccountCreateDate() {
        return accountCreateDate;
    }
}
