package main.java.com.alex.dto;

import jakarta.persistence.*;
import main.java.com.alex.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id_from", nullable = false)
    private BankAccount bankAccountFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id_to", nullable = false)
    private BankAccount bankAccountTo;

    public Transaction() {
    }

    public Transaction(String number, Currency currency, BigDecimal amount, LocalDateTime date, BankAccount bankAccountFrom, BankAccount bankAccountTo) {
        this.number = number;
        this.currency = currency;
        this.amount = amount;
        this.date = date;
        this.bankAccountFrom = bankAccountFrom;
        this.bankAccountTo = bankAccountTo;
    }

    public Transaction(Long id, String number, Currency currency, BigDecimal amount, LocalDateTime date, BankAccount bankAccountFrom, BankAccount bankAccountTo) {
        this.id = id;
        this.number = number;
        this.currency = currency;
        this.amount = amount;
        this.date = date;
        this.bankAccountFrom = bankAccountFrom;
        this.bankAccountTo = bankAccountTo;
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

    public BankAccount getBankAccountFrom() {
        return bankAccountFrom;
    }

    public BankAccount getBankAccountTo() {
        return bankAccountTo;
    }
}
