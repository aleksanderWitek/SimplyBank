package main.java.com.alex.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "order_details")
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item", nullable = false, length = 100)
    private String item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonBackReference
    private Client client;

    public OrderDetails() {
    }

    public OrderDetails(String item) {
        this.item = item;
    }

    public OrderDetails(String item, Client client) {
        this.item = item;
        this.client = client;
        this.client.getOrders().add(this);
    }

    public Long getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public Client getClient() {
        return client;
    }

    void setClient(Client client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "id=" + id +
                ", item='" + item + '\'' +
                '}';
    }
}
