package com.alex.service.validation;

import com.alex.Currency;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;

public class CurrencyValidation {

    public static void validateIfCurrencyIsCorrect(String currency, String message){
        if (currency == null || currency.isBlank()) {
            throw new NullPointerRuntimeException(message);
        }

        try {
            Currency.valueOf(currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentRuntimeException(message + ": " + currency);
        }
    }
}
