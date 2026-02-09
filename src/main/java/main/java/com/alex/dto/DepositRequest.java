package main.java.com.alex.dto;

import java.math.BigDecimal;

public class DepositRequest {

    private final Long bankAccountToId;
    private final BigDecimal amount;
    private final String currency;
    private final String description;

    public DepositRequest(Long bankAccountToId, BigDecimal amount, String currency, String description) {
        this.bankAccountToId = bankAccountToId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public Long getBankAccountToId() {
        return bankAccountToId;
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
