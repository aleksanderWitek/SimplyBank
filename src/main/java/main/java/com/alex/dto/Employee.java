package main.java.com.alex.dto;

import jakarta.persistence.*;

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

    @ManyToMany(mappedBy = "employee")
    private final List<UserAccount> userAccount = new ArrayList<>();

    public Employee() {
    }

    public Employee(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Employee(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
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
