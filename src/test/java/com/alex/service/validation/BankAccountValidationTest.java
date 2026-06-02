package com.alex.service.validation;

import com.alex.BankAccountType;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankAccountValidationTest {

    @Test
    void validateIfBankAccountTypeIsCorrect_nullType_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> BankAccountValidation.validateIfBankAccountTypeIsCorrect(null, "Invalid type"))
                .isInstanceOf(NullPointerRuntimeException.class)
                .hasMessage("Invalid type");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void validateIfBankAccountTypeIsCorrect_blankType_throwsNullPointerRuntimeException(String blank) {
        assertThatThrownBy(() -> BankAccountValidation.validateIfBankAccountTypeIsCorrect(blank, "Invalid type"))
                .isInstanceOf(NullPointerRuntimeException.class)
                .hasMessage("Invalid type");
    }

    @Test
    void validateIfBankAccountTypeIsCorrect_invalidType_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> BankAccountValidation.validateIfBankAccountTypeIsCorrect("NOT_A_TYPE", "Invalid type"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Invalid type: NOT_A_TYPE");
    }

    @ParameterizedTest
    @EnumSource(BankAccountType.class)
    void validateIfBankAccountTypeIsCorrect_validType_doesNotThrow(BankAccountType type) {
        assertThatCode(() ->
                BankAccountValidation.validateIfBankAccountTypeIsCorrect(type.name(), "Invalid type"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateIfBankAccountTypeIsCorrect_lowerCaseValidType_doesNotThrow() {
        assertThatCode(() ->
                BankAccountValidation.validateIfBankAccountTypeIsCorrect("checking", "Invalid type"))
                .doesNotThrowAnyException();
    }

    @Test
    void constructor_canBeInstantiated_forCoverage() {
        assertThatCode(BankAccountValidation::new).doesNotThrowAnyException();
    }
}
