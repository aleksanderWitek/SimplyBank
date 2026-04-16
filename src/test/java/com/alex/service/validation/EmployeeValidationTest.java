package com.alex.service.validation;

import com.alex.dto.Employee;
import com.alex.exception.IllegalArgumentRuntimeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeValidationTest {

    @Test
    void ensureEmployeePresent_nullEmployee_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> EmployeeValidation.ensureEmployeePresent(null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Employee is null");
    }

    @Test
    void ensureEmployeePresent_presentEmployee_doesNotThrow() {
        Employee employee = new Employee();
        assertThatCode(() -> EmployeeValidation.ensureEmployeePresent(employee)).doesNotThrowAnyException();
    }

    @Test
    void constructor_canBeInstantiated_forCoverage() {
        assertThatCode(EmployeeValidation::new).doesNotThrowAnyException();
    }
}
