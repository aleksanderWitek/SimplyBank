package com.alex.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-only-secret-at-least-32-bytes-long-xxxxxxxxxxxxxxxxx";

    private static JwtService newService(long expirationMs) throws Exception {
        JwtService service = new JwtService(SECRET, expirationMs);
        Method init = JwtService.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(service);
        return service;
    }

    @Test
    void generateToken_andExtract_roundTripsUsernameAndRole() throws Exception {
        JwtService service = newService(60_000L);

        String token = service.generateToken("alice", "CLIENT");

        assertThat(service.extractUsername(token)).isEqualTo("alice");
        assertThat(service.extractRole(token)).isEqualTo("CLIENT");
    }

    @Test
    void isTokenValid_freshToken_returnsTrue() throws Exception {
        JwtService service = newService(60_000L);

        String token = service.generateToken("alice", "CLIENT");

        assertThat(service.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() throws Exception {
        JwtService service = newService(1L);

        String token = service.generateToken("alice", "CLIENT");
        Thread.sleep(10L);

        assertThat(service.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() throws Exception {
        JwtService service = newService(60_000L);

        String token = service.generateToken("alice", "CLIENT");
        String tampered = token.substring(0, token.length() - 2) + "AA";

        assertThat(service.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValid_garbageToken_returnsFalse() throws Exception {
        JwtService service = newService(60_000L);

        assertThat(service.isTokenValid("not-a-token")).isFalse();
    }

    @Test
    void extractRole_withDifferentRole_returnsMatchingRole() throws Exception {
        JwtService service = newService(60_000L);

        String token = service.generateToken("bob", "ADMIN");

        assertThat(service.extractRole(token)).isEqualTo("ADMIN");
    }
}
