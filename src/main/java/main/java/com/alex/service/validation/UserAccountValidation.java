package main.java.com.alex.service.validation;

import main.java.com.alex.UserAccountRole;
import main.java.com.alex.dto.UserAccount;
import main.java.com.alex.exception.IllegalArgumentRuntimeException;
import main.java.com.alex.exception.NullPointerRuntimeException;

public class UserAccountValidation {

    public static void ensureUserAccountPresent(UserAccount userAccount) {
        if(userAccount == null) {
            throw new IllegalArgumentRuntimeException("UserAccount is null");
        }
    }

    public static void ensureFirstNamePresent(String firstName) {
        if(firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentRuntimeException("First Name is null or empty");
        }
    }

    public static void ensureLastNamePresent(String lastName) {
        if(lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentRuntimeException("Last Name is null or empty");
        }
    }

    public static void ensureUserAccountRoleIsCorrect(String userAccountRole){
        if (userAccountRole == null || userAccountRole.isBlank()) {
            throw new NullPointerRuntimeException("User Account Role is null or empty");
        }

        try {
            UserAccountRole.valueOf(userAccountRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentRuntimeException("userAccountRole is invalid: " + userAccountRole);
        }
    }
}
