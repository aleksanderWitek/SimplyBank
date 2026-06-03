package com.alex.config;

import com.alex.service.IUserAccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private IUserAccountService userAccountService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_nonApiPath_continuesChainWithoutTouchingJwt() throws Exception {
        when(request.getRequestURI()).thenReturn("/login");

        new JwtAuthenticationFilter(jwtService, userAccountService)
                .doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService, userAccountService);
    }

    @Test
    void doFilterInternal_missingAuthHeader_continuesChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/accounts");
        when(request.getHeader("Authorization")).thenReturn(null);

        new JwtAuthenticationFilter(jwtService, userAccountService)
                .doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService, userAccountService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_nonBearerHeader_continuesChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/accounts");
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        new JwtAuthenticationFilter(jwtService, userAccountService)
                .doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService, userAccountService);
    }

    @Test
    void doFilterInternal_invalidToken_continuesChainWithoutAuthenticating() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/accounts");
        when(request.getHeader("Authorization")).thenReturn("Bearer bad");
        when(jwtService.isTokenValid("bad")).thenReturn(false);

        new JwtAuthenticationFilter(jwtService, userAccountService)
                .doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(userAccountService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_validToken_populatesSecurityContext() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/accounts");
        when(request.getHeader("Authorization")).thenReturn("Bearer good");
        when(jwtService.isTokenValid("good")).thenReturn(true);
        when(jwtService.extractUsername("good")).thenReturn("alice");

        UserDetails userDetails = new User("alice", "pwd",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        when(userAccountService.loadUserByUsername("alice")).thenReturn(userDetails);

        new JwtAuthenticationFilter(jwtService, userAccountService)
                .doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isSameAs(userDetails);
        assertThat(auth.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_CLIENT");
        assertThat(auth.getDetails()).isNotNull();
    }

    @Test
    void doFilterInternal_existingAuthentication_notOverwritten() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/accounts");
        when(request.getHeader("Authorization")).thenReturn("Bearer good");
        when(jwtService.isTokenValid("good")).thenReturn(true);
        when(jwtService.extractUsername("good")).thenReturn("alice");

        UsernamePasswordAuthenticationToken existing = new UsernamePasswordAuthenticationToken(
                "existing-user", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(existing);

        new JwtAuthenticationFilter(jwtService, userAccountService)
                .doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(userAccountService, never()).loadUserByUsername(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
    }

    @Test
    void doFilterInternal_validTokenUnknownUser_continuesChainWithoutAuthenticating() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/accounts");
        when(request.getHeader("Authorization")).thenReturn("Bearer good");
        when(jwtService.isTokenValid("good")).thenReturn(true);
        when(jwtService.extractUsername("good")).thenReturn("ghost");
        when(userAccountService.loadUserByUsername("ghost"))
                .thenThrow(new UsernameNotFoundException("User not found with login: ghost"));

        new JwtAuthenticationFilter(jwtService, userAccountService)
                .doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_validTokenNullUsername_continuesChainWithoutAuthenticating() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/accounts");
        when(request.getHeader("Authorization")).thenReturn("Bearer good");
        when(jwtService.isTokenValid("good")).thenReturn(true);
        when(jwtService.extractUsername("good")).thenReturn(null);

        new JwtAuthenticationFilter(jwtService, userAccountService)
                .doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(userAccountService, never()).loadUserByUsername(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
