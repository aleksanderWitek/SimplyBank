package com.alex.service.validation;

import com.alex.UserAccountRole;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void ensureFirstNamePresent_blankFirstName_throwsIllegalArgumentRuntimeException(String blank) {
        assertThatThrownBy(() -> UserAccountValidation.ensureFirstNamePresent(blank))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("First Name is null or empty");
    }

    @Test
    void ensureFirstNamePresent_nullFirstName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> UserAccountValidation.ensureFirstNamePresent(null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("First Name is null or empty");
    }

    @Test
    void ensureFirstNamePresent_presentFirstName_doesNotThrow() {
        assertThatCode(() -> UserAccountValidation.ensureFirstNamePresent("Alice"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void ensureLastNamePresent_blankLastName_throwsIllegalArgumentRuntimeException(String blank) {
        assertThatThrownBy(() -> UserAccountValidation.ensureLastNamePresent(blank))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Last Name is null or empty");
    }

    @Test
    void ensureLastNamePresent_nullLastName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> UserAccountValidation.ensureLastNamePresent(null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Last Name is null or empty");
    }

    @Test
    void ensureLastNamePresent_presentLastName_doesNotThrow() {
        assertThatCode(() -> UserAccountValidation.ensureLastNamePresent("Smith"))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureUserAccountRoleIsCorrect_nullRole_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> UserAccountValidation.ensureUserAccountRoleIsCorrect(null))
                .isInstanceOf(NullPointerRuntimeException.class)
                .hasMessage("User Account Role is null");
    }

    @ParameterizedTest
    @EnumSource(UserAccountRole.class)
    void ensureUserAccountRoleIsCorrect_presentRole_doesNotThrow(UserAccountRole role) {
        assertThatCode(() -> UserAccountValidation.ensureUserAccountRoleIsCorrect(role))
                .doesNotThrowAnyException();
    }

    @Test
    void constructor_canBeInstantiated_forCoverage() {
        assertThatCode(UserAccountValidation::new).doesNotThrowAnyException();
    }
}
