package com.alex.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CleanupServiceTest {

    @Test
    void canBeInstantiated() {
        CleanupService service = new CleanupService();
        assertThat(service).isNotNull();
    }
}
