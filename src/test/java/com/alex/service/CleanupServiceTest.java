package com.alex.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupServiceTest {

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private CleanupService cleanupService;

    @Test
    void purgeExpiredLoginAttempts_delegatesToLoginAttemptService_returnsRemovedCount() {
        when(loginAttemptService.purgeExpired()).thenReturn(7);

        int removed = cleanupService.purgeExpiredLoginAttempts();

        assertThat(removed).isEqualTo(7);
        verify(loginAttemptService).purgeExpired();
    }

    @Test
    void purgeExpiredLoginAttempts_nothingToPurge_returnsZero() {
        when(loginAttemptService.purgeExpired()).thenReturn(0);

        int removed = cleanupService.purgeExpiredLoginAttempts();

        assertThat(removed).isZero();
        verify(loginAttemptService).purgeExpired();
    }
}
