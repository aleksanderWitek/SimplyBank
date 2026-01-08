package main.java.com.alex.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "client_details_id")
    private ClientDetails clientDetails;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @JsonManagedReference
    private final List<OrderDetails> orders = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "client_account",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private final List<Account> accounts = new ArrayList<>();

    public Client() {
    }

    public Client(String name, ClientDetails clientDetails) {
        this.name = name;
        this.clientDetails = clientDetails;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ClientDetails getClientDetails() {
        return clientDetails;
    }

    public List<OrderDetails> getOrders() {
        return orders;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", clientDetails=" + clientDetails +
                '}';
    }

    public void addOrder(OrderDetails orderDetails) {
        this.orders.add(orderDetails);
        orderDetails.setClient(this);
    }

    public void removeOrder(OrderDetails orderDetails) {
        this.orders.remove(orderDetails);
        orderDetails.setClient(null);
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
        account.getClients().add(this);
    }

    public void removeAccount(Account account) {
        this.accounts.remove(account);
        account.getClients().remove(this);
    }
}
