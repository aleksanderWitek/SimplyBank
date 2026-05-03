package com.alex.config;

import com.alex.exception.SecurityRuntimeException;
import com.alex.service.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginRateLimitFilterTest {

    @Mock private LoginAttemptService loginAttemptService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;

    @Test
    void doFilter_getMethod_skipsRateLimit() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        new LoginRateLimitFilter(loginAttemptService).doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(loginAttemptService);
    }

    @Test
    void doFilter_postLoginNullUsername_continuesChain() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("username")).thenReturn(null);

        new LoginRateLimitFilter(loginAttemptService).doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_postLoginNotBlocked_continuesChain() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("username")).thenReturn("alice");
        when(loginAttemptService.isBlocked("alice")).thenReturn(false);

        new LoginRateLimitFilter(loginAttemptService).doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void doFilter_postLoginBlocked_redirectsAndSkipsChain() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("username")).thenReturn("alice");
        when(loginAttemptService.isBlocked("alice")).thenReturn(true);
        when(loginAttemptService.getRemainingLockSeconds("alice")).thenReturn(120);

        new LoginRateLimitFilter(loginAttemptService).doFilter(request, response, chain);

        verify(response).sendRedirect("/login?locked&minutes=2");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_ioErrorOnRedirect_wrapsInSecurityRuntimeException() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("username")).thenReturn("alice");
        when(loginAttemptService.isBlocked("alice")).thenReturn(true);
        when(loginAttemptService.getRemainingLockSeconds("alice")).thenReturn(60);
        doThrow(new IOException("fail")).when(response).sendRedirect("/login?locked&minutes=1");

        LoginRateLimitFilter filter = new LoginRateLimitFilter(loginAttemptService);

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(SecurityRuntimeException.class)
                .hasMessageContaining("Failed to process login rate limit filter");
    }
}
