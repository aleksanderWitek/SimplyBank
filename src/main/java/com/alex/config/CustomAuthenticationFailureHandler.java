package com.alex.config;

import com.alex.exception.SecurityRuntimeException;
import com.alex.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    public CustomAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) {
        try {
            String username = request.getParameter("username");
            if (username != null) {
                loginAttemptService.loginFailed(username);

                if (loginAttemptService.isBlocked(username)) {
                    int seconds = loginAttemptService.getRemainingLockSeconds(username);
                    int minutes = (seconds + 59) / 60;
                    getRedirectStrategy().sendRedirect(request, response,
                            "/login?locked&minutes=" + minutes);
                    return;
                }
            }

            getRedirectStrategy().sendRedirect(request, response, "/login?error");
        } catch (Exception e) {
            throw new SecurityRuntimeException("Failed to handle authentication failure redirect", e);
        }
    }
}
