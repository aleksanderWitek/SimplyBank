package com.alex.service;

import com.alex.BankAccountType;
import com.alex.Currency;
import com.alex.dto.BankAccount;
import com.alex.dto.ClientProfile;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.IllegalStateRuntimeException;
import com.alex.exception.NullPointerRuntimeException;
import com.alex.repository.IBankAccountClientRepository;
import com.alex.repository.IBankAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock private IBankAccountRepository bankAccountRepository;
    @Mock private IBankAccountClientRepository bankAccountClientRepository;
    @Mock private IClientService clientService;
    @Mock private SecureRandom secureRandom;

    // save ----------------------------------------------------------------------------------------

    @Test
    void save_nullClientId_throwsNullPointerRuntimeException() {
        BankAccountService service = newService();

        assertThatThrownBy(() -> service.save(null, "CHECKING", "EUR"))
                .isInstanceOf(NullPointerRuntimeException.class)
                .hasMessage("Id is null");

        verifyNoInteractions(bankAccountRepository);
    }

    @Test
    void save_invalidType_throwsIllegalArgumentRuntimeException() {
        BankAccountService service = newService();

        assertThatThrownBy(() -> service.save(1L, "NONSENSE", "EUR"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessageContaining("Invalid bank account type");
    }

    @Test
    void save_invalidCurrency_throwsIllegalArgumentRuntimeException() {
        BankAccountService service = newService();

        assertThatThrownBy(() -> service.save(1L, "CHECKING", "XYZ"))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessageContaining("Invalid or not supported currency value");
    }

    @Test
    void save_validInput_persistsAndLinksAndReturnsBankAccount() {
        // Force the 12-digit number that the service will produce.
        when(secureRandom.nextInt(9)).thenReturn(0);   // +1 = 1
        when(secureRandom.nextInt(10)).thenReturn(0);  // 11 zeros
        when(bankAccountRepository.existsByNumber("100000000000")).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(42L);

        BankAccountService service = newService();
        BankAccount result = service.save(7L, "CHECKING", "EUR");

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getNumber()).isEqualTo("100000000000");
        assertThat(result.getAccountType()).isEqualTo(BankAccountType.CHECKING);
        assertThat(result.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(result.getBalance()).isEqualByComparingTo("0");

        ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getNumber()).isEqualTo("100000000000");
        verify(bankAccountClientRepository).linkBankAccountToClient(42L, 7L);
    }

    @Test
    void save_clientAtTenAccounts_throwsIllegalStateRuntimeException() {
        when(bankAccountClientRepository.countActiveBankAccountsByClientIdForUpdate(7L)).thenReturn(10);

        BankAccountService service = newService();

        assertThatThrownBy(() -> service.save(7L, "CHECKING", "EUR"))
                .isInstanceOf(IllegalStateRuntimeException.class)
                .hasMessage("Bank account limit reached (10 per client)");

        verify(bankAccountRepository, never()).save(any(BankAccount.class));
        verify(bankAccountClientRepository, never()).linkBankAccountToClient(anyLong(), anyLong());
    }

    // findById ------------------------------------------------------------------------------------

    @Test
    void findById_nullId_throwsNullPointerRuntimeException() {
        BankAccountService service = newService();
        assertThatThrownBy(() -> service.findById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findById_delegatesToRepository() {
        BankAccount account = new BankAccount(1L, "111", BankAccountType.CHECKING, Currency.EUR,
                BigDecimal.ONE, LocalDateTime.now());
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        BankAccountService service = newService();
        Optional<BankAccount> result = service.findById(1L);

        assertThat(result).contains(account);
    }

    // findByIdForUpdate ---------------------------------------------------------------------------

    @Test
    void findByIdForUpdate_nullId_throwsNullPointerRuntimeException() {
        BankAccountService service = newService();
        assertThatThrownBy(() -> service.findByIdForUpdate(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findByIdForUpdate_delegatesToRepository() {
        BankAccount account = new BankAccount(1L, "111", BankAccountType.CHECKING, Currency.EUR,
                BigDecimal.ONE, LocalDateTime.now());
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        BankAccountService service = newService();
        Optional<BankAccount> result = service.findByIdForUpdate(1L);

        assertThat(result).contains(account);
    }

    // addToBalance --------------------------------------------------------------------------------

    @Test
    void addToBalance_nullId_throwsNullPointerRuntimeException() {
        BankAccountService service = newService();
        assertThatThrownBy(() -> service.addToBalance(null, BigDecimal.TEN))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void addToBalance_delegatesToRepository() {
        BankAccountService service = newService();
        service.addToBalance(1L, new BigDecimal("5.00"));
        verify(bankAccountRepository).addToBalance(1L, new BigDecimal("5.00"));
    }

    // subtractFromBalance -------------------------------------------------------------------------

    @Test
    void subtractFromBalance_nullId_throwsNullPointerRuntimeException() {
        BankAccountService service = newService();
        assertThatThrownBy(() -> service.subtractFromBalance(null, BigDecimal.TEN))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void subtractFromBalance_delegatesToRepository() {
        BankAccountService service = newService();
        service.subtractFromBalance(1L, new BigDecimal("5.00"));
        verify(bankAccountRepository).subtractFromBalance(1L, new BigDecimal("5.00"));
    }

    // findAll -------------------------------------------------------------------------------------

    @Test
    void findAll_delegatesToRepository() {
        List<BankAccount> expected = List.of();
        when(bankAccountRepository.findAll()).thenReturn(expected);

        BankAccountService service = newService();
        assertThat(service.findAll()).isSameAs(expected);
    }

    // deleteById ----------------------------------------------------------------------------------

    @Test
    void deleteById_nullId_throwsNullPointerRuntimeException() {
        BankAccountService service = newService();
        assertThatThrownBy(() -> service.deleteById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void deleteById_unlinksAllClientsAndSoftDeletes() {
        when(bankAccountClientRepository.findClientsIdLinkedToBankAccountByBankAccountId(1L))
                .thenReturn(Arrays.asList(10L, 11L));

        BankAccountService service = newService();
        service.deleteById(1L);

        verify(bankAccountClientRepository).unlinkBankAccountToClient(1L, 10L);
        verify(bankAccountClientRepository).unlinkBankAccountToClient(1L, 11L);
        verify(bankAccountRepository).deleteById(1L);
    }

    @Test
    void deleteById_noLinkedClients_stillDeletesAccount() {
        when(bankAccountClientRepository.findClientsIdLinkedToBankAccountByBankAccountId(1L))
                .thenReturn(List.of());

        BankAccountService service = newService();
        service.deleteById(1L);

        verify(bankAccountClientRepository, never()).unlinkBankAccountToClient(eq(1L), anyLong());
        verify(bankAccountRepository).deleteById(1L);
    }

    private static long anyLong() {
        return org.mockito.ArgumentMatchers.anyLong();
    }

    // generateUniqueBankAccountNumber -------------------------------------------------------------

    @Test
    void generateUniqueBankAccountNumber_firstAttemptUnique_returnsIt() {
        when(secureRandom.nextInt(9)).thenReturn(0);
        when(secureRandom.nextInt(10)).thenReturn(0);
        when(bankAccountRepository.existsByNumber("100000000000")).thenReturn(false);

        BankAccountService service = newService();
        assertThat(service.generateUniqueBankAccountNumber()).isEqualTo("100000000000");
    }

    @Test
    void generateUniqueBankAccountNumber_collidesThenUnique_returnsSecondNumber() {
        when(secureRandom.nextInt(9)).thenReturn(0);
        when(secureRandom.nextInt(10)).thenReturn(0);
        when(bankAccountRepository.existsByNumber(anyString())).thenReturn(true, false);

        BankAccountService service = newService();
        String number = service.generateUniqueBankAccountNumber();

        assertThat(number).isEqualTo("100000000000");
        verify(bankAccountRepository, times(2)).existsByNumber(anyString());
    }

    @Test
    void generateUniqueBankAccountNumber_allCollide_throwsIllegalStateRuntimeException() {
        when(secureRandom.nextInt(9)).thenReturn(0);
        when(secureRandom.nextInt(10)).thenReturn(0);
        when(bankAccountRepository.existsByNumber(anyString())).thenReturn(true);

        BankAccountService service = newService();

        assertThatThrownBy(service::generateUniqueBankAccountNumber)
                .isInstanceOf(IllegalStateRuntimeException.class)
                .hasMessageContaining("Unable to generate unique bank account number after 100 attempts");
    }

    // findByNumber --------------------------------------------------------------------------------

    @Test
    void findByNumber_nullNumber_returnsEmptyWithoutHittingRepository() {
        BankAccountService service = newService();
        assertThat(service.findByNumber(null)).isEmpty();
        verifyNoInteractions(bankAccountRepository);
    }

    @Test
    void findByNumber_blankNumber_returnsEmptyWithoutHittingRepository() {
        BankAccountService service = newService();
        assertThat(service.findByNumber("   ")).isEmpty();
        verifyNoInteractions(bankAccountRepository);
    }

    @Test
    void findByNumber_presentNumber_delegatesToRepository() {
        BankAccount account = new BankAccount(1L, "100000000000", BankAccountType.CHECKING, Currency.EUR,
                BigDecimal.ONE, LocalDateTime.now());
        when(bankAccountRepository.findByNumber("100000000000")).thenReturn(Optional.of(account));

        BankAccountService service = newService();
        assertThat(service.findByNumber("100000000000")).contains(account);
    }

    @Test
    void findByNumber_unknownNumber_returnsEmpty() {
        when(bankAccountRepository.findByNumber("999")).thenReturn(Optional.empty());

        BankAccountService service = newService();
        assertThat(service.findByNumber("999")).isEmpty();
    }

    // findByClientId ------------------------------------------------------------------------------

    @Test
    void findByClientId_returnsOnlyResolvableAccountsAndSkipsMissing() {
        when(bankAccountClientRepository.findBankAccountsIdLinkedToClientByClientId(7L))
                .thenReturn(List.of(100L, 200L));
        BankAccount account = new BankAccount(100L, "100000000000", BankAccountType.CHECKING, Currency.EUR,
                BigDecimal.TEN, LocalDateTime.now());
        when(bankAccountRepository.findById(100L)).thenReturn(Optional.of(account));
        when(bankAccountRepository.findById(200L)).thenReturn(Optional.empty());

        BankAccountService service = newService();
        assertThat(service.findByClientId(7L)).containsExactly(account);
    }

    // findOwnersByBankAccountId -------------------------------------------------------------------

    @Test
    void findOwnersByBankAccountId_returnsOnlyResolvableProfilesAndSkipsMissing() {
        when(bankAccountClientRepository.findClientsIdLinkedToBankAccountByBankAccountId(5L))
                .thenReturn(List.of(10L, 20L));
        ClientProfile profile = new ClientProfile(10L, "A", "B", "C", "S", "1", "ID",
                LocalDateTime.now(), null, 1L, "alice", "CLIENT", LocalDateTime.now());
        when(clientService.findProfileById(10L)).thenReturn(Optional.of(profile));
        when(clientService.findProfileById(20L)).thenReturn(Optional.empty());

        BankAccountService service = newService();
        assertThat(service.findOwnersByBankAccountId(5L)).containsExactly(profile);
    }

    private BankAccountService newService() {
        return new BankAccountService(bankAccountRepository, bankAccountClientRepository,
                clientService, secureRandom);
    }
}
