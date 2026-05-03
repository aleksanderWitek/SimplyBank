package com.alex.controller;

import com.alex.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAccountControllerIntegrationTest extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // GET /api/user_account/{id} -------------------------------------------------------------

    @Test
    void findById_noToken_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/user_account/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void findById_asOwner_returnsAccount() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/user_account/1").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("alice"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void findById_asOtherClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertUserAccount(2L, "bob", "Password1!", "CLIENT");
        String token = generateToken("bob", "CLIENT");

        mockMvc.perform(get("/api/user_account/1").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only view your own account"));
    }

    @Test
    void findById_asAdmin_canViewAnyAccount() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/user_account/1").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("alice"));
    }

    @Test
    void findById_asAdminButAccountMissing_returnsNotFound() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/user_account/999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no User with provided id:999"));
    }

    // GET /api/user_account (list, ADMIN-only) -----------------------------------------------

    @Test
    void findAll_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/user_account").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAll_asEmployee_isForbidden() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/user_account").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAll_asAdmin_returnsList() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/user_account").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // PUT /api/user_account/{id}/password ---------------------------------------------------

    @Test
    void updatePassword_asOwner_succeeds() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "currentPassword", "Password1!", "newPassword", "NewPassw0rd!"));

        mockMvc.perform(put("/api/user_account/1/password")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void updatePassword_asOtherClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertUserAccount(2L, "bob", "Password1!", "CLIENT");
        String token = generateToken("bob", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "currentPassword", "x", "newPassword", "NewPassw0rd!"));

        mockMvc.perform(put("/api/user_account/1/password")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only change your own password"));
    }

    @Test
    void updatePassword_wrongCurrentPassword_returnsBadRequest() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "currentPassword", "WrongPass1!", "newPassword", "NewPassw0rd!"));

        mockMvc.perform(put("/api/user_account/1/password")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePassword_newPasswordDoesNotMeetRequirements_returnsBadRequest() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "currentPassword", "Password1!", "newPassword", "short"));

        mockMvc.perform(put("/api/user_account/1/password")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePassword_sameAsCurrent_returnsBadRequest() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");
        String body = objectMapper.writeValueAsString(Map.of(
                "currentPassword", "Password1!", "newPassword", "Password1!"));

        mockMvc.perform(put("/api/user_account/1/password")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePassword_asAdminForOtherUser_succeeds() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");
        // Admin doesn't know alice's password, so wrong current should still 400 on validation
        String body = objectMapper.writeValueAsString(Map.of(
                "currentPassword", "x", "newPassword", "NewPassw0rd!"));

        mockMvc.perform(put("/api/user_account/1/password")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest()); // reached the service, password mismatch
    }

    // POST /api/user_account/{id}/password/reset (admin-only) -------------------------------

    @Test
    void resetPassword_asAdmin_returnsNewPassword() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(post("/api/user_account/1/password/reset")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userAccountId").value(1))
                .andExpect(jsonPath("$.login").value("alice"))
                .andExpect(jsonPath("$.newPassword").isNotEmpty());
    }

    @Test
    void resetPassword_asEmployee_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(post("/api/user_account/1/password/reset")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void resetPassword_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertUserAccount(2L, "bob", "Password1!", "CLIENT");
        String token = generateToken("bob", "CLIENT");

        mockMvc.perform(post("/api/user_account/1/password/reset")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void resetPassword_noToken_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/api/user_account/1/password/reset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void resetPassword_missingUser_returnsNotFound() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(post("/api/user_account/999/password/reset")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no User Account with provided id:999"));
    }
}
