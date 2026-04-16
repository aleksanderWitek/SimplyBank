package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.Client;
import com.alex.dto.ClientProfile;
import com.alex.dto.UserAccount;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IBankAccountClientRepository;
import com.alex.repository.IClientRepository;
import com.alex.repository.IUserAccountClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private IClientRepository clientRepository;
    @Mock private IUserAccountClientRepository userAccountClientRepository;
    @Mock private IBankAccountClientRepository bankAccountClientRepository;
    @Mock private IUserAccountService userAccountService;

    @InjectMocks private ClientService service;

    private static Client newClient(String first, String last) {
        return new Client(first, last, "Warsaw", "Main", "1", "ID123", null);
    }

    private static UserAccount newUser(Long id) {
        return new UserAccount(id, "alismi", "pwd", UserAccountRole.CLIENT, LocalDateTime.now());
    }

    // save ----------------------------------------------------------------------------------------

    @Test
    void save_nullClient_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.save(null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Client is null");
    }

    @Test
    void save_nullFirstName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.save(newClient(null, "Smith")))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("First Name is null or empty");
    }

    @Test
    void save_nullLastName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.save(newClient("Alice", null)))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Last Name is null or empty");
    }

    @Test
    void save_valid_persistsClientCreatesUserAndLinks() {
        when(clientRepository.save(any(Client.class))).thenReturn(42L);
        when(userAccountService.save("Alice", "Smith", UserAccountRole.CLIENT)).thenReturn(newUser(99L));

        Client result = service.save(newClient("Alice", "Smith"));

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getLastName()).isEqualTo("Smith");

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertThat(captor.getValue().getCreateDate()).isNotNull();

        verify(userAccountService).save("Alice", "Smith", UserAccountRole.CLIENT);
        verify(userAccountClientRepository).linkUserAccountToClient(99L, 42L);
    }

    // updateById ----------------------------------------------------------------------------------

    @Test
    void updateById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.updateById(null, newClient("A", "B")))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void updateById_nullClient_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.updateById(1L, null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Client is null");
    }

    @Test
    void updateById_nullFirstName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.updateById(1L, newClient(null, "Smith")))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void updateById_nullLastName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.updateById(1L, newClient("Alice", null)))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void updateById_valid_delegatesToRepository() {
        Client client = newClient("Alice", "Smith");
        service.updateById(1L, client);
        verify(clientRepository).updateById(1L, client);
    }

    // findById ------------------------------------------------------------------------------------

    @Test
    void findById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findById_delegatesToRepository() {
        Client client = new Client(1L, "A", "B", "C", "S", "1", "ID", LocalDateTime.now());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThat(service.findById(1L)).contains(client);
    }

    // findAll -------------------------------------------------------------------------------------

    @Test
    void findAll_delegatesToRepository() {
        List<Client> clients = List.of();
        when(clientRepository.findAll()).thenReturn(clients);
        assertThat(service.findAll()).isSameAs(clients);
    }

    // deleteById ----------------------------------------------------------------------------------

    @Test
    void deleteById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.deleteById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void deleteById_noLinkedUserAccount_throwsUserAccountNotFoundRuntimeException() {
        when(userAccountClientRepository.findUserAccountIdByClientId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById(1L))
                .isInstanceOf(UserAccountNotFoundRuntimeException.class)
                .hasMessageContaining("There is no User Account linked to Client with id:1");

        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    void deleteById_happyPath_unlinksBankAccountsUnlinksUserAccountDeletesUserAndClient() {
        when(userAccountClientRepository.findUserAccountIdByClientId(1L)).thenReturn(Optional.of(99L));
        when(bankAccountClientRepository.findBankAccountsIdLinkedToClientByClientId(1L))
                .thenReturn(List.of(10L, 11L));

        service.deleteById(1L);

        verify(bankAccountClientRepository).unlinkBankAccountToClient(10L, 1L);
        verify(bankAccountClientRepository).unlinkBankAccountToClient(11L, 1L);
        verify(userAccountClientRepository).unlinkUserAccountFromClient(99L, 1L);
        verify(userAccountService).deleteById(99L);
        verify(clientRepository).deleteById(1L);
    }

    @Test
    void deleteById_noBankAccounts_stillUnlinksAndDeletes() {
        when(userAccountClientRepository.findUserAccountIdByClientId(1L)).thenReturn(Optional.of(99L));
        when(bankAccountClientRepository.findBankAccountsIdLinkedToClientByClientId(1L)).thenReturn(List.of());

        service.deleteById(1L);

        verify(userAccountClientRepository).unlinkUserAccountFromClient(99L, 1L);
        verify(userAccountService).deleteById(99L);
        verify(clientRepository).deleteById(1L);
    }

    // findProfileByUserAccountId ------------------------------------------------------------------

    @Test
    void findProfileByUserAccountId_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findProfileByUserAccountId(null))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findProfileByUserAccountId_delegatesToRepository() {
        ClientProfile profile = new ClientProfile(1L, "A", "B", "C", "S", "1", "ID",
                LocalDateTime.now(), null, 2L, "a", "CLIENT", LocalDateTime.now());
        when(clientRepository.findProfileByUserAccountId(2L)).thenReturn(Optional.of(profile));

        assertThat(service.findProfileByUserAccountId(2L)).contains(profile);
    }
}
