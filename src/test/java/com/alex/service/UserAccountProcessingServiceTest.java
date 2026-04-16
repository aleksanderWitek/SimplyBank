package com.alex.service;

import com.alex.exception.IllegalArgumentRuntimeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountProcessingServiceTest {

    private final UserAccountProcessingService service = new UserAccountProcessingService();

    // generateLogin -------------------------------------------------------------------------------

    @Test
    void generateLogin_nullFirstName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.generateLogin(null, "Smith"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("firstName and lastName must be provided");
    }

    @Test
    void generateLogin_nullLastName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.generateLogin("Alice", null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("firstName and lastName must be provided");
    }

    @Test
    void generateLogin_blankFirstName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.generateLogin("  ", "Smith"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("firstName and lastName must be non-blank");
    }

    @Test
    void generateLogin_blankLastName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.generateLogin("Alice", "  "))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("firstName and lastName must be non-blank");
    }

    @Test
    void generateLogin_validNames_returns14CharLowercaseLogin() {
        String login = service.generateLogin("Alice", "Smith");

        assertThat(login).hasSize(14);
        assertThat(login).startsWith("alismi");
        assertThat(login).isEqualTo(login.toLowerCase());
    }

    @Test
    void generateLogin_shortNames_doublesUntilLongEnough() {
        String login = service.generateLogin("Al", "Bo");
        // "Al" -> "AlAl" => first 3 = "ala"; "Bo" -> "BoBo" => first 3 = "bob"
        assertThat(login).startsWith("alabob");
        assertThat(login).hasSize(14);
    }

    @Test
    void generateLogin_oneCharNames_doublesMultipleTimes() {
        String login = service.generateLogin("A", "B");
        // "A" doubles to "AA", "AAAA" -> first 3 = "aaa"; same for "B"
        assertThat(login).startsWith("aaabbb");
        assertThat(login).hasSize(14);
    }

    @Test
    void generateLogin_trimsWhitespace() {
        String login = service.generateLogin("  Alice  ", "  Smith  ");
        assertThat(login).startsWith("alismi");
    }

    // generatePassword ----------------------------------------------------------------------------

    @Test
    void generatePassword_returns12CharString() {
        String password = service.generatePassword();
        assertThat(password).hasSize(12);
    }

    @Test
    void generatePassword_containsUppercase() {
        String password = service.generatePassword();
        assertThat(password).matches(".*[A-Z].*");
    }

    @Test
    void generatePassword_containsLowercase() {
        String password = service.generatePassword();
        assertThat(password).matches(".*[a-z].*");
    }

    @Test
    void generatePassword_containsDigit() {
        String password = service.generatePassword();
        assertThat(password).matches(".*\\d.*");
    }

    @Test
    void generatePassword_containsSpecial() {
        String password = service.generatePassword();
        assertThat(password).matches(".*[!@#$%^&*].*");
    }

    @Test
    void generatePassword_calledTwice_returnsDifferentValues() {
        String first = service.generatePassword();
        String second = service.generatePassword();
        assertThat(first).isNotEqualTo(second);
    }
}
