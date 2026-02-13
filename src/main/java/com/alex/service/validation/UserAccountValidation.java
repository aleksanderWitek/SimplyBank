package com.alex.service.validation;

import com.alex.UserAccountRole;
import com.alex.dto.UserAccount;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;

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

    public static void ensureUserAccountRoleIsCorrect(UserAccountRole role){
        if (role == null) {
            throw new NullPointerRuntimeException("User Account Role is null");
        }
    }
}
