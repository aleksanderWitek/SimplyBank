package main.java.com.alex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import main.java.com.alex.BankAccountType;
import main.java.com.alex.Currency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_account")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, length = 15)
    private String number;

    @Column(name = "account_type", nullable = false, length = 15)
    private BankAccountType accountType;

    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "transaction_bank_account",
            joinColumns = @JoinColumn(name = "bank_account_id"),
            inverseJoinColumns = @JoinColumn(name = "transaction_id")
    )
    @JsonIgnoreProperties({"bankAccounts", "hibernateLazyInitializer", "handler"})
    private final List<Transaction> transactions = new ArrayList<>();

    public BankAccount() {
    }

    public BankAccount(String number, BankAccountType accountType, Currency currency, BigDecimal balance) {
        this.number = number;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = balance;
    }

    public BankAccount(Long id, String number, BankAccountType accountType, Currency currency, BigDecimal balance) {
        this.id = id;
        this.number = number;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public BankAccountType getAccountType() {
        return accountType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        transaction.getBankAccounts().add(this);
    }

    public void removeTransaction(Transaction transaction) {
        this.transactions.remove(transaction);
        transaction.getBankAccounts().remove(this);
    }
}
