package com.alex.service.validation;

import com.alex.dto.Employee;
import com.alex.exception.IllegalArgumentRuntimeException;

public class EmployeeValidation {
    public static void ensureEmployeePresent(Employee employee) {
        if(employee == null) {
            throw new IllegalArgumentRuntimeException("Employee is null");
        }
    }
}
