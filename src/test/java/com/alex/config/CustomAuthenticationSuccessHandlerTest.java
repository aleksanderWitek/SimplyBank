package com.alex.config;

import com.alex.exception.SecurityRuntimeException;
import com.alex.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationSuccessHandlerTest {

    @Mock private LoginAttemptService loginAttemptService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Authentication authentication;

    @Test
    void onAuthenticationSuccess_clearsAttemptsAndRedirects() throws Exception {
        when(authentication.getName()).thenReturn("alice");
        // After commit / clean response, Spring will sendRedirect to "/"
        when(response.encodeRedirectURL("/")).thenReturn("/");

        CustomAuthenticationSuccessHandler handler = new CustomAuthenticationSuccessHandler(loginAttemptService);
        handler.onAuthenticationSuccess(request, response, authentication);

        verify(loginAttemptService).loginSucceeded("alice");
        verify(response).sendRedirect("/");
    }

    @Test
    void onAuthenticationSuccess_ioError_wrapsInSecurityRuntimeException() throws Exception {
        when(authentication.getName()).thenReturn("alice");
        when(response.encodeRedirectURL("/")).thenReturn("/");
        org.mockito.Mockito.doThrow(new IOException("fail")).when(response).sendRedirect("/");

        CustomAuthenticationSuccessHandler handler = new CustomAuthenticationSuccessHandler(loginAttemptService);

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(SecurityRuntimeException.class)
                .hasMessageContaining("Failed to handle authentication success redirect");
    }
}
