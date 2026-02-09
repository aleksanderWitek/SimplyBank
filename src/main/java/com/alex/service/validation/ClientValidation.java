package com.alex.service.validation;

import com.alex.dto.Client;
import com.alex.exception.IllegalArgumentRuntimeException;

public class ClientValidation {

    public static void ensureClientPresent(Client client) {
        if(client == null) {
            throw new IllegalArgumentRuntimeException("Client is null");
        }
    }
}
