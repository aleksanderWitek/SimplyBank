package com.alex.config;

import com.alex.exception.SecurityRuntimeException;
import com.alex.service.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;

    public LoginRateLimitFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath())) {
                String username = request.getParameter("username");
                if (username != null && loginAttemptService.isBlocked(username)) {
                    int seconds = loginAttemptService.getRemainingLockSeconds(username);
                    int minutes = (seconds + 59) / 60;
                    response.sendRedirect("/login?locked&minutes=" + minutes);
                    return;
                }
            }
        } catch (Exception e) {
            // Guard only the rate-limit logic; downstream errors must surface normally
            // rather than being masked as a generic security failure.
            throw new SecurityRuntimeException("Failed to process login rate limit filter", e);
        }

        filterChain.doFilter(request, response);
    }
}
