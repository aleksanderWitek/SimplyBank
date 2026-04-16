package com.alex.service;

import com.alex.BankAccountType;
import com.alex.Currency;
import com.alex.TransactionType;
import com.alex.dto.BankAccount;
import com.alex.dto.Transaction;
import com.alex.exception.BankAccountNotFoundRuntimeException;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.IllegalStateRuntimeException;
import com.alex.exception.NullPointerRuntimeException;
import com.alex.repository.ITransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private ITransactionRepository transactionRepository;
    @Mock private IBankAccountService bankAccountService;

    @InjectMocks private TransactionService service;

    private static BankAccount account(Long id, BigDecimal balance, Currency currency) {
        return new BankAccount(id, "num-" + id, BankAccountType.CHECKING, currency,
                balance, LocalDateTime.now());
    }

    // transfer ------------------------------------------------------------------------------------

    @Test
    void transfer_nullFromId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.transfer(null, 2L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void transfer_nullToId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.transfer(1L, null, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void transfer_sameFromAndTo_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.transfer(1L, 1L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Transaction cannot have same from and to accounts");
    }

    @Test
    void transfer_invalidCurrency_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.TEN, "XYZ", "d"))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void transfer_zeroAmount_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.ZERO, "EUR", "d"))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void transfer_descriptionTooLong_throwsIllegalArgumentRuntimeException() {
        String desc = "a".repeat(256);
        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.TEN, "EUR", desc))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void transfer_lockFirstAccountNotFound_throwsBankAccountNotFoundRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(BankAccountNotFoundRuntimeException.class)
                .hasMessageContaining("Bank account not found with id: 1");
    }

    @Test
    void transfer_lockSecondAccountNotFound_throwsBankAccountNotFoundRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.of(account(1L, BigDecimal.TEN, Currency.EUR)));
        when(bankAccountService.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(BankAccountNotFoundRuntimeException.class)
                .hasMessageContaining("Bank account not found with id: 2");
    }

    @Test
    void transfer_rereadFromNotFound_throwsBankAccountNotFoundRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.of(account(1L, BigDecimal.TEN, Currency.EUR)));
        when(bankAccountService.findByIdForUpdate(2L)).thenReturn(Optional.of(account(2L, BigDecimal.ONE, Currency.EUR)));
        when(bankAccountService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(BankAccountNotFoundRuntimeException.class)
                .hasMessageContaining("(from) not found with id: 1");
    }

    @Test
    void transfer_rereadToNotFound_throwsBankAccountNotFoundRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.of(account(1L, BigDecimal.TEN, Currency.EUR)));
        when(bankAccountService.findByIdForUpdate(2L)).thenReturn(Optional.of(account(2L, BigDecimal.ONE, Currency.EUR)));
        when(bankAccountService.findById(1L)).thenReturn(Optional.of(account(1L, BigDecimal.TEN, Currency.EUR)));
        when(bankAccountService.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(BankAccountNotFoundRuntimeException.class)
                .hasMessageContaining("(to) not found with id: 2");
    }

    @Test
    void transfer_currencyMismatch_throwsIllegalArgumentRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.of(account(1L, BigDecimal.TEN, Currency.USD)));
        when(bankAccountService.findByIdForUpdate(2L)).thenReturn(Optional.of(account(2L, BigDecimal.ONE, Currency.EUR)));
        when(bankAccountService.findById(1L)).thenReturn(Optional.of(account(1L, BigDecimal.TEN, Currency.USD)));
        when(bankAccountService.findById(2L)).thenReturn(Optional.of(account(2L, BigDecimal.ONE, Currency.EUR)));

        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    void transfer_insufficientBalance_throwsIllegalStateRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.of(account(1L, BigDecimal.ONE, Currency.EUR)));
        when(bankAccountService.findByIdForUpdate(2L)).thenReturn(Optional.of(account(2L, BigDecimal.ONE, Currency.EUR)));
        when(bankAccountService.findById(1L)).thenReturn(Optional.of(account(1L, BigDecimal.ONE, Currency.EUR)));
        when(bankAccountService.findById(2L)).thenReturn(Optional.of(account(2L, BigDecimal.ONE, Currency.EUR)));

        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(IllegalStateRuntimeException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    void transfer_happyPath_locksLowerIdFirstThenUpdatesBalancesAndSavesTransaction() {
        // fromId=2, toId=1 → lower-ID-first means the lock order should still be 1 then 2.
        BankAccount from = account(2L, new BigDecimal("100.00"), Currency.EUR);
        BankAccount to = account(1L, new BigDecimal("5.00"), Currency.EUR);
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.of(to));
        when(bankAccountService.findByIdForUpdate(2L)).thenReturn(Optional.of(from));
        when(bankAccountService.findById(2L)).thenReturn(Optional.of(from));
        when(bankAccountService.findById(1L)).thenReturn(Optional.of(to));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(99L);

        Transaction result = service.transfer(2L, 1L, new BigDecimal("30.00"), "EUR", "Rent");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(result.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(result.getAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getDescription()).isEqualTo("Rent");

        InOrder order = inOrder(bankAccountService);
        order.verify(bankAccountService).findByIdForUpdate(1L);
        order.verify(bankAccountService).findByIdForUpdate(2L);
        order.verify(bankAccountService).findById(2L);
        order.verify(bankAccountService).findById(1L);
        order.verify(bankAccountService).subtractFromBalance(2L, new BigDecimal("30.00"));
        order.verify(bankAccountService).addToBalance(1L, new BigDecimal("30.00"));
    }

    // deposit -------------------------------------------------------------------------------------

    @Test
    void deposit_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.deposit(null, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void deposit_invalidCurrency_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.deposit(1L, BigDecimal.TEN, "XYZ", "d"))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void deposit_accountNotFound_throwsBankAccountNotFoundRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deposit(1L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(BankAccountNotFoundRuntimeException.class);
    }

    @Test
    void deposit_currencyMismatch_throwsIllegalArgumentRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L))
                .thenReturn(Optional.of(account(1L, BigDecimal.TEN, Currency.USD)));

        assertThatThrownBy(() -> service.deposit(1L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    void deposit_happyPath_addsToBalanceAndSavesTransaction() {
        BankAccount to = account(1L, new BigDecimal("50.00"), Currency.EUR);
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.of(to));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(5L);

        Transaction result = service.deposit(1L, new BigDecimal("20.00"), "EUR", null);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.getBankAccountFrom()).isNull();
        assertThat(result.getBankAccountTo()).isSameAs(to);

        verify(bankAccountService).addToBalance(1L, new BigDecimal("20.00"));
        verify(bankAccountService, never()).subtractFromBalance(any(), any());
    }

    // withdraw ------------------------------------------------------------------------------------

    @Test
    void withdraw_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.withdraw(null, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void withdraw_accountNotFound_throwsBankAccountNotFoundRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.withdraw(1L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(BankAccountNotFoundRuntimeException.class);
    }

    @Test
    void withdraw_insufficientBalance_throwsIllegalStateRuntimeException() {
        when(bankAccountService.findByIdForUpdate(1L))
                .thenReturn(Optional.of(account(1L, BigDecimal.ONE, Currency.EUR)));
        assertThatThrownBy(() -> service.withdraw(1L, BigDecimal.TEN, "EUR", "d"))
                .isInstanceOf(IllegalStateRuntimeException.class);
    }

    @Test
    void withdraw_happyPath_subtractsAndSavesTransaction() {
        BankAccount from = account(1L, new BigDecimal("50.00"), Currency.EUR);
        when(bankAccountService.findByIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(7L);

        Transaction result = service.withdraw(1L, new BigDecimal("20.00"), "EUR", "ATM");

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(result.getBankAccountFrom()).isSameAs(from);
        assertThat(result.getBankAccountTo()).isNull();

        verify(bankAccountService).subtractFromBalance(1L, new BigDecimal("20.00"));
        verify(bankAccountService, never()).addToBalance(any(), any());
    }

    // finders -------------------------------------------------------------------------------------

    @Test
    void findById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findById_delegates() {
        Transaction tx = new Transaction(1L, TransactionType.DEPOSIT, Currency.EUR, BigDecimal.ONE,
                null, null, null, LocalDateTime.now());
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        assertThat(service.findById(1L)).contains(tx);
    }

    @Test
    void findAll_delegates() {
        List<Transaction> transactions = List.of();
        when(transactionRepository.findAll()).thenReturn(transactions);
        assertThat(service.findAll()).isSameAs(transactions);
    }

    @Test
    void findTransactionsByBankAccountFromId_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findTransactionsByBankAccountFromId(null))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findTransactionsByBankAccountFromId_delegates() {
        List<Transaction> transactions = List.of();
        when(transactionRepository.findTransactionsByBankAccountFromId(1L)).thenReturn(transactions);
        assertThat(service.findTransactionsByBankAccountFromId(1L)).isSameAs(transactions);
    }

    @Test
    void findTransactionsByBankAccountToId_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findTransactionsByBankAccountToId(null))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findTransactionsByBankAccountToId_delegates() {
        List<Transaction> transactions = List.of();
        when(transactionRepository.findTransactionsByBankAccountToId(1L)).thenReturn(transactions);
        assertThat(service.findTransactionsByBankAccountToId(1L)).isSameAs(transactions);
    }

    @Test
    void findTransactionsBetweenBankAccounts_nullFrom_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findTransactionsBetweenBankAccounts(null, 2L))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findTransactionsBetweenBankAccounts_nullTo_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findTransactionsBetweenBankAccounts(1L, null))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findTransactionsBetweenBankAccounts_delegates() {
        List<Transaction> transactions = List.of();
        when(transactionRepository.findTransactionsBetweenBankAccounts(1L, 2L)).thenReturn(transactions);
        assertThat(service.findTransactionsBetweenBankAccounts(1L, 2L)).isSameAs(transactions);
    }
}
