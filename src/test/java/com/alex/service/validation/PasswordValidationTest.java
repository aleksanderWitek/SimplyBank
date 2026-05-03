package com.alex.service.validation;

import com.alex.exception.IllegalArgumentRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordValidationTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    // ensurePasswordMeetsRequirements ------------------------------------------------------------

    @Test
    void ensurePasswordMeetsRequirements_nullPassword_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> PasswordValidation.ensurePasswordMeetsRequirements(null, 8))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Password must be at least 8 characters long");
    }

    @Test
    void ensurePasswordMeetsRequirements_tooShort_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> PasswordValidation.ensurePasswordMeetsRequirements("Ab1!", 8))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Password must be at least 8 characters long");
    }

    @Test
    void ensurePasswordMeetsRequirements_noUppercase_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> PasswordValidation.ensurePasswordMeetsRequirements("abcdef1!", 8))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Password must contain at least one uppercase letter");
    }

    @Test
    void ensurePasswordMeetsRequirements_noLowercase_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> PasswordValidation.ensurePasswordMeetsRequirements("ABCDEF1!", 8))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Password must contain at least one lowercase letter");
    }

    @Test
    void ensurePasswordMeetsRequirements_noDigit_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> PasswordValidation.ensurePasswordMeetsRequirements("Abcdefg!", 8))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Password must contain at least one digit");
    }

    @Test
    void ensurePasswordMeetsRequirements_noSpecialChar_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> PasswordValidation.ensurePasswordMeetsRequirements("Abcdefg1", 8))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Password must contain at least one special character");
    }

    @Test
    void ensurePasswordMeetsRequirements_validPassword_doesNotThrow() {
        assertThatCode(() -> PasswordValidation.ensurePasswordMeetsRequirements("Abcdefg1!", 8))
                .doesNotThrowAnyException();
    }

    // ensureProvidedPasswordIsDifferentFromExistingPassword --------------------------------------

    @Test
    void ensureProvidedPasswordIsDifferentFromExistingPassword_nullNewPassword_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> PasswordValidation.ensureProvidedPasswordIsDifferentFromExistingPassword(
                null, "encoded", passwordEncoder))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Provided password cannot be null");
    }

    @Test
    void ensureProvidedPasswordIsDifferentFromExistingPassword_sameAsCurrent_throwsIllegalArgumentRuntimeException() {
        when(passwordEncoder.matches("NewPwd1!", "encoded-current")).thenReturn(true);

        assertThatThrownBy(() -> PasswordValidation.ensureProvidedPasswordIsDifferentFromExistingPassword(
                "NewPwd1!", "encoded-current", passwordEncoder))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("New password must be different from current password");
    }

    @Test
    void ensureProvidedPasswordIsDifferentFromExistingPassword_differentFromCurrent_doesNotThrow() {
        when(passwordEncoder.matches("NewPwd1!", "encoded-current")).thenReturn(false);

        assertThatCode(() -> PasswordValidation.ensureProvidedPasswordIsDifferentFromExistingPassword(
                "NewPwd1!", "encoded-current", passwordEncoder))
                .doesNotThrowAnyException();
    }

    // authenticatePassword -----------------------------------------------------------------------

    @Test
    void authenticatePassword_nullProvidedPassword_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> PasswordValidation.authenticatePassword(null, "encoded", passwordEncoder))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Provided password cannot be null");
    }

    @Test
    void authenticatePassword_wrongPassword_throwsIllegalArgumentRuntimeException() {
        when(passwordEncoder.matches("wrong", "stored")).thenReturn(false);

        assertThatThrownBy(() -> PasswordValidation.authenticatePassword("wrong", "stored", passwordEncoder))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Invalid password");
    }

    @Test
    void authenticatePassword_correctPassword_doesNotThrow() {
        when(passwordEncoder.matches("correct", "stored")).thenReturn(true);

        assertThatCode(() -> PasswordValidation.authenticatePassword("correct", "stored", passwordEncoder))
                .doesNotThrowAnyException();
    }

    @Test
    void constructor_canBeInstantiated_forCoverage() {
        assertThatCode(PasswordValidation::new).doesNotThrowAnyException();
    }
}
