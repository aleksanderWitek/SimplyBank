package com.alex.config;

import com.alex.exception.SecurityRuntimeException;
import com.alex.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationFailureHandlerTest {

    @Mock private LoginAttemptService loginAttemptService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private AuthenticationException authException;
    @Mock private RedirectStrategy redirectStrategy;

    private CustomAuthenticationFailureHandler newHandler() {
        CustomAuthenticationFailureHandler handler = new CustomAuthenticationFailureHandler(loginAttemptService);
        handler.setRedirectStrategy(redirectStrategy);
        return handler;
    }

    @Test
    void onAuthenticationFailure_nullUsername_redirectsToErrorOnly() throws Exception {
        when(request.getParameter("username")).thenReturn(null);

        newHandler().onAuthenticationFailure(request, response, authException);

        verify(loginAttemptService, never()).loginFailed(org.mockito.ArgumentMatchers.anyString());
        verify(redirectStrategy).sendRedirect(request, response, "/login?error");
    }

    @Test
    void onAuthenticationFailure_notYetBlocked_incrementsAndRedirectsError() throws Exception {
        when(request.getParameter("username")).thenReturn("alice");
        when(loginAttemptService.isBlocked("alice")).thenReturn(false);

        newHandler().onAuthenticationFailure(request, response, authException);

        verify(loginAttemptService).loginFailed("alice");
        verify(redirectStrategy).sendRedirect(request, response, "/login?error");
    }

    @Test
    void onAuthenticationFailure_blocked_redirectsLockedWithMinutes() throws Exception {
        when(request.getParameter("username")).thenReturn("alice");
        when(loginAttemptService.isBlocked("alice")).thenReturn(true);
        when(loginAttemptService.getRemainingLockSeconds("alice")).thenReturn(61);

        newHandler().onAuthenticationFailure(request, response, authException);

        // ceil(61/60) = 2
        verify(redirectStrategy).sendRedirect(request, response, "/login?locked&minutes=2");
        verify(redirectStrategy, never()).sendRedirect(request, response, "/login?error");
    }

    @Test
    void onAuthenticationFailure_ioError_wrapsInSecurityRuntimeException() throws Exception {
        when(request.getParameter("username")).thenReturn(null);
        doThrow(new IOException("fail")).when(redirectStrategy)
                .sendRedirect(request, response, "/login?error");

        assertThatThrownBy(() -> newHandler().onAuthenticationFailure(request, response, authException))
                .isInstanceOf(SecurityRuntimeException.class)
                .hasMessageContaining("Failed to handle authentication failure redirect");
    }
}
