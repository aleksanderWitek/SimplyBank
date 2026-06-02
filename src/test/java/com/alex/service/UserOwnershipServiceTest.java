package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.ClientProfile;
import com.alex.dto.UserAccount;
import com.alex.exception.AccessDeniedRuntimeException;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IBankAccountClientRepository;
import com.alex.repository.IUserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOwnershipServiceTest {

    @Mock private IUserAccountRepository userAccountRepository;
    @Mock private IClientService clientService;
    @Mock private IBankAccountClientRepository bankAccountClientRepository;

    @InjectMocks private UserOwnershipService service;

    private static UserAccount user(Long id, UserAccountRole role) {
        return new UserAccount(id, "u" + id, "p", role, LocalDateTime.now());
    }

    private static Principal principal(String name) {
        return () -> name;
    }

    // resolveCurrentUser --------------------------------------------------------------------------

    @Test
    void resolveCurrentUser_known_returnsAccount() {
        UserAccount account = user(1L, UserAccountRole.CLIENT);
        when(userAccountRepository.findByLogin("alice")).thenReturn(Optional.of(account));

        assertThat(service.resolveCurrentUser(principal("alice"))).isSameAs(account);
    }

    @Test
    void resolveCurrentUser_unknown_throwsUserAccountNotFoundRuntimeException() {
        when(userAccountRepository.findByLogin("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveCurrentUser(principal("missing")))
                .isInstanceOf(UserAccountNotFoundRuntimeException.class)
                .hasMessageContaining("User account not found for login: missing");
    }

    // isClient / isAdmin --------------------------------------------------------------------------

    @Test
    void isClient_trueForClientRole() {
        assertThat(service.isClient(user(1L, UserAccountRole.CLIENT))).isTrue();
    }

    @Test
    void isClient_falseForNonClientRole() {
        assertThat(service.isClient(user(1L, UserAccountRole.EMPLOYEE))).isFalse();
        assertThat(service.isClient(user(1L, UserAccountRole.ADMIN))).isFalse();
    }

    @Test
    void isAdmin_trueForAdminRole() {
        assertThat(service.isAdmin(user(1L, UserAccountRole.ADMIN))).isTrue();
    }

    @Test
    void isAdmin_falseForNonAdminRole() {
        assertThat(service.isAdmin(user(1L, UserAccountRole.CLIENT))).isFalse();
        assertThat(service.isAdmin(user(1L, UserAccountRole.EMPLOYEE))).isFalse();
    }

    // ensureClient --------------------------------------------------------------------------------

    @Test
    void ensureClient_clientRole_doesNotThrow() {
        assertThatCode(() -> service.ensureClient(user(1L, UserAccountRole.CLIENT), "transfer funds"))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureClient_nonClientRole_throwsAccessDeniedRuntimeException() {
        assertThatThrownBy(() -> service.ensureClient(user(1L, UserAccountRole.EMPLOYEE), "transfer funds"))
                .isInstanceOf(AccessDeniedRuntimeException.class)
                .hasMessage("Only clients can transfer funds");
    }

    // getOwnedBankAccountIds ----------------------------------------------------------------------

    @Test
    void getOwnedBankAccountIds_nonClient_returnsEmpty() {
        assertThat(service.getOwnedBankAccountIds(user(1L, UserAccountRole.EMPLOYEE))).isEmpty();
        assertThat(service.getOwnedBankAccountIds(user(1L, UserAccountRole.ADMIN))).isEmpty();
    }

    @Test
    void getOwnedBankAccountIds_clientWithoutProfile_returnsEmpty() {
        UserAccount account = user(1L, UserAccountRole.CLIENT);
        when(clientService.findProfileByUserAccountId(1L)).thenReturn(Optional.empty());

        assertThat(service.getOwnedBankAccountIds(account)).isEmpty();
    }

    @Test
    void getOwnedBankAccountIds_clientWithProfile_returnsOwnedIds() {
        UserAccount account = user(1L, UserAccountRole.CLIENT);
        ClientProfile profile = new ClientProfile(42L, "A", "B", "C", "S", "1", "ID",
                LocalDateTime.now(), null, 1L, "alice", "CLIENT", LocalDateTime.now());
        when(clientService.findProfileByUserAccountId(1L)).thenReturn(Optional.of(profile));
        when(bankAccountClientRepository.findBankAccountsIdLinkedToClientByClientId(42L))
                .thenReturn(List.of(100L, 200L));

        assertThat(service.getOwnedBankAccountIds(account)).containsExactlyInAnyOrder(100L, 200L);
    }

    // ownsBankAccount -----------------------------------------------------------------------------

    @Test
    void ownsBankAccount_nonClient_returnsFalse() {
        assertThat(service.ownsBankAccount(user(1L, UserAccountRole.ADMIN), 1L)).isFalse();
    }

    @Test
    void ownsBankAccount_clientOwns_returnsTrue() {
        UserAccount account = user(1L, UserAccountRole.CLIENT);
        ClientProfile profile = new ClientProfile(42L, "A", "B", "C", "S", "1", "ID",
                LocalDateTime.now(), null, 1L, "alice", "CLIENT", LocalDateTime.now());
        when(clientService.findProfileByUserAccountId(1L)).thenReturn(Optional.of(profile));
        when(bankAccountClientRepository.findBankAccountsIdLinkedToClientByClientId(42L))
                .thenReturn(List.of(100L, 200L));

        assertThat(service.ownsBankAccount(account, 100L)).isTrue();
    }

    @Test
    void ownsBankAccount_clientDoesNotOwn_returnsFalse() {
        UserAccount account = user(1L, UserAccountRole.CLIENT);
        ClientProfile profile = new ClientProfile(42L, "A", "B", "C", "S", "1", "ID",
                LocalDateTime.now(), null, 1L, "alice", "CLIENT", LocalDateTime.now());
        when(clientService.findProfileByUserAccountId(1L)).thenReturn(Optional.of(profile));
        when(bankAccountClientRepository.findBankAccountsIdLinkedToClientByClientId(42L))
                .thenReturn(List.of(100L));

        assertThat(service.ownsBankAccount(account, 999L)).isFalse();
    }
}
