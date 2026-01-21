package main.java.com.alex.dto;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

    @Column(name = "city", nullable = false, length = 30)
    private String city;

    @Column(name = "street", nullable = false, length = 30)
    private String street;

    @Column(name = "house_number", nullable = false, length = 10)
    private String houseNumber;

    @Column(name = "identification_number", nullable = false, length = 15)
    private String identificationNumber;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @ManyToMany(mappedBy = "client")
    private final List<UserAccount> userAccount = new ArrayList<>();

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

    public List<UserAccount> getUserAccount() {
        return userAccount;
    }

    public void addUserAccount(UserAccount userAccount) {
        this.userAccount.add(userAccount);
        userAccount.getClient().add(this);
    }

    public void removeUserAccount(UserAccount userAccount) {
        this.userAccount.remove(userAccount);
        userAccount.getClient().remove(this);
    }
}
