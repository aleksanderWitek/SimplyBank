package com.alex.dto;

import com.alex.UserAccountRole;

import java.time.LocalDateTime;

public class UserAccount {

    private Long id;

    private String login;

    private String password;

    private UserAccountRole role;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private LocalDateTime deleteDate;

    public UserAccount() {
    }

    public UserAccount(String login, String password, UserAccountRole role, LocalDateTime createDate) {
        this.login = login;
        this.password = password;
        this.role = role;
        this.createDate = createDate;
    }

    public UserAccount(Long id, String login, String password, UserAccountRole role, LocalDateTime createDate) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
        this.createDate = createDate;
    }

    public UserAccount(Long id, String login, String password, UserAccountRole role, LocalDateTime createDate,
                       LocalDateTime modifyDate, LocalDateTime deleteDate) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.deleteDate = deleteDate;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public UserAccountRole getRole() {
        return role;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public LocalDateTime getModifyDate() {
        return modifyDate;
    }

    public LocalDateTime getDeleteDate() {
        return deleteDate;
    }
}
