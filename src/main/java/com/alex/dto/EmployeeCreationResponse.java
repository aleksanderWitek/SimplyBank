package com.alex.dto;

public class EmployeeCreationResponse {

    private final Employee employee;
    private final String login;
    private final String generatedPassword;

    public EmployeeCreationResponse(Employee employee, String login, String generatedPassword) {
        this.employee = employee;
        this.login = login;
        this.generatedPassword = generatedPassword;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getLogin() {
        return login;
    }

    public String getGeneratedPassword() {
        return generatedPassword;
    }
}
