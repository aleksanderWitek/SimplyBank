package com.alex.controller;

import com.alex.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientControllerIntegrationTest extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String clientJson(String firstName, String lastName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("city", "Warsaw");
        payload.put("street", "Main");
        payload.put("houseNumber", "1A");
        payload.put("identificationNumber", "ID-" + firstName);
        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // POST /api/client ------------------------------------------------------------------------

    @Test
    void saveClient_noToken_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/api/client").contentType("application/json").content(clientJson("A", "B")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void saveClient_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(post("/api/client")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(clientJson("New", "Client")))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveClient_asEmployee_returnsCreated() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(post("/api/client")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(clientJson("New", "Client")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.client.firstName").value("New"))
                .andExpect(jsonPath("$.client.lastName").value("Client"))
                .andExpect(jsonPath("$.login").isNotEmpty())
                .andExpect(jsonPath("$.generatedPassword").isNotEmpty());
    }

    @Test
    void saveClient_asAdmin_returnsCreated() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(post("/api/client")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(clientJson("Another", "Person")))
                .andExpect(status().isCreated());
    }

    @Test
    void saveClient_missingFirstName_returnsBadRequest() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");
        String body = clientJson(null, "X");

        mockMvc.perform(post("/api/client")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // PUT /api/client/{id} --------------------------------------------------------------------

    @Test
    void updateClient_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(put("/api/client/10")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(clientJson("A", "B")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateClient_asEmployee_returnsNoContent() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(put("/api/client/10")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(clientJson("Updated", "Name")))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateClient_missingClient_returnsNotFound() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(put("/api/client/9999")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(clientJson("Ghost", "Client")))
                .andExpect(status().isNotFound());
    }

    // GET /api/client/profile -----------------------------------------------------------------

    @Test
    void findClientProfile_asAuthenticated_returnsProfile() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        linkUserAccountToClient(1L, 10L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/client/profile")
                        .param("userAccountId", "1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Anderson"));
    }

    @Test
    void findClientProfile_notLinked_returnsNotFound() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/client/profile")
                        .param("userAccountId", "1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no Client profile for userAccountId: 1"));
    }

    // GET /api/client/{id}/profile ------------------------------------------------------------

    @Test
    void findClientProfileById_asEmployee_returnsProfile() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        linkUserAccountToClient(1L, 10L);
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/client/10/profile")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(10))
                .andExpect(jsonPath("$.userAccountId").value(1))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Anderson"))
                .andExpect(jsonPath("$.login").value("alice"))
                .andExpect(jsonPath("$.identificationNumber").value("ID001"));
    }

    @Test
    void findClientProfileById_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        linkUserAccountToClient(1L, 10L);
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/client/10/profile")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findClientProfileById_missing_returnsNotFound() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/client/999/profile")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no Client profile for id: 999"));
    }

    // GET /api/client/{id} --------------------------------------------------------------------

    @Test
    void findById_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/client/10").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findById_asEmployee_returnsClient() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/client/10").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void findById_missingClient_returnsNotFound() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/client/999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no Client with provided id:999"));
    }

    // GET /api/client -------------------------------------------------------------------------

    @Test
    void findAllClients_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/client").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAllClients_asEmployee_returnsList() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertClient(10L, "Alice", "A", "Warsaw", "Main", "1", "ID001");
        insertClient(20L, "Carl", "C", "Warsaw", "Main", "2", "ID002");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/client").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // DELETE /api/client/{id} -----------------------------------------------------------------

    @Test
    void deleteById_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(delete("/api/client/10").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteById_asEmployee_returnsNoContent() throws Exception {
        // Create a client with linked user_account via the POST endpoint so soft-delete works.
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");
        // Save via API so link row exists.
        String saveBody = clientJson("Del", "Target");
        String response = mockMvc.perform(post("/api/client")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(saveBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(response, Map.class);
        Map<?, ?> clientNode = (Map<?, ?>) responseMap.get("client");
        Long id = ((Number) clientNode.get("id")).longValue();

        mockMvc.perform(delete("/api/client/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteById_missingLink_returnsNotFound() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(delete("/api/client/10").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no User Account linked to Client with id:10"));
    }
}
