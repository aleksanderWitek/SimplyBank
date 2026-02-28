package com.alex.dto;

import com.alex.BankAccountType;
import com.alex.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccount {

    private Long id;

    private String number;

    private BankAccountType accountType;

    private Currency currency;

    private BigDecimal balance;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private LocalDateTime deleteDate;

    public BankAccount() {
    }

    public BankAccount(String number, BankAccountType accountType, Currency currency, BigDecimal balance, LocalDateTime createDate) {
        this.number = number;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = balance;
        this.createDate = createDate;
    }

    public BankAccount(Long id, String number, BankAccountType accountType, Currency currency, BigDecimal balance,
                       LocalDateTime createDate) {
        this.id = id;
        this.number = number;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = balance;
        this.createDate = createDate;
    }

    public BankAccount(Long id, String number, BankAccountType accountType, Currency currency, BigDecimal balance,
                       LocalDateTime createDate, LocalDateTime modifyDate, LocalDateTime deleteDate) {
        this.id = id;
        this.number = number;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = balance;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.deleteDate = deleteDate;
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

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public LocalDateTime getModifyDate() {
        return modifyDate;
    }

    public LocalDateTime getDeleteDate() {
        return deleteDate;
    }
}
