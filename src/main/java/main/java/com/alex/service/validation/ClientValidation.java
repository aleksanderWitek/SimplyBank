package main.java.com.alex.service.validation;

import main.java.com.alex.dto.Client;
import main.java.com.alex.exception.IllegalArgumentRuntimeException;

public class ClientValidation {

    public static void ensureClientPresent(Client client) {
        if(client == null) {
            throw new IllegalArgumentRuntimeException("Client is null");
        }
    }
}
