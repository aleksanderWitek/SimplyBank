package com.alex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    void isBlocked_noFailedAttempts_returnsFalse() {
        assertThat(service.isBlocked("alice")).isFalse();
    }

    @Test
    void isBlocked_fewerThanFiveFailures_returnsFalse() {
        service.loginFailed("alice");
        service.loginFailed("alice");
        service.loginFailed("alice");

        assertThat(service.isBlocked("alice")).isFalse();
    }

    @Test
    void isBlocked_fiveFailures_returnsTrue() {
        for (int i = 0; i < 5; i++) {
            service.loginFailed("alice");
        }

        assertThat(service.isBlocked("alice")).isTrue();
    }

    @Test
    void loginSucceeded_clearsAttempts() {
        for (int i = 0; i < 5; i++) {
            service.loginFailed("alice");
        }
        assertThat(service.isBlocked("alice")).isTrue();

        service.loginSucceeded("alice");

        assertThat(service.isBlocked("alice")).isFalse();
    }

    @Test
    void getRemainingLockSeconds_noAttempts_returnsZero() {
        assertThat(service.getRemainingLockSeconds("alice")).isEqualTo(0);
    }

    @Test
    void getRemainingLockSeconds_afterFailure_returnsPositive() {
        service.loginFailed("alice");

        int remaining = service.getRemainingLockSeconds("alice");

        assertThat(remaining).isPositive().isLessThanOrEqualTo(15 * 60);
    }

    @Test
    void isBlocked_expiredAttempts_returnsFalseAndClears() throws Exception {
        // Simulate expired entry: 5 failures but firstAttempt is 20 minutes ago.
        service.loginFailed("alice");
        overwriteAttempt(service, "alice", 5, LocalDateTime.now().minusMinutes(20));

        assertThat(service.isBlocked("alice")).isFalse();
    }

    @Test
    void loginFailed_afterExpired_resetsCounter() throws Exception {
        overwriteAttempt(service, "alice", 5, LocalDateTime.now().minusMinutes(20));

        service.loginFailed("alice");

        // Count should be reset to 1 → not blocked.
        assertThat(service.isBlocked("alice")).isFalse();
    }

    @Test
    void getRemainingLockSeconds_expiredAttempts_returnsZero() throws Exception {
        overwriteAttempt(service, "alice", 5, LocalDateTime.now().minusMinutes(20));

        assertThat(service.getRemainingLockSeconds("alice")).isEqualTo(0);
    }

    /**
     * Reflection helper that replaces or inserts an {@code AttemptInfo} with a
     * caller-chosen {@code firstAttempt} timestamp. Lets us cover the "expired"
     * branches of the service without waiting 15 minutes.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void overwriteAttempt(LoginAttemptService service, String username,
                                         int count, LocalDateTime firstAttempt) throws Exception {
        Field attemptsField = LoginAttemptService.class.getDeclaredField("attempts");
        attemptsField.setAccessible(true);
        ConcurrentHashMap map = (ConcurrentHashMap) attemptsField.get(service);
        Class<?> infoClass = Class.forName("com.alex.service.LoginAttemptService$AttemptInfo");
        var ctor = infoClass.getDeclaredConstructor(int.class, LocalDateTime.class);
        ctor.setAccessible(true);
        map.put(username, ctor.newInstance(count, firstAttempt));
    }
}
