package com.alex.dto;

public class ClientCreationResponse {

    private final Client client;
    private final String login;
    private final String generatedPassword;

    public ClientCreationResponse(Client client, String login, String generatedPassword) {
        this.client = client;
        this.login = login;
        this.generatedPassword = generatedPassword;
    }

    public Client getClient() {
        return client;
    }

    public String getLogin() {
        return login;
    }

    public String getGeneratedPassword() {
        return generatedPassword;
    }
}
