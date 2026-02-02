package main.java.com.alex.service.validation;

import main.java.com.alex.exception.IllegalArgumentRuntimeException;

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

    public static void ensureProvidedPasswordIsDifferentFromExistingPassword(String providedNewPassword, String currentPassword) {
        ensurePasswordExist(providedNewPassword);
        if(providedNewPassword.equals(currentPassword)){
            throw new IllegalArgumentRuntimeException("New password must be different from current password");
        }
    }

    public static void ensureCurrentPasswordMatches(String providedPassword, String currentPassword) {
        ensurePasswordExist(providedPassword);
        if(!providedPassword.equals(currentPassword)){
            throw new IllegalArgumentRuntimeException("Provided Password is not matching account password");
        }
    }

    private static void ensurePasswordExist(String providedNewPassword) {
        if (providedNewPassword == null) {
            throw new IllegalArgumentRuntimeException("Provided password cannot be null");
        }
    }
}
