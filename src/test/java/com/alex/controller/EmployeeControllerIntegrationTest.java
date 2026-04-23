package com.alex.controller;

import com.alex.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeControllerIntegrationTest extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String employeeJson(String firstName, String lastName) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "firstName", firstName == null ? "" : firstName,
                "lastName", lastName == null ? "" : lastName));
    }

    // POST /api/employee ----------------------------------------------------------------------

    @Test
    void saveEmployee_noToken_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/api/employee").contentType("application/json").content("{}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void saveEmployee_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(post("/api/employee")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(employeeJson("E", "M")))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveEmployee_asEmployee_isForbidden() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(post("/api/employee")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(employeeJson("E", "M")))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveEmployee_asAdmin_returnsCreated() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(post("/api/employee")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(employeeJson("Eve", "Manager")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employee.firstName").value("Eve"))
                .andExpect(jsonPath("$.employee.lastName").value("Manager"))
                .andExpect(jsonPath("$.login").isNotEmpty())
                .andExpect(jsonPath("$.generatedPassword").isNotEmpty());
    }

    // PUT /api/employee/{id} ------------------------------------------------------------------

    @Test
    void updateEmployee_asAdmin_returnsNoContent() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        insertEmployee(20L, "Eve", "Manager");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(put("/api/employee/20")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(employeeJson("Updated", "Person")))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateEmployee_asEmployee_isForbidden() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertEmployee(20L, "Eve", "Manager");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(put("/api/employee/20")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(employeeJson("X", "Y")))
                .andExpect(status().isForbidden());
    }

    // GET /api/employee/{id} ------------------------------------------------------------------

    @Test
    void findById_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/employee/20").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findById_asAdmin_returnsEmployee() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        insertEmployee(20L, "Eve", "Manager");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/employee/20").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.firstName").value("Eve"));
    }

    @Test
    void findById_missing_returnsNotFound() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/employee/999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no Employee with provided id:999"));
    }

    // GET /api/employee list ------------------------------------------------------------------

    @Test
    void findAll_asAdmin_returnsList() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        insertEmployee(20L, "Eve", "Manager");
        insertEmployee(30L, "Ian", "Clerk");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/employee").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findAll_asEmployee_isForbidden() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/employee").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    // DELETE /api/employee/{id} ---------------------------------------------------------------

    @Test
    void deleteById_asAdmin_returnsNoContent() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        // Create via API so the user_account_employee link row exists for delete cascade.
        String body = mockMvc.perform(post("/api/employee")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(employeeJson("Del", "Target")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(body, Map.class);
        Map<?, ?> employeeNode = (Map<?, ?>) responseMap.get("employee");
        Long id = ((Number) employeeNode.get("id")).longValue();

        mockMvc.perform(delete("/api/employee/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteById_asEmployee_isForbidden() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertEmployee(20L, "Eve", "Manager");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(delete("/api/employee/20").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    // GET /api/employee/profile ---------------------------------------------------------------

    @Test
    void findEmployeeProfile_asEmployee_returnsProfile() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertEmployee(20L, "Eve", "Manager");
        linkUserAccountToEmployee(2L, 20L);
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/employee/profile")
                        .param("userAccountId", "2")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Eve"))
                .andExpect(jsonPath("$.lastName").value("Manager"));
    }

    @Test
    void findEmployeeProfile_asAdmin_returnsProfile() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        insertEmployee(20L, "Eve", "Manager");
        linkUserAccountToEmployee(2L, 20L);
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/employee/profile")
                        .param("userAccountId", "2")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Eve"));
    }

    @Test
    void findEmployeeProfile_asClient_isForbidden() throws Exception {
        insertUserAccount(1L, "alice", "Password1!", "CLIENT");
        String token = generateToken("alice", "CLIENT");

        mockMvc.perform(get("/api/employee/profile")
                        .param("userAccountId", "1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findEmployeeProfile_missing_returnsNotFound() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/employee/profile")
                        .param("userAccountId", "999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no Employee profile for userAccountId: 999"));
    }

    // GET /api/employee/admin-profile ---------------------------------------------------------

    @Test
    void findAdminProfile_asAdmin_returnsProfile() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/employee/admin-profile")
                        .param("userAccountId", "3")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.login").value("carol"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void findAdminProfile_asEmployee_isForbidden() throws Exception {
        insertUserAccount(2L, "bob", "Password1!", "EMPLOYEE");
        String token = generateToken("bob", "EMPLOYEE");

        mockMvc.perform(get("/api/employee/admin-profile")
                        .param("userAccountId", "2")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAdminProfile_missingUser_returnsNotFound() throws Exception {
        insertUserAccount(3L, "carol", "Password1!", "ADMIN");
        String token = generateToken("carol", "ADMIN");

        mockMvc.perform(get("/api/employee/admin-profile")
                        .param("userAccountId", "999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no User Account with provided id: 999"));
    }
}
