package com.alex.service.validation;

import com.alex.exception.NullPointerRuntimeException;

public class IdValidation {
    public static void ensureIdPresent(Long id) {
        if (id == null) {
            throw new NullPointerRuntimeException("Id is null");
        }
    }
}
