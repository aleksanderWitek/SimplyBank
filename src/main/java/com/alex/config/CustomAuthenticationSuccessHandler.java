package com.alex.config;

import com.alex.exception.SecurityRuntimeException;
import com.alex.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;

    public CustomAuthenticationSuccessHandler(LoginAttemptService loginAttemptService) {
        super("/");
        this.loginAttemptService = loginAttemptService;
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        try {
            loginAttemptService.loginSucceeded(authentication.getName());
            super.onAuthenticationSuccess(request, response, authentication);
        } catch (Exception e) {
            throw new SecurityRuntimeException("Failed to handle authentication success redirect", e);
        }
    }
}
