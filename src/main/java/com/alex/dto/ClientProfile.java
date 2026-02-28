package com.alex.dto;

import java.time.LocalDateTime;

public class ClientProfile {

    private Long clientId;
    private String firstName;
    private String lastName;
    private String city;
    private String street;
    private String houseNumber;
    private String identificationNumber;
    private LocalDateTime clientCreateDate;
    private LocalDateTime clientModifyDate;
    private Long userAccountId;
    private String login;
    private String role;
    private LocalDateTime accountCreateDate;

    public ClientProfile(Long clientId, String firstName, String lastName, String city, String street,
                         String houseNumber, String identificationNumber, LocalDateTime clientCreateDate,
                         LocalDateTime clientModifyDate, Long userAccountId, String login, String role,
                         LocalDateTime accountCreateDate) {
        this.clientId = clientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
        this.identificationNumber = identificationNumber;
        this.clientCreateDate = clientCreateDate;
        this.clientModifyDate = clientModifyDate;
        this.userAccountId = userAccountId;
        this.login = login;
        this.role = role;
        this.accountCreateDate = accountCreateDate;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public LocalDateTime getClientCreateDate() {
        return clientCreateDate;
    }

    public LocalDateTime getClientModifyDate() {
        return clientModifyDate;
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
