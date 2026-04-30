package com.alex.controller;

import com.alex.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ApplicationControllerIntegrationTest extends BaseIntegrationTest {

    // /login is public -------------------------------------------------------------------------

    @Test
    void login_unauthenticated_returnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // Protected pages require auth -------------------------------------------------------------

    @Test
    void home_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void home_authenticated_returnsDashboardView() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    void homeAlias_authenticated_returnsDashboardView() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/home").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    void transactions_authenticated_returnsTransactionsView() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/transactions").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"));
    }

    @Test
    void newTransaction_authenticated_returnsNewTransactionView() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/new-transaction").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("new-transaction"));
    }

    @Test
    void accounts_authenticated_returnsAccountsView() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/accounts").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts"));
    }

    @Test
    void account_authenticated_returnsAccountView() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/account").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("account"));
    }

    @Test
    void userProfile_authenticated_returnsUserProfileView() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/user-profile").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile"));
    }

    // /management is role-gated ---------------------------------------------------------------

    @Test
    void management_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/management").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void management_asEmployee_returnsManagementView() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/management").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("management"));
    }

    @Test
    void management_asAdmin_returnsManagementView() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/management").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("management"));
    }

    @Test
    void management_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/management"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
