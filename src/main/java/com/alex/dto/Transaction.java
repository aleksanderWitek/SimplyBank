package com.alex.dto;

import com.alex.Currency;
import com.alex.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private Long id;

    private TransactionType transactionType;

    private Currency currency;

    private BigDecimal amount;

    private BankAccount bankAccountFrom;

    private BankAccount bankAccountTo;

    private String description;

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
