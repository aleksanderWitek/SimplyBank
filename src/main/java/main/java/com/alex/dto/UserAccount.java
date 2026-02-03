package main.java.com.alex.dto;

import jakarta.persistence.*;
import main.java.com.alex.UserAccountRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false, length = 12)
    private String login;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "role", nullable = false)
    private UserAccountRole role;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_account_client",
            joinColumns = @JoinColumn(name = "user_account_id"),
            inverseJoinColumns = @JoinColumn(name = "client_id")
    )
    private final List<Client> client = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_account_employee",
            joinColumns = @JoinColumn(name = "user_account_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private final List<Employee> employee = new ArrayList<>();

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

    public List<Client> getClient() {
        return client;
    }

    public List<Employee> getEmployee() {
        return employee;
    }

    public void addClient(Client client) {
        this.client.add(client);
        client.getUserAccount().add(this);
    }

    public void removeClient(Client client) {
        this.client.remove(client);
        client.getUserAccount().remove(this);
    }

    public void addEmployee(Employee employee) {
        this.employee.add(employee);
        employee.getUserAccount().add(this);
    }

    public void removeEmployee(Employee employee) {
        this.employee.remove(employee);
        employee.getUserAccount().remove(this);
    }
}
