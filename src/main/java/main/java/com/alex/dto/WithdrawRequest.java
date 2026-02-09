package main.java.com.alex.dto;

import java.math.BigDecimal;

public class WithdrawRequest {

    private final Long bankAccountFromId;
    private final BigDecimal amount;
    private final String currency;
    private final String description;

    public WithdrawRequest(Long bankAccountFromId, BigDecimal amount, String currency, String description) {
        this.bankAccountFromId = bankAccountFromId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public Long getBankAccountFromId() {
        return bankAccountFromId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }
}
