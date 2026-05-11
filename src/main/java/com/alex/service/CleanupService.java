package com.alex.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CleanupService implements ICleanupService {

    private final LoginAttemptService loginAttemptService;

    public CleanupService(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    @Scheduled(fixedDelayString = "${cleanup.login-attempts.interval-ms:900000}")
    public int purgeExpiredLoginAttempts() {
        return loginAttemptService.purgeExpired();
    }
}
