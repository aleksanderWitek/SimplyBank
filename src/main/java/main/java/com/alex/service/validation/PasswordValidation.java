package main.java.com.alex.service.validation;

import main.java.com.alex.exception.IllegalArgumentRuntimeException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordValidation {

    public static void ensurePasswordMeetsRequirements(String password) {
        if (password == null || password.length() < 10) {
            throw new IllegalArgumentRuntimeException("Password must be at least 10 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentRuntimeException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentRuntimeException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentRuntimeException("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentRuntimeException("Password must contain at least one special character");
        }
    }

    public static void ensureProvidedPasswordIsDifferentFromExistingPassword(
            String providedNewPassword,
            String currentEncodedPassword,
            PasswordEncoder passwordEncoder) {
        ensurePasswordExist(providedNewPassword);
        if(passwordEncoder.matches(providedNewPassword, currentEncodedPassword)){
            throw new IllegalArgumentRuntimeException("New password must be different from current password");
        }
    }

    public static void authenticatePassword(
            String providedPassword,
            String storedPassword,
            PasswordEncoder passwordEncoder) {
        ensurePasswordExist(providedPassword);
        if(!passwordEncoder.matches(providedPassword, storedPassword)){
            throw new IllegalArgumentRuntimeException("Invalid password");
        }
    }

    private static void ensurePasswordExist(String providedNewPassword) {
        if (providedNewPassword == null) {
            throw new IllegalArgumentRuntimeException("Provided password cannot be null");
        }
    }
}
