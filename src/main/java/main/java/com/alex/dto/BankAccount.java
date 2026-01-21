package main.java.com.alex.dto;

import jakarta.persistence.*;
import main.java.com.alex.BankAccountType;
import main.java.com.alex.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_account")
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

    @Column(name = "balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal balance;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @OneToMany(mappedBy = "bankAccountFrom", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Transaction> transactionsFrom = new ArrayList<>();

    @OneToMany(mappedBy = "bankAccountTo", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Transaction> transactionsTo = new ArrayList<>();

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
