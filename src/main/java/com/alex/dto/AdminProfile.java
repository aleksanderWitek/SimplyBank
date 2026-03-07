package com.alex.dto;

import java.time.LocalDateTime;

public class AdminProfile {

    private Long userAccountId;
    private String login;
    private String role;
    private LocalDateTime accountCreateDate;

    public AdminProfile(Long userAccountId, String login, String role, LocalDateTime accountCreateDate) {
        this.userAccountId = userAccountId;
        this.login = login;
        this.role = role;
        this.accountCreateDate = accountCreateDate;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getAccountCreateDate() {
        return accountCreateDate;
    }
}
