package com.alex.controller;

import com.alex.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BankAccountControllerIntegrationTest extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String saveRequestJson(Long clientId, String type, String currency) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("clientId", clientId);
        body.put("bankAccountType", type);
        body.put("bankAccountCurrency", currency);
        return objectMapper.writeValueAsString(body);
    }

    // POST /api/bank_account ------------------------------------------------------------------

    @Test
    void save_noToken_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/api/bank_account").contentType("application/json").content("{}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void save_asClientLinkedToProfile_returnsCreatedAndIgnoresBodyClientId() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        linkUserAccountToClient(1L, 10L);
        // Decoy client to verify the controller does NOT honor body clientId for CLIENT callers.
        insertClient(20L, "Mallory", "M", "W", "M", "2", "ID2");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(post("/api/bank_account")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(saveRequestJson(20L, "CHECKING", "EUR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value("CHECKING"));

        Integer linkedToAlice = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bank_account_client WHERE client_id = 10 AND delete_date IS NULL",
                Integer.class);
        Integer linkedToMallory = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bank_account_client WHERE client_id = 20 AND delete_date IS NULL",
                Integer.class);
        org.assertj.core.api.Assertions.assertThat(linkedToAlice).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(linkedToMallory).isEqualTo(0);
    }

    @Test
    void save_asClientWithoutProfile_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        // intentionally not linked
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(post("/api/bank_account")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(saveRequestJson(10L, "CHECKING", "EUR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void save_asClient_atTenAccounts_returnsConflict() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        linkUserAccountToClient(1L, 10L);
        for (long i = 1; i <= 10; i++) {
            insertBankAccount(100L + i, "10000000000" + i, "CHECKING", "EUR", new BigDecimal("0.00"));
            linkBankAccountToClient(100L + i, 10L);
        }
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(post("/api/bank_account")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(saveRequestJson(null, "CHECKING", "EUR")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Bank account limit reached (10 per client)"));
    }

    @Test
    void save_asEmployee_returnsCreated() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(post("/api/bank_account")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(saveRequestJson(10L, "CHECKING", "EUR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void save_asAdmin_returnsCreated() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(post("/api/bank_account")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(saveRequestJson(10L, "SAVING", "USD")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value("SAVING"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void save_invalidType_returnsBadRequest() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(post("/api/bank_account")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(saveRequestJson(10L, "INVALID", "EUR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_invalidCurrency_returnsBadRequest() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(post("/api/bank_account")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(saveRequestJson(10L, "CHECKING", "XYZ")))
                .andExpect(status().isBadRequest());
    }

    // GET /api/bank_account/{id} --------------------------------------------------------------

    @Test
    void findById_asOwningClient_returnsAccount() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        linkUserAccountToClient(1L, 10L);
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("50.00"));
        linkBankAccountToClient(100L, 10L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/bank_account/100").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    void findById_asNonOwningClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        linkUserAccountToClient(1L, 10L);
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("50.00"));
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/bank_account/200").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have access to this bank account"));
    }

    @Test
    void findById_asEmployee_returnsAny() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("50.00"));
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/bank_account/100").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void findById_asAdmin_returnsAny() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("50.00"));
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/bank_account/100").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void findById_missing_returnsNotFound() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/bank_account/999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no bank account with provided id:999"));
    }

    // GET /api/bank_account -------------------------------------------------------------------

    @Test
    void findAll_asClient_returnsOnlyOwnedAccounts() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        linkUserAccountToClient(1L, 10L);
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("50.00"));
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("75.00"));
        linkBankAccountToClient(100L, 10L);
        // 200 not linked to alice
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/bank_account").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void findAll_asEmployee_returnsAll() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("50.00"));
        insertBankAccount(200L, "100000000002", "CHECKING", "EUR", new BigDecimal("75.00"));
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/bank_account").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findAll_asAdmin_returnsAll() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("50.00"));
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/bank_account").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // DELETE /api/bank_account/{id} -----------------------------------------------------------

    @Test
    void deleteById_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("0.00"));
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(delete("/api/bank_account/100").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteById_asEmployee_returnsNoContent() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("0.00"));
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(delete("/api/bank_account/100").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteById_asEmployeeWithLinkedClients_unlinksAndReturnsNoContent() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertClient(10L, "Alice", "A", "W", "M", "1", "ID");
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("0.00"));
        linkBankAccountToClient(100L, 10L);
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(delete("/api/bank_account/100").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteById_missingBankAccount_returnsNotFound() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(delete("/api/bank_account/9999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
