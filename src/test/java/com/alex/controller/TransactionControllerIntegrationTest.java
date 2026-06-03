package com.alex.controller;

import com.alex.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerIntegrationTest extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private void setupAliceWithAccount(Long userId, Long clientId, Long bankAccountId,
                                       BigDecimal balance, String currency, String login) {
        insertUserAccount(userId, login, "Password1!", "CLIENT");
        insertClient(clientId, "First" + userId, "Last" + userId, "W", "M", "1", "ID" + userId);
        linkUserAccountToClient(userId, clientId);
        insertBankAccount(bankAccountId, "1000000000" + bankAccountId, "CHECKING", currency, balance);
        linkBankAccountToClient(bankAccountId, clientId);
    }

    private void insertTransaction(Long id, String type, String currency, BigDecimal amount,
                                   Long fromId, Long toId) {
        jdbcTemplate.update(
                "INSERT INTO transaction (id, transaction_type, currency, amount, bank_account_id_from, bank_account_id_to, description, create_date)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, type, currency, amount, fromId, toId, "test",
                Timestamp.valueOf(LocalDateTime.now()));
    }

    // POST /api/transaction/transfer ----------------------------------------------------------

    @Test
    void transfer_noToken_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/api/transaction/transfer").contentType("application/json").content("{}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void transfer_happyPath_updatesBothBalances() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        setupAliceWithAccount(2L, 20L, 200L, new BigDecimal("10.00"), "EUR", "carl");
        // Alice owns 100; carl owns 200.
        // Link 200 to alice as well so she can be the sender
        linkBankAccountToClient(200L, 10L);
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "bankAccountToId", 200L,
                "amount", 30.00, "currency", "EUR", "description", "Rent"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(30.00));

        assertThat(balanceOf(100L)).isEqualByComparingTo("70.00");
        assertThat(balanceOf(200L)).isEqualByComparingTo("40.00");
    }

    @Test
    void transfer_fromAccountNotOwnedByClient_isForbidden() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 200L, "bankAccountToId", 100L,
                "amount", 5.00, "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have access to the source bank account"));
    }

    @Test
    void transfer_insufficientBalance_returnsConflict() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("1.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        linkBankAccountToClient(200L, 10L);
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "bankAccountToId", 200L,
                "amount", 50.00, "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void transfer_sameAccount_returnsBadRequest() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "bankAccountToId", 100L,
                "amount", 5.00, "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_currencyMismatch_returnsBadRequest() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "USD", new BigDecimal("10.00"));
        linkBankAccountToClient(200L, 10L);
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "bankAccountToId", 200L,
                "amount", 5.00, "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_missingToAccount_returnsNotFound() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "bankAccountToId", 999L,
                "amount", 5.00, "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // POST /api/transaction/deposit -----------------------------------------------------------

    @Test
    void deposit_asOwningClient_updatesBalance() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("50.00"), "EUR", "alice");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 100L, "amount", 20.00,
                "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));

        assertThat(balanceOf(100L)).isEqualByComparingTo("70.00");
    }

    @Test
    void deposit_toNonOwnedAccount_isForbidden() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("50.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 200L, "amount", 20.00,
                "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have access to the target bank account"));
    }

    @Test
    void deposit_exceedingDailyLimitOnSameAccount_returnsConflict() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("0.00"), "PLN", "alice");
        String token = generateToken("alice", "CLIENT");

        // First deposit: 600 PLN — succeeds
        String first = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 100L, "amount", 600.00,
                "currency", "PLN", "description", "x"));
        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(first))
                .andExpect(status().isCreated());

        // Second deposit: 500 PLN — 600 + 500 = 1100 > 1000 → 409
        String second = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 100L, "amount", 500.00,
                "currency", "PLN", "description", "x"));
        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Daily deposit limit")));

        assertThat(balanceOf(100L)).isEqualByComparingTo("600.00");
    }

    @Test
    void deposit_exceedingDailyLimitAcrossCurrenciesAndAccounts_returnsConflict() throws Exception {
        // Alice owns a PLN account (100) and an EUR account (200). 600 PLN + 500 EUR > 1000.
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("0.00"), "PLN", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("0.00"));
        linkBankAccountToClient(200L, 10L);
        String token = generateToken("alice", "CLIENT");

        String first = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 100L, "amount", 600.00,
                "currency", "PLN", "description", "x"));
        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(first))
                .andExpect(status().isCreated());

        String second = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 200L, "amount", 500.00,
                "currency", "EUR", "description", "x"));
        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Daily deposit limit")));

        assertThat(balanceOf(200L)).isEqualByComparingTo("0.00");
    }

    @Test
    void deposit_atDailyLimitBoundary_succeeds() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("0.00"), "PLN", "alice");
        String token = generateToken("alice", "CLIENT");

        String first = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 100L, "amount", 400.00,
                "currency", "PLN", "description", "x"));
        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(first))
                .andExpect(status().isCreated());

        // 400 + 600 = 1000 exactly → still allowed
        String second = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 100L, "amount", 600.00,
                "currency", "PLN", "description", "x"));
        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(second))
                .andExpect(status().isCreated());

        assertThat(balanceOf(100L)).isEqualByComparingTo("1000.00");
    }

    @Test
    void deposit_yesterdayDoesNotCountTowardsTodaysLimit() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("0.00"), "PLN", "alice");
        // Backdated DEPOSIT row: 800 yesterday should not count against today's bucket.
        jdbcTemplate.update(
                "INSERT INTO transaction (transaction_type, currency, amount, bank_account_id_from, bank_account_id_to, description, create_date)"
                        + " VALUES ('DEPOSIT', 'PLN', 800.00, NULL, ?, 'yesterday', ?)",
                100L, java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(1)));

        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 100L, "amount", 900.00,
                "currency", "PLN", "description", "today"));
        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());

        // Balance: 800 (backdated row only inserted the transaction, didn't touch balance) + 900 today
        assertThat(balanceOf(100L)).isEqualByComparingTo("900.00");
    }

    @Test
    void deposit_asEmployee_isForbidden() throws Exception {
        // Funds movement is CLIENT-only (SecurityConfig: POST /api/transaction/deposit hasRole CLIENT).
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("50.00"));
        String token = generateToken("bob", "EMPLOYEE");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountToId", 100L, "amount", 25.00,
                "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/deposit")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());

        assertThat(balanceOf(100L)).isEqualByComparingTo("50.00");
    }

    // POST /api/transaction/withdraw ----------------------------------------------------------

    @Test
    void withdraw_asOwningClient_updatesBalance() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "amount", 30.00,
                "currency", "EUR", "description", "ATM"));

        mockMvc.perform(post("/api/transaction/withdraw")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));

        assertThat(balanceOf(100L)).isEqualByComparingTo("70.00");
    }

    @Test
    void withdraw_fromNonOwnedAccount_isForbidden() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("100.00"));
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 200L, "amount", 5.00,
                "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/withdraw")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have access to the source bank account"));
    }

    @Test
    void withdraw_insufficientBalance_returnsConflict() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("1.00"), "EUR", "alice");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "amount", 500.00,
                "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/withdraw")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    // GET /api/transaction/{id} ---------------------------------------------------------------

    @Test
    void findById_asOwningClient_returnsTransaction() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertTransaction(500L, "TRANSFER", "EUR", new BigDecimal("5.00"), 100L, 200L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/500").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500));
    }

    @Test
    void findById_asNonOwningClient_isForbidden() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertBankAccount(300L, "100000000003", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertTransaction(500L, "TRANSFER", "EUR", new BigDecimal("5.00"), 200L, 300L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/500").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have access to this transaction"));
    }

    @Test
    void findById_asEmployee_returnsAny() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("100.00"));
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertTransaction(500L, "TRANSFER", "EUR", new BigDecimal("5.00"), 100L, 200L);
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/transaction/500").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void findById_missing_returnsNotFound() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/transaction/999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no transaction with provided id:999"));
    }

    // GET /api/transaction --------------------------------------------------------------------

    @Test
    void findAll_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAll_asEmployee_returnsList() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("100.00"));
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertTransaction(500L, "TRANSFER", "EUR", new BigDecimal("5.00"), 100L, 200L);
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/transaction").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // GET /api/transaction/bank_account_from/{id} --------------------------------------------

    @Test
    void findByFromId_asOwner_returnsList() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertTransaction(500L, "TRANSFER", "EUR", new BigDecimal("5.00"), 100L, 200L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/bank_account_from/100").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findByFromId_asNonOwner_isForbidden() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/bank_account_from/200").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    // GET /api/transaction/bank_account_to/{id} ----------------------------------------------

    @Test
    void findByToId_asOwner_returnsList() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertTransaction(500L, "TRANSFER", "EUR", new BigDecimal("5.00"), 200L, 100L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/bank_account_to/100").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findByToId_asNonOwner_isForbidden() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/bank_account_to/200").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    // GET /api/transaction/between_bank_accounts ----------------------------------------------

    @Test
    void findBetween_asOwnerOfFrom_returnsList() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertTransaction(500L, "TRANSFER", "EUR", new BigDecimal("5.00"), 100L, 200L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/between_bank_accounts")
                        .param("bank_account_from_id", "100")
                        .param("bank_account_to_id", "200")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findBetween_asNonOwnerOfBoth_isForbidden() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertBankAccount(300L, "100000000003", "CHECKING", "EUR", new BigDecimal("10.00"));
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/between_bank_accounts")
                        .param("bank_account_from_id", "200")
                        .param("bank_account_to_id", "300")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have access to these bank accounts"));
    }

    @Test
    void findBetween_asEmployee_returnsList() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("100.00"));
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("10.00"));
        insertTransaction(500L, "TRANSFER", "EUR", new BigDecimal("5.00"), 100L, 200L);
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/transaction/between_bank_accounts")
                        .param("bank_account_from_id", "100")
                        .param("bank_account_to_id", "200")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // POST /api/transaction/transfer — recipient resolved by account number -------------------

    @Test
    void transfer_byRecipientAccountNumber_updatesBalances() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        insertBankAccount(200L, "100000000200", "CHECKING", "EUR", new BigDecimal("0.00"));
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "bankAccountToNumber", "100000000200",
                "amount", 30.00, "currency", "EUR", "description", "by number"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());

        assertThat(balanceOf(100L)).isEqualByComparingTo("70.00");
        assertThat(balanceOf(200L)).isEqualByComparingTo("30.00");
    }

    @Test
    void transfer_byUnknownRecipientNumber_returnsNotFound() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("100.00"), "EUR", "alice");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "bankAccountFromId", 100L, "bankAccountToNumber", "999999999999",
                "amount", 30.00, "currency", "EUR", "description", "x"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // GET /api/transaction/{id} — deposit/withdrawal rows have a null counterparty ------------

    @Test
    void findById_depositIntoOwnedAccount_asClient_returnsTransaction() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("0.00"), "EUR", "alice");
        insertTransaction(500L, "DEPOSIT", "EUR", new BigDecimal("5.00"), null, 100L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/500").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));
    }

    @Test
    void findById_withdrawalFromOwnedAccount_asClient_returnsTransaction() throws Exception {
        setupAliceWithAccount(1L, 10L, 100L, new BigDecimal("50.00"), "EUR", "alice");
        insertTransaction(500L, "WITHDRAWAL", "EUR", new BigDecimal("5.00"), 100L, null);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/transaction/500").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));
    }
}
