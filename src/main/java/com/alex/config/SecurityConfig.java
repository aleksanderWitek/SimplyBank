package com.alex.config;

import com.alex.exception.SecurityRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomAuthenticationFailureHandler failureHandler,
                          CustomAuthenticationSuccessHandler successHandler,
                          LoginRateLimitFilter loginRateLimitFilter,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.failureHandler = failureHandler;
        this.successHandler = successHandler;
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http
                    // CSRF protection for the session-cookie (browser) flow. The token is stored in
                    // a JS-readable cookie (XSRF-TOKEN) and echoed back via the X-XSRF-TOKEN header by
                    // the frontend. Stateless requests authenticated with a Bearer token are exempt —
                    // they carry no session cookie and therefore cannot be CSRF-forged.
                    .csrf(csrf -> csrf
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                            .ignoringRequestMatchers(SecurityConfig::hasBearerToken))
                    .sessionManagement(sm -> sm
                            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                            .invalidSessionUrl("/login?expired")
                            .maximumSessions(1))
                    .authorizeHttpRequests(auth -> auth
                            // Public resources
                            .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()

                            // Auth API — all authenticated users
                            .requestMatchers("/api/auth/**").authenticated()

                            // Management page — EMPLOYEE and ADMIN only
                            .requestMatchers("/management").hasAnyRole("EMPLOYEE", "ADMIN")

                            // Employee API — specific paths before wildcard
                            .requestMatchers(HttpMethod.GET, "/api/employee/admin-profile").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/employee/profile").hasAnyRole("EMPLOYEE", "ADMIN")
                            .requestMatchers("/api/employee/**").hasRole("ADMIN")

                            // Client API — profile for all authenticated, self-delete for client, hard-delete admin-only, rest for staff
                            .requestMatchers(HttpMethod.GET, "/api/client/profile").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/client/profile/delete").hasRole("CLIENT")
                            .requestMatchers(HttpMethod.DELETE, "/api/client/**").hasRole("ADMIN")
                            .requestMatchers("/api/client/**").hasAnyRole("EMPLOYEE", "ADMIN")

                            // Bank Account API — staff-only lookup endpoints first, then general read for all, then create
                            .requestMatchers(HttpMethod.GET, "/api/bank_account/by-number/*/currency").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/bank_account/by-number/**").hasAnyRole("EMPLOYEE", "ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/bank_account/by-client/**").hasAnyRole("EMPLOYEE", "ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/bank_account/*/owners").hasAnyRole("EMPLOYEE", "ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/bank_account/**").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/bank_account").authenticated()
                            .requestMatchers("/api/bank_account/**").hasAnyRole("EMPLOYEE", "ADMIN")

                            // Transaction API — GET all is staff-only; funds movement is CLIENT-only
                            .requestMatchers(HttpMethod.GET, "/api/transaction/bank_account_from/**").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/transaction/bank_account_to/**").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/transaction/between_bank_accounts").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/transaction/{id}").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/transaction").hasAnyRole("EMPLOYEE", "ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/transaction/transfer").hasRole("CLIENT")
                            .requestMatchers(HttpMethod.POST, "/api/transaction/deposit").hasRole("CLIENT")
                            .requestMatchers(HttpMethod.POST, "/api/transaction/withdraw").hasRole("CLIENT")

                            // User Account API — list all and admin password reset are admin-only, rest authenticated
                            .requestMatchers(HttpMethod.GET, "/api/user_account").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/user_account/*/password/reset").hasRole("ADMIN")
                            .requestMatchers("/api/user_account/**").authenticated()

                            // All other pages — authenticated
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .formLogin(form -> form
                            .loginPage("/login")
                            .successHandler(successHandler)
                            .failureHandler(failureHandler)
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutSuccessUrl("/login?logout")
                            .deleteCookies("JSESSIONID")
                            .invalidateHttpSession(true)
                            .clearAuthentication(true)
                            .permitAll()
                    );

            return http.build();
        } catch (Exception e) {
            throw new SecurityRuntimeException("Failed to build security filter chain", e);
        }
    }

    private static boolean hasBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
    }
}
