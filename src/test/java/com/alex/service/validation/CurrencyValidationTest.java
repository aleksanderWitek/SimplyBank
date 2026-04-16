package com.alex.service.validation;

import com.alex.Currency;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyValidationTest {

    @Test
    void validateIfCurrencyIsCorrect_nullCurrency_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> CurrencyValidation.validateIfCurrencyIsCorrect(null, "Invalid currency"))
                .isInstanceOf(NullPointerRuntimeException.class)
                .hasMessage("Invalid currency");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void validateIfCurrencyIsCorrect_blankCurrency_throwsNullPointerRuntimeException(String blank) {
        assertThatThrownBy(() -> CurrencyValidation.validateIfCurrencyIsCorrect(blank, "Invalid currency"))
                .isInstanceOf(NullPointerRuntimeException.class)
                .hasMessage("Invalid currency");
    }

    @Test
    void validateIfCurrencyIsCorrect_invalidCurrency_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> CurrencyValidation.validateIfCurrencyIsCorrect("XYZ", "Invalid currency"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Invalid currency: XYZ");
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void validateIfCurrencyIsCorrect_validCurrency_doesNotThrow(Currency currency) {
        assertThatCode(() ->
                CurrencyValidation.validateIfCurrencyIsCorrect(currency.name(), "Invalid currency"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateIfCurrencyIsCorrect_lowerCaseValidCurrency_doesNotThrow() {
        assertThatCode(() ->
                CurrencyValidation.validateIfCurrencyIsCorrect("eur", "Invalid currency"))
                .doesNotThrowAnyException();
    }

    @Test
    void constructor_canBeInstantiated_forCoverage() {
        assertThatCode(CurrencyValidation::new).doesNotThrowAnyException();
    }
}
