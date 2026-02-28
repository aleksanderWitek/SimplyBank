package com.alex.dto;

import java.time.LocalDateTime;

public class Employee {

    private Long id;

    private String firstName;

    private String lastName;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private LocalDateTime deleteDate;

    public Employee() {
    }

    public Employee(String firstName, String lastName, LocalDateTime createDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.createDate = createDate;
    }

    public Employee(Long id, String firstName, String lastName, LocalDateTime createDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createDate = createDate;
    }

    public Employee(Long id, String firstName, String lastName, LocalDateTime createDate, LocalDateTime modifyDate, LocalDateTime deleteDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
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
