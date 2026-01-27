package main.java.com.alex.service.validation;

import main.java.com.alex.UserAccountRole;
import main.java.com.alex.exception.IllegalArgumentRuntimeException;
import main.java.com.alex.exception.NullPointerRuntimeException;

public class UserAccountValidation {

    public static void validateIfUserAccountRoleIsCorrect(String userAccountRole, String message){
        if (userAccountRole == null || userAccountRole.isBlank()) {
            throw new NullPointerRuntimeException(message);
        }

        try {
            UserAccountRole.valueOf(userAccountRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentRuntimeException(message);
        }
    }
}
