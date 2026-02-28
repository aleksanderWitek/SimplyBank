package com.alex.dto;

import java.time.LocalDateTime;

public class Client {

    private Long id;

    private String firstName;

    private String lastName;

    private String city;

    private String street;

    private String houseNumber;

    private String identificationNumber;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private LocalDateTime deleteDate;

    public Client() {
    }

    public Client(String firstName, String lastName, String city, String street,
                  String houseNumber, String identificationNumber, LocalDateTime createDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
        this.identificationNumber = identificationNumber;
        this.createDate = createDate;
    }

    public Client(Long id, String firstName, String lastName, String city,
                  String street, String houseNumber, String identificationNumber, LocalDateTime createDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
        this.identificationNumber = identificationNumber;
        this.createDate = createDate;
    }

    public Client(Long id, String firstName, String lastName, String city, String street, String houseNumber,
                  String identificationNumber, LocalDateTime createDate, LocalDateTime modifyDate,
                  LocalDateTime deleteDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
        this.identificationNumber = identificationNumber;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.deleteDate = deleteDate;
    }

    public Long getId() {
        return id;
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
