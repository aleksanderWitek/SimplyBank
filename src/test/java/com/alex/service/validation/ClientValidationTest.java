package com.alex.service.validation;

import com.alex.dto.Client;
import com.alex.exception.IllegalArgumentRuntimeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientValidationTest {

    @Test
    void ensureClientPresent_nullClient_throwsIllegalArgumentRuntimeException() {
        assertThatThrownBy(() -> ClientValidation.ensureClientPresent(null))
                .isInstanceOf(IllegalArgumentRuntimeException.class)
                .hasMessage("Client is null");
    }

    @Test
    void ensureClientPresent_presentClient_doesNotThrow() {
        Client client = new Client();
        assertThatCode(() -> ClientValidation.ensureClientPresent(client)).doesNotThrowAnyException();
    }

    @Test
    void constructor_canBeInstantiated_forCoverage() {
        assertThatCode(ClientValidation::new).doesNotThrowAnyException();
    }
}
