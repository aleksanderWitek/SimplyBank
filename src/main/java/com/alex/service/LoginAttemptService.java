package com.alex.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        AttemptInfo info = attempts.compute(username, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new AttemptInfo(1, LocalDateTime.now());
            }
            return new AttemptInfo(existing.count + 1, existing.firstAttempt);
        });
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    public boolean isBlocked(String username) {
        AttemptInfo info = attempts.get(username);
        if (info == null) {
            return false;
        }
        if (info.isExpired()) {
            attempts.remove(username);
            return false;
        }
        return info.count >= MAX_ATTEMPTS;
    }

    public int getRemainingLockSeconds(String username) {
        AttemptInfo info = attempts.get(username);
        if (info == null || info.isExpired()) {
            return 0;
        }
        LocalDateTime unlockTime = info.firstAttempt.plusMinutes(LOCK_DURATION_MINUTES);
        long seconds = java.time.Duration.between(LocalDateTime.now(), unlockTime).getSeconds();
        return (int) Math.max(0, seconds);
    }

    private static class AttemptInfo {
        final int count;
        final LocalDateTime firstAttempt;

        AttemptInfo(int count, LocalDateTime firstAttempt) {
            this.count = count;
            this.firstAttempt = firstAttempt;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(firstAttempt.plusMinutes(LOCK_DURATION_MINUTES));
        }
    }
}
