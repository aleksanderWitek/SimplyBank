package main.java.com.alex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import main.java.com.alex.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transaction")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, length = 15)
    private String number;

    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    //todo how to add role from table transaction_bank_account check 2ndApp.txt or db script
    @ManyToMany(mappedBy = "transactions")
    private final List<BankAccount> bankAccounts = new ArrayList<>();

    public Transaction() {
    }

    public Transaction(String number, Currency currency, BigDecimal amount, LocalDateTime date) {
        this.number = number;
        this.currency = currency;
        this.amount = amount;
        this.date = date;
    }

    public Transaction(Long id, String number, Currency currency, BigDecimal amount, LocalDateTime date) {
        this.id = id;
        this.number = number;
        this.currency = currency;
        this.amount = amount;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public List<BankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public void addBankAccount(BankAccount bankAccount) {
        this.bankAccounts.add(bankAccount);
        bankAccount.getTransactions().add(this);
    }

    public void removeBankAccount(BankAccount bankAccount) {
        this.bankAccounts.remove(bankAccount);
        bankAccount.getTransactions().remove(this);
    }
}
