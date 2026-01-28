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

    public static void validateIfUserAccountRoleIsCorrect(String userAccountRole){
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
