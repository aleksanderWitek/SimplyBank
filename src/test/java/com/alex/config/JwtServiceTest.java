package com.alex.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-only-secret-at-least-32-bytes-long-xxxxxxxxxxxxxxxxx";

    private static JwtService newService(long expirationMs) throws Exception {
        return newService(SECRET, expirationMs);
    }

    private static JwtService newService(String secret, long expirationMs) throws Exception {
        JwtService service = new JwtService(secret, expirationMs);
        invokeInit(service);
        return service;
    }

    private static void invokeInit(JwtService service) throws Exception {
        Method init = JwtService.class.getDeclaredMethod("init");
        init.setAccessible(true);
        try {
            init.invoke(service);
        } catch (InvocationTargetException e) {
            // Unwrap so tests can assert on the real cause thrown inside init().
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw e;
        }
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

    @Test
    void init_nullSecret_throws() {
        assertThatThrownBy(() -> newService(null, 60_000L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.secret must be configured");
    }

    @Test
    void init_blankSecret_throws() {
        assertThatThrownBy(() -> newService("   ", 60_000L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.secret must be configured");
    }

    @Test
    void init_insecureDefaultSecret_throws() {
        String insecureDefault = "changeme-local-dev-secret-at-least-32-bytes-long-abcdef";

        assertThatThrownBy(() -> newService(insecureDefault, 60_000L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("insecure default");
    }

    @Test
    void init_secretShorterThan32Bytes_throws() {
        assertThatThrownBy(() -> newService("too-short-secret", 60_000L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}
