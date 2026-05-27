package com.alex.dto;

import com.alex.Currency;

public class AccountCurrencyResponse {

    private Currency currency;

    public AccountCurrencyResponse() {
    }

    public AccountCurrencyResponse(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
