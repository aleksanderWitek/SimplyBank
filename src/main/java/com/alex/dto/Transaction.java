package com.alex.dto;

import jakarta.persistence.*;
import com.alex.Currency;
import com.alex.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id_from")
    private BankAccount bankAccountFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id_to")
    private BankAccount bankAccountTo;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    public Transaction() {
    }

    public Transaction(TransactionType transactionType, Currency currency, BigDecimal amount,
                       BankAccount bankAccountFrom, BankAccount bankAccountTo, String description,
                       LocalDateTime createDate) {
        this.transactionType = transactionType;
        this.currency = currency;
        this.amount = amount;
        this.bankAccountFrom = bankAccountFrom;
        this.bankAccountTo = bankAccountTo;
        this.description = description;
        this.createDate = createDate;
    }

    public Transaction(Long id, TransactionType transactionType, Currency currency, BigDecimal amount,
                       BankAccount bankAccountFrom, BankAccount bankAccountTo, String description,
                       LocalDateTime createDate) {
        this.id = id;
        this.transactionType = transactionType;
        this.currency = currency;
        this.amount = amount;
        this.bankAccountFrom = bankAccountFrom;
        this.bankAccountTo = bankAccountTo;
        this.description = description;
        this.createDate = createDate;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BankAccount getBankAccountFrom() {
        return bankAccountFrom;
    }

    public BankAccount getBankAccountTo() {
        return bankAccountTo;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }
}
