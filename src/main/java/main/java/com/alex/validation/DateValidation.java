package main.java.com.alex.validation;

import main.java.com.alex.exception.NullPointerRuntimeException;

import java.sql.Timestamp;

public class DateValidation {

    public static void validateIfDateIsCorrect(Timestamp date, String message){
        if (date == null) {
            throw new NullPointerRuntimeException(message);
        }
    }
}
