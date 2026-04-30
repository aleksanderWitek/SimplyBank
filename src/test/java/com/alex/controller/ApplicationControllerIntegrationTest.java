package com.alex.controller;

import com.alex.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

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
    @WithMockUser(username = "alice", roles = "CLIENT")
    void home_authenticated_returnsDashboardView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @WithMockUser(username = "alice", roles = "CLIENT")
    void homeAlias_authenticated_returnsDashboardView() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @WithMockUser(username = "alice", roles = "CLIENT")
    void transactions_authenticated_returnsTransactionsView() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"));
    }

    @Test
    @WithMockUser(username = "alice", roles = "CLIENT")
    void newTransaction_authenticated_returnsNewTransactionView() throws Exception {
        mockMvc.perform(get("/new-transaction"))
                .andExpect(status().isOk())
                .andExpect(view().name("new-transaction"));
    }

    @Test
    @WithMockUser(username = "alice", roles = "CLIENT")
    void accounts_authenticated_returnsAccountsView() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts"));
    }

    @Test
    @WithMockUser(username = "alice", roles = "CLIENT")
    void account_authenticated_returnsAccountView() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(view().name("account"));
    }

    @Test
    @WithMockUser(username = "alice", roles = "CLIENT")
    void userProfile_authenticated_returnsUserProfileView() throws Exception {
        mockMvc.perform(get("/user-profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile"));
    }

    // /management is role-gated ---------------------------------------------------------------

    @Test
    @WithMockUser(username = "alice", roles = "CLIENT")
    void management_asClient_isForbidden() throws Exception {
        mockMvc.perform(get("/management"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "bob", roles = "EMPLOYEE")
    void management_asEmployee_returnsManagementView() throws Exception {
        mockMvc.perform(get("/management"))
                .andExpect(status().isOk())
                .andExpect(view().name("management"));
    }

    @Test
    @WithMockUser(username = "carol", roles = "ADMIN")
    void management_asAdmin_returnsManagementView() throws Exception {
        mockMvc.perform(get("/management"))
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
