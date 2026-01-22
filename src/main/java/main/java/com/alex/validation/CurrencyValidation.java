package main.java.com.alex.validation;

import main.java.com.alex.exception.NullPointerRuntimeException;

public class CurrencyValidation {

    public static void validateIfCurrencyIsCorrect(String currency, String message){
        if (currency == null || currency.isBlank()) {
            throw new NullPointerRuntimeException(message);
        }
    }
}
