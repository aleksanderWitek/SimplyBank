package main.java.com.alex.service.validation;

import main.java.com.alex.Currency;
import main.java.com.alex.exception.IllegalArgumentRuntimeException;
import main.java.com.alex.exception.NullPointerRuntimeException;
import main.java.com.alex.exception.SQLRuntimeException;

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
