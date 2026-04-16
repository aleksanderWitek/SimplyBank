package com.alex.controller;

import com.alex.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void me_noToken_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void me_invalidToken_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer garbage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void me_asClient_returnsProfileWithFirstAndLastName() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        insertClient(10L, "Alice", "Anderson", "Warsaw", "Main", "1A", "ID001");
        linkUserAccountToClient(1L, 10L);

        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("alice"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Anderson"));
    }

    @Test
    void me_asClientWithoutProfile_returnsUserWithoutName() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("alice"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.firstName").doesNotExist());
    }

    @Test
    void me_asEmployee_returnsProfileWithFirstAndLastName() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertEmployee(20L, "Bob", "Brown");
        linkUserAccountToEmployee(2L, 20L);

        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.login").value("bob"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.firstName").value("Bob"))
                .andExpect(jsonPath("$.lastName").value("Brown"));
    }

    @Test
    void me_asAdmin_returnsCoreUserFields() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.login").value("carol"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

}
