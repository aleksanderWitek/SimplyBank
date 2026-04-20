package com.alex.service;

import com.alex.UserAccountRole;
import com.alex.dto.Password;
import com.alex.dto.PasswordResetResponse;
import com.alex.dto.UserAccount;
import com.alex.exception.IllegalArgumentRuntimeException;
import com.alex.exception.NullPointerRuntimeException;
import com.alex.exception.UserAccountNotFoundRuntimeException;
import com.alex.repository.IUserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class UserAccountServiceTest {

    @Mock private IUserAccountRepository userAccountRepository;
    @Mock private IUserAccountProcessingService processingService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserAccountService service;

    // save ----------------------------------------------------------------------------------------

    @Test
    void save_nullFirstName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.save(null, "Smith", UserAccountRole.CLIENT))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void save_nullLastName_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> service.save("Alice", null, UserAccountRole.CLIENT))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
    }

    @Test
    void save_nullRole_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.save("Alice", "Smith", null))
                .isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void save_valid_generatesLoginEncodesPasswordAndReturnsRawPasswordInResult() {
        when(processingService.generateLogin("Alice", "Smith")).thenReturn("alismiab123456");
        when(processingService.generatePassword()).thenReturn("Raw-Pwd-1!");
        when(passwordEncoder.encode("Raw-Pwd-1!")).thenReturn("encoded");
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(42L);

        UserAccount result = service.save("Alice", "Smith", UserAccountRole.CLIENT);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getLogin()).isEqualTo("alismiab123456");
        // Returned DTO exposes the generated raw password so the caller can show it once.
        assertThat(result.getPassword()).isEqualTo("Raw-Pwd-1!");
        assertThat(result.getRole()).isEqualTo(UserAccountRole.CLIENT);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    // updatePassword ------------------------------------------------------------------------------

    @Test
    void updatePassword_invalidNewPassword_throwsIllegalArgumentRuntimeException() {
        Password password = new Password("OldPwd1!", "short");
        assertThatThrownBy(() -> service.updatePassword(1L, password))
                .isInstanceOf(IllegalArgumentRuntimeException.class);
        verify(userAccountRepository, never()).updatePassword(any(), any());
    }

    @Test
    void updatePassword_userNotFound_throwsUserAccountNotFoundRuntimeException() {
        Password password = new Password("OldPwd1234!", "NewPassword1234!");
        when(userAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePassword(1L, password))
                .isInstanceOf(UserAccountNotFoundRuntimeException.class)
                .hasMessageContaining("There is no User Account with provided id:1");
    }

    @Test
    void updatePassword_wrongCurrentPassword_throwsIllegalArgumentRuntimeException() {
        Password password = new Password("OldPwd1234!", "NewPassword1234!");
        UserAccount existing = new UserAccount(1L, "u", "existing-encoded", UserAccountRole.CLIENT,
                LocalDateTime.now());
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("OldPwd1234!", "existing-encoded")).thenReturn(false);

        assertThatThrownBy(() -> service.updatePassword(1L, password))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Invalid password");
    }

    @Test
    void updatePassword_sameAsCurrentPassword_throwsIllegalArgumentRuntimeException() {
        Password password = new Password("OldPwd1234!", "NewPassword1234!");
        UserAccount existing = new UserAccount(1L, "u", "existing-encoded", UserAccountRole.CLIENT,
                LocalDateTime.now());
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("OldPwd1234!", "existing-encoded")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword1234!", "existing-encoded")).thenReturn(true);

        assertThatThrownBy(() -> service.updatePassword(1L, password))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("New password must be different from current password");
    }

    @Test
    void updatePassword_happyPath_encodesAndPersists() {
        Password password = new Password("OldPwd1234!", "NewPassword1234!");
        UserAccount existing = new UserAccount(1L, "u", "existing-encoded", UserAccountRole.CLIENT,
                LocalDateTime.now());
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("OldPwd1234!", "existing-encoded")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword1234!", "existing-encoded")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword1234!")).thenReturn("new-encoded");

        service.updatePassword(1L, password);

        verify(userAccountRepository).updatePassword(1L, "new-encoded");
    }

    // resetPassword -------------------------------------------------------------------------------

    @Test
    void resetPassword_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.resetPassword(null))
                .isInstanceOf(NullPointerRuntimeException.class);
        verify(userAccountRepository, never()).updatePassword(any(), any());
    }

    @Test
    void resetPassword_userNotFound_throwsUserAccountNotFoundRuntimeException() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(1L))
                .isInstanceOf(UserAccountNotFoundRuntimeException.class)
                .hasMessageContaining("There is no User Account with provided id:1");

        verify(userAccountRepository, never()).updatePassword(any(), any());
    }

    @Test
    void resetPassword_happyPath_generatesEncodesPersistsAndReturnsPlaintext() {
        UserAccount existing = new UserAccount(1L, "alismi", "old-encoded", UserAccountRole.CLIENT,
                LocalDateTime.now());
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(processingService.generatePassword()).thenReturn("GenPwd-1!");
        when(passwordEncoder.encode("GenPwd-1!")).thenReturn("new-encoded");

        PasswordResetResponse response = service.resetPassword(1L);

        assertThat(response.getUserAccountId()).isEqualTo(1L);
        assertThat(response.getLogin()).isEqualTo("alismi");
        assertThat(response.getNewPassword()).isEqualTo("GenPwd-1!");
        verify(userAccountRepository).updatePassword(1L, "new-encoded");
    }

    // findById ------------------------------------------------------------------------------------

    @Test
    void findById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.findById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void findById_delegatesToRepository() {
        UserAccount account = new UserAccount(1L, "u", "p", UserAccountRole.CLIENT, LocalDateTime.now());
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThat(service.findById(1L)).contains(account);
    }

    // findAll -------------------------------------------------------------------------------------

    @Test
    void findAll_delegatesToRepository() {
        List<UserAccount> accounts = List.of();
        when(userAccountRepository.findAll()).thenReturn(accounts);
        assertThat(service.findAll()).isSameAs(accounts);
    }

    // deleteById ----------------------------------------------------------------------------------

    @Test
    void deleteById_nullId_throwsNullPointerRuntimeException() {
        assertThatThrownBy(() -> service.deleteById(null)).isInstanceOf(NullPointerRuntimeException.class);
    }

    @Test
    void deleteById_delegatesToRepository() {
        service.deleteById(1L);
        verify(userAccountRepository).deleteById(1L);
    }

    // loadUserByUsername --------------------------------------------------------------------------

    @Test
    void loadUserByUsername_unknownLogin_throwsUsernameNotFoundException() {
        when(userAccountRepository.findByLogin("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with login: missing");
    }

    @Test
    void loadUserByUsername_known_returnsUserDetailsWithRole() {
        UserAccount account = new UserAccount(1L, "alice", "encoded", UserAccountRole.CLIENT,
                LocalDateTime.now());
        when(userAccountRepository.findByLogin("alice")).thenReturn(Optional.of(account));

        UserDetails details = service.loadUserByUsername("alice");

        assertThat(details).isInstanceOf(User.class);
        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.getAuthorities()).extracting(a -> a.getAuthority())
                .containsExactly("ROLE_CLIENT");
    }
}
