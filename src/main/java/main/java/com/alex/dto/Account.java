package main.java.com.alex.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "individual_number")
    private Integer individualNumber;

    @ManyToMany(mappedBy = "accounts")
    @JsonIgnore
    private final List<Client> clients = new ArrayList<>();

    public Account() {
    }

    public Account(Integer individualNumber) {
        this.individualNumber = individualNumber;
    }

    public Long getId() {
        return id;
    }

    public Integer getIndividualNumber() {
        return individualNumber;
    }

    public List<Client> getClients() {
        return clients;
    }
}
