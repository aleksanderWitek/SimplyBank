package com.alex.dto;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @ManyToMany(mappedBy = "employee")
    private final List<UserAccount> userAccount = new ArrayList<>();

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

    public List<UserAccount> getUserAccount() {
        return userAccount;
    }

    public void addUserAccount(UserAccount userAccount) {
        this.userAccount.add(userAccount);
        userAccount.getEmployee().add(this);
    }

    public void removeUserAccount(UserAccount userAccount) {
        this.userAccount.remove(userAccount);
        userAccount.getEmployee().remove(this);
    }
}
