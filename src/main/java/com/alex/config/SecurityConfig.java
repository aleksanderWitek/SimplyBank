package com.alex.config;

import com.alex.exception.SecurityRuntimeException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final LoginRateLimitFilter loginRateLimitFilter;

    public SecurityConfig(CustomAuthenticationFailureHandler failureHandler,
                          CustomAuthenticationSuccessHandler successHandler,
                          LoginRateLimitFilter loginRateLimitFilter) {
        this.failureHandler = failureHandler;
        this.successHandler = successHandler;
        this.loginRateLimitFilter = loginRateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http
                    .csrf(AbstractHttpConfigurer::disable)
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

                            // Client API — profile for all authenticated, rest for staff
                            .requestMatchers(HttpMethod.GET, "/api/client/profile").authenticated()
                            .requestMatchers("/api/client/**").hasAnyRole("EMPLOYEE", "ADMIN")

                            // Bank Account API — read for all, write for staff
                            .requestMatchers(HttpMethod.GET, "/api/bank_account/**").authenticated()
                            .requestMatchers("/api/bank_account/**").hasAnyRole("EMPLOYEE", "ADMIN")

                            // Transaction API — GET all is staff-only, per-account and POST for all
                            .requestMatchers(HttpMethod.GET, "/api/transaction/bank_account_from/**").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/transaction/bank_account_to/**").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/transaction/between_bank_accounts").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/transaction/{id}").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/transaction").hasAnyRole("EMPLOYEE", "ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/transaction/**").authenticated()

                            // User Account API — list all is admin-only, rest authenticated
                            .requestMatchers(HttpMethod.GET, "/api/user_account").hasRole("ADMIN")
                            .requestMatchers("/api/user_account/**").authenticated()

                            // All other pages — authenticated
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                    .formLogin(form -> form
                            .loginPage("/login")
                            .successHandler(successHandler)
                            .failureHandler(failureHandler)
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutSuccessUrl("/login?logout")
                            .permitAll()
                    );

            return http.build();
        } catch (Exception e) {
            throw new SecurityRuntimeException("Failed to build security filter chain", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
