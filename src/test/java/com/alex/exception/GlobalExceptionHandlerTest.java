package com.alex.exception;

import com.alex.exception.GlobalExceptionHandler.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleClientNotFound_returns404WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleClientNotFound(new ClientNotFoundRuntimeException("client 42 not found"));

        assertBody(response, HttpStatus.NOT_FOUND, "client 42 not found");
    }

    @Test
    void handleEmployeeNotFound_returns404WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleEmployeeNotFound(new EmployeeNotFoundRuntimeException("employee 7 not found"));

        assertBody(response, HttpStatus.NOT_FOUND, "employee 7 not found");
    }

    @Test
    void handleUserAccountNotFound_returns404WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUserAccountNotFound(new UserAccountNotFoundRuntimeException("user 9 not found"));

        assertBody(response, HttpStatus.NOT_FOUND, "user 9 not found");
    }

    @Test
    void handleBankAccountNotFound_returns404WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBankAccountNotFound(new BankAccountNotFoundRuntimeException("account 3 not found"));

        assertBody(response, HttpStatus.NOT_FOUND, "account 3 not found");
    }

    @Test
    void handleTransactionNotFound_returns404WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTransactionNotFound(new TransactionNotFoundRuntimeException("transaction 1 not found"));

        assertBody(response, HttpStatus.NOT_FOUND, "transaction 1 not found");
    }

    @Test
    void handleIllegalArgument_returns400WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentRuntimeException("bad input"));

        assertBody(response, HttpStatus.BAD_REQUEST, "bad input");
    }

    @Test
    void handleDataAccess_returns500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDataAccess(new EmptyResultDataAccessException(1));

        assertBody(response, HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred");
    }

    @Test
    void handleAccessDenied_returns403WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedRuntimeException("not your resource"));

        assertBody(response, HttpStatus.FORBIDDEN, "not your resource");
    }

    @Test
    void handleIllegalState_returns409WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalState(new IllegalStateRuntimeException("insufficient funds"));

        assertBody(response, HttpStatus.CONFLICT, "insufficient funds");
    }

    @Test
    void handleSecurity_returns401WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleSecurity(new SecurityRuntimeException("bad token"));

        assertBody(response, HttpStatus.UNAUTHORIZED, "bad token");
    }

    @Test
    void handleSQL_returns500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleSQL(new SQLRuntimeException("column missing"));

        assertBody(response, HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred");
    }

    @Test
    void handleNullPointerRuntime_returns500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNullPointerRuntime(new NullPointerRuntimeException("npe somewhere"));

        assertBody(response, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: null value encountered");
    }

    @Test
    void handleNullPointer_returns500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNullPointer(new NullPointerException("npe"));

        assertBody(response, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: null value encountered");
    }

    @Test
    void handleGenericException_returns500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new Exception("boom"));

        assertBody(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static void assertBody(ResponseEntity<ErrorResponse> response,
                                   HttpStatus expectedStatus, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(expectedStatus.value());
        assertThat(body.message()).isEqualTo(expectedMessage);
        assertThat(body.timestamp()).isNotNull();
    }
}
