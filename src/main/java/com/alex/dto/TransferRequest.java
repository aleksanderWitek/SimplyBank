package com.alex.dto;

import java.math.BigDecimal;

public class TransferRequest {

    private final Long bankAccountFromId;
    private final Long bankAccountToId;
    private final String bankAccountToNumber;
    private final BigDecimal amount;
    private final String currency;
    private final String description;

    public TransferRequest(Long bankAccountFromId, Long bankAccountToId, String bankAccountToNumber,
                           BigDecimal amount, String currency, String description) {
        this.bankAccountFromId = bankAccountFromId;
        this.bankAccountToId = bankAccountToId;
        this.bankAccountToNumber = bankAccountToNumber;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public Long getBankAccountFromId() {
        return bankAccountFromId;
    }

    public Long getBankAccountToId() {
        return bankAccountToId;
    }

    public String getBankAccountToNumber() {
        return bankAccountToNumber;
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
