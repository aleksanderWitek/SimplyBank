package com.alex.service.validation;

import com.alex.BankAccountType;
import com.alex.Currency;
import com.alex.dto.BankAccount;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.IllegalStateRuntimeException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionValidationTest {

    // validateDescriptionLength -----------------------------------------------------------------

    @Test
    void validateDescriptionLength_nullDescription_doesNotThrow() {
        assertThatCode(() -> TransactionValidation.validateDescriptionLength(null))
                .doesNotThrowAnyException();
    }

    @Test
    void validateDescriptionLength_shortDescription_doesNotThrow() {
        assertThatCode(() -> TransactionValidation.validateDescriptionLength("ok"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateDescriptionLength_exactly255Chars_doesNotThrow() {
        String exactly255 = "a".repeat(255);
        assertThatCode(() -> TransactionValidation.validateDescriptionLength(exactly255))
                .doesNotThrowAnyException();
    }

    @Test
    void validateDescriptionLength_over255Chars_throwsIllegalArgumentRuntimeException() {
        String over = "a".repeat(256);
        assertThatThrownBy(() -> TransactionValidation.validateDescriptionLength(over))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Description must not exceed 255 characters");
    }

    // validateIfBankAccountsAreTheSameForTransaction ---------------------------------------------

    @Test
    void validateIfBankAccountsAreTheSameForTransaction_sameAccount_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() ->
                TransactionValidation.validateIfBankAccountsAreTheSameForTransaction(1L, 1L))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Transaction cannot have same from and to accounts");
    }

    @Test
    void validateIfBankAccountsAreTheSameForTransaction_differentAccounts_doesNotThrow() {
        assertThatCode(() ->
                TransactionValidation.validateIfBankAccountsAreTheSameForTransaction(1L, 2L))
                .doesNotThrowAnyException();
    }

    // validateAmount ------------------------------------------------------------------------------

    @Test
    void validateAmount_nullAmount_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> TransactionValidation.validateAmount(null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    void validateAmount_zeroAmount_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> TransactionValidation.validateAmount(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    void validateAmount_negativeAmount_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> TransactionValidation.validateAmount(new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    void validateAmount_positiveAmount_doesNotThrow() {
        assertThatCode(() -> TransactionValidation.validateAmount(new BigDecimal("10.00")))
                .doesNotThrowAnyException();
    }

    // validateSufficientBalance ------------------------------------------------------------------

    @Test
    void validateSufficientBalance_insufficient_throwsIllegalStateRuntimeException() {
        BankAccount account = account("123456789012", new BigDecimal("5.00"), Currency.EUR);

        assertThatThrownBy(() ->
                TransactionValidation.validateSufficientBalance(account, new BigDecimal("10.00")))
                .isInstanceOf(IllegalStateRuntimeException.class)
                .hasMessageContaining("Insufficient balance")
                // The account number must not leak into the error message (finding #5).
                .hasMessageNotContaining("123456789012");
    }

    @Test
    void validateSufficientBalance_exactBalance_doesNotThrow() {
        BankAccount account = account("123456789012", new BigDecimal("10.00"), Currency.EUR);

        assertThatCode(() ->
                TransactionValidation.validateSufficientBalance(account, new BigDecimal("10.00")))
                .doesNotThrowAnyException();
    }

    @Test
    void validateSufficientBalance_sufficient_doesNotThrow() {
        BankAccount account = account("123456789012", new BigDecimal("50.00"), Currency.EUR);

        assertThatCode(() ->
                TransactionValidation.validateSufficientBalance(account, new BigDecimal("10.00")))
                .doesNotThrowAnyException();
    }

    // validateCurrencyMatch -----------------------------------------------------------------------

    @Test
    void validateCurrencyMatch_nullCurrencyParam_doesNotThrow() {
        BankAccount account = account("n", BigDecimal.TEN, Currency.EUR);
        assertThatCode(() -> TransactionValidation.validateCurrencyMatch(account, null))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCurrencyMatch_nullAccountCurrency_doesNotThrow() {
        BankAccount account = account("n", BigDecimal.TEN, null);
        assertThatCode(() -> TransactionValidation.validateCurrencyMatch(account, "EUR"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCurrencyMatch_matchingCurrency_doesNotThrow() {
        BankAccount account = account("n", BigDecimal.TEN, Currency.EUR);
        assertThatCode(() -> TransactionValidation.validateCurrencyMatch(account, "eur"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCurrencyMatch_mismatchingCurrency_throwsIllegalArgumentRuntimeException() {
        BankAccount account = account("n", BigDecimal.TEN, Currency.EUR);
        assertThatThrownBy(() -> TransactionValidation.validateCurrencyMatch(account, "USD"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessageContaining("Currency mismatch: transaction currency USD")
                .hasMessageContaining("does not match account currency EUR");
    }

    @Test
    void constructor_canBeInstantiated_forCoverage() {
        assertThatCode(TransactionValidation::new).doesNotThrowAnyException();
    }

    private static BankAccount account(String number, BigDecimal balance, Currency currency) {
        return new BankAccount(1L, number, BankAccountType.CHECKING, currency, balance, LocalDateTime.now());
    }
}
