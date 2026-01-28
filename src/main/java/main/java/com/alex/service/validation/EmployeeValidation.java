package main.java.com.alex.service.validation;

import main.java.com.alex.dto.Employee;
import main.java.com.alex.exception.IllegalArgumentRuntimeException;

public class EmployeeValidation {
    public static void ensureEmployeePresent(Employee employee) {
        if(employee == null) {
            throw new IllegalArgumentRuntimeException("Employee is null");
        }
    }
}
