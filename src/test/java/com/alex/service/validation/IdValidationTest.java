package com.alex.service.validation;

import com.alex.exception.NullPointerRuntimeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdValidationTest {

    @Test
    void ensureIdPresent_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> IdValidation.ensureIdPresent(null))
                .isInstanceOf(NullPointerRuntimeException.class)
                .hasMessage("Id is null");
    }

    @Test
    void ensureIdPresent_presentId_doesNotThrow() {
        assertThatCode(() -> IdValidation.ensureIdPresent(42L)).doesNotThrowAnyException();
    }

    @Test
    void constructor_canBeInstantiated_forCoverage() {
        assertThatCode(IdValidation::new).doesNotThrowAnyException();
    }
}
