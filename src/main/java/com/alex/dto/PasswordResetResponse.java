package com.alex.dto;

public class PasswordResetResponse {

    private final Long userAccountId;
    private final String login;
    private final String newPassword;

    public PasswordResetResponse(Long userAccountId, String login, String newPassword) {
        this.userAccountId = userAccountId;
        this.login = login;
        this.newPassword = newPassword;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public String getLogin() {
        return login;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
