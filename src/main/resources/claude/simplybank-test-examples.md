# SimplyBank — Example Tests with Explanations

---

# PART 1: UNIT TESTS

Unit tests isolate a single class. Every dependency is mocked. No Spring context, no database.

---

## Unit Test 1 — Happy path: find account by ID

```java
@ExtendWith(MockitoExtension.class)                                     // [1]
class AccountServiceTest {

    @Mock                                                                // [2]
    private AccountRepository accountRepository;

    @InjectMocks                                                         // [3]
    private AccountService accountService;

    @Test
    void findById_accountExists_returnsAccount() {                       // [4]
        // GIVEN
        Account account = new Account(1L, 1L, new BigDecimal("500.00"), false);  // [5]
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));   // [6]

        // WHEN
        Account result = accountService.findById(1L);                    // [7]

        // THEN
        assertThat(result).isNotNull();                                  // [8]
        assertThat(result.getBalance()).isEqualByComparingTo("500.00");  // [9]
        verify(accountRepository).findById(1L);                          // [10]
        verifyNoMoreInteractions(accountRepository);                     // [11]
    }
}
```

| Line | What it does | Why |
|------|-------------|-----|
| [1] | `@ExtendWith(MockitoExtension.class)` — registers Mockito with JUnit 5. | Enables `@Mock` and `@InjectMocks` annotations. We do NOT use `@SpringBootTest` — this is a unit test, no Spring context needed. |
| [2] | `@Mock` — creates a fake `AccountRepository`. | The repository talks to the database via JdbcTemplate. We don't want a database here — we want to test only the service logic. |
| [3] | `@InjectMocks` — creates a real `AccountService` and injects the mock into it. | This is the class under test. It's real, not mocked. Mockito wires the mock repository into it automatically via constructor injection. |
| [4] | Test method name: `findById_accountExists_returnsAccount`. | Convention: `methodUnderTest_scenario_expectedResult`. Anyone reading the test name knows exactly what it covers. |
| [5] | Create a plain Java `Account` object with known values. | This is our test fixture. No database, no SQL — just a constructor call. We control every field. |
| [6] | `when(...).thenReturn(...)` — tell the mock what to return when `findById(1L)` is called. | This is a **stub**. It defines the scenario: "when the service asks the repo for account 1, hand back this object." |
| [7] | Call the real service method. | This is the **action**. The service calls `accountRepository.findById(1L)` internally, which hits our stub. |
| [8] | Assert the result is not null. | Basic sanity check — the service should not return null for an existing account. |
| [9] | `isEqualByComparingTo("500.00")` — compares BigDecimal values ignoring scale. | `BigDecimal("500.00").equals(BigDecimal("500"))` is false because of scale. `isEqualByComparingTo` avoids that trap. Always use this for money. |
| [10] | `verify(accountRepository).findById(1L)` — confirm the repo was called with exactly `1L`. | Proves the service actually delegated to the repo, not some hardcoded return. |
| [11] | `verifyNoMoreInteractions` — no other repo methods were called. | Catches accidental side effects. If someone adds an extra repo call in the service, this test breaks — which is what we want. |

---

## Unit Test 2 — Sad path: account not found throws exception

```java
@Test
void findById_accountDoesNotExist_throwsNotFoundException() {
    // GIVEN
    when(accountRepository.findById(99L)).thenReturn(Optional.empty());          // [1]

    // WHEN & THEN
    assertThatThrownBy(() -> accountService.findById(99L))                       // [2]
        .isInstanceOf(ResourceNotFoundException.class)                           // [3]
        .hasMessageContaining("Account")                                         // [4]
        .hasMessageContaining("99");                                             // [5]

    verify(accountRepository).findById(99L);                                     // [6]
}
```

| Line | What it does | Why |
|------|-------------|-----|
| [1] | Stub returns `Optional.empty()` — simulates "no such account in the database." | We're testing the sad path. The service must handle this gracefully, not return null or crash with `NoSuchElementException`. |
| [2] | `assertThatThrownBy(lambda)` — executes the lambda and expects it to throw. | AssertJ's way of testing exceptions. Cleaner than JUnit's `assertThrows` because it lets you chain assertions on the exception itself. |
| [3] | `.isInstanceOf(ResourceNotFoundException.class)` — checks the exception type. | The service should throw a domain-specific exception, not a generic `RuntimeException`. This ensures our error handling is intentional. |
| [4]-[5] | `.hasMessageContaining(...)` — checks the error message includes useful info. | When this error reaches the controller, it becomes a 404 response. The message should tell the caller *what* was not found and *which ID*, for debugging. |
| [6] | Verify the repo was still called. | Even though it threw, we confirm the service tried the lookup. This catches bugs where the service might throw *before* even checking the repo. |

---

## Unit Test 3 — Soft delete sets flag, does not hard-delete

```java
@Test
void deleteAccount_existingAccount_setsSoftDeleteFlag() {
    // GIVEN
    Account account = new Account(1L, 1L, BigDecimal.ZERO, false);              // [1]
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));       // [2]

    // WHEN
    accountService.deleteAccount(1L);                                            // [3]

    // THEN
    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);     // [4]
    verify(accountRepository).save(captor.capture());                            // [5]
    Account saved = captor.getValue();                                           // [6]

    assertThat(saved.isDeleted()).isTrue();                                      // [7]
    assertThat(saved.getId()).isEqualTo(1L);                                     // [8]
    verify(accountRepository, never()).delete(any());                            // [9]
}
```

| Line | What it does | Why |
|------|-------------|-----|
| [1] | Account with `deleted = false`. | Starting state: the account is active. |
| [2] | Stub the lookup so the service finds it. | Without this, the service would throw "not found" and we'd be testing the wrong path. |
| [3] | Call the delete method on the service. | The **action** — this should soft-delete, not hard-delete. |
| [4] | `ArgumentCaptor` — captures the argument passed to `accountRepository.save(...)`. | We need to inspect *what* the service saved. We can't just verify `save` was called — we need to see the object it saved. |
| [5] | `verify(...).save(captor.capture())` — asserts `save` was called AND captures its argument. | Two birds: it proves the service called save, and gives us the exact object to inspect. |
| [6] | Extract the captured value. | Now we have the Account object the service passed to `save`. |
| [7] | `assertThat(saved.isDeleted()).isTrue()` — the deleted flag must be `true`. | **The core assertion.** SimplyBank uses soft deletes. The service must flip this flag, not remove the row. |
| [8] | Confirm the ID is unchanged. | Sanity: make sure the service didn't accidentally create a new account or mix up IDs. |
| [9] | `verify(accountRepository, never()).delete(any())` — no hard delete method was called. | **Critical safety net.** If someone refactors the service and accidentally calls a hard delete, this test catches it immediately. |

---

## Unit Test 4 — Transfer with insufficient funds is rejected

```java
@Test
void transfer_insufficientFunds_throwsException() {
    // GIVEN
    Account sender = new Account(1L, 1L, new BigDecimal("50.00"), false);        // [1]
    Account receiver = new Account(2L, 2L, new BigDecimal("200.00"), false);     // [2]
    when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));        // [3]
    when(accountRepository.findById(2L)).thenReturn(Optional.of(receiver));

    BigDecimal transferAmount = new BigDecimal("100.00");                        // [4]

    // WHEN & THEN
    assertThatThrownBy(() ->
        accountService.transfer(1L, 2L, transferAmount))                         // [5]
        .isInstanceOf(InsufficientFundsException.class);

    verify(accountRepository, never()).save(any());                              // [6]
    verify(accountRepository, never()).update(any());                            // [7]
}
```

| Line | What it does | Why |
|------|-------------|-----|
| [1] | Sender has balance of 50.00. | Deliberately less than the transfer amount — this sets up the failure scenario. |
| [2] | Receiver with 200.00. | We need both accounts to exist so the service doesn't fail on "not found" before reaching the balance check. |
| [3] | Stub both lookups. | The service fetches both sender and receiver before attempting the transfer. |
| [4] | Transfer amount: 100.00 — more than sender's 50.00. | This is the trigger for the exception. |
| [5] | Expect `InsufficientFundsException`. | The service must validate funds *before* modifying any data. If it throws a generic exception, this test fails — good, because the controller needs specific exception types for proper HTTP error codes. |
| [6] | `verify(..., never()).save(any())` — nothing was persisted. | **Critical.** If the transfer fails, neither account should be modified. This ensures atomicity at the service level. |
| [7] | Also verify no `update` was called. | Belt and suspenders. No data mutation of any kind should happen on a failed transfer. |

---

---

# PART 2: INTEGRATION TESTS

Integration tests boot the full Spring context, hit real MySQL, and test through HTTP with MockMvc.

---

## Integration Test 1 — Client retrieves own account

```java
@SpringBootTest                                                                  // [1]
@AutoConfigureMockMvc                                                            // [2]
@ActiveProfiles("test")                                                          // [3]
@Transactional                                                                   // [4]
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;                                                     // [5]

    @Autowired
    private JdbcTemplate jdbcTemplate;                                           // [6]

    @Autowired
    private JwtService jwtService;                                               // [7]

    @Autowired
    private PasswordEncoder passwordEncoder;                                     // [8]

    private String clientToken;

    @BeforeEach
    void setUp() {
        String hashedPassword = passwordEncoder.encode("password123");           // [9]

        jdbcTemplate.update(
            "INSERT INTO users (id, username, password, role, deleted) VALUES (?, ?, ?, ?, ?)",
            1L, "alex", hashedPassword, "CLIENT", false                          // [10]
        );

        jdbcTemplate.update(
            "INSERT INTO accounts (id, user_id, balance, deleted) VALUES (?, ?, ?, ?)",
            1L, 1L, new BigDecimal("1500.00"), false                             // [11]
        );

        clientToken = jwtService.generateToken("alex", "CLIENT");                // [12]
    }

    @Test
    void getAccount_asOwner_returns200WithBalance() throws Exception {
        mockMvc.perform(get("/api/accounts/1")                                   // [13]
                .header("Authorization", "Bearer " + clientToken))               // [14]
            .andExpect(status().isOk())                                          // [15]
            .andExpect(jsonPath("$.balance").value(1500.00))                      // [16]
            .andExpect(jsonPath("$.deleted").value(false));                       // [17]
    }
}
```

| Line | What it does | Why |
|------|-------------|-----|
| [1] | `@SpringBootTest` — boots the full Spring application context. | Everything is real: controllers, services, repositories, security filters, JdbcTemplate. This is the opposite of a unit test. |
| [2] | `@AutoConfigureMockMvc` — gives us a `MockMvc` instance wired into the context. | MockMvc sends HTTP requests to the controllers without starting a real HTTP server. Fast, but still goes through the full filter chain. |
| [3] | `@ActiveProfiles("test")` — loads `application-test.yml`. | Points to the `simplybank_test` MySQL database. Same engine, same SQL dialect as production. |
| [4] | `@Transactional` — wraps each test in a transaction that **rolls back** after the test. | The data we insert in `@BeforeEach` disappears after the test. Next test starts with a clean database. No leftover data, no test ordering issues. |
| [5] | `MockMvc` — the HTTP client for our tests. | We use it to perform GET, POST, PUT, DELETE and assert on status codes, headers, and JSON bodies. |
| [6] | `JdbcTemplate` — the same one the app uses. | We use it to insert test data directly into MySQL. No mocking — real INSERT statements, real rows. |
| [7] | `JwtService` — the app's own JWT generator. | We generate tokens the same way the app does. This tests the real security filter chain end-to-end. |
| [8] | `PasswordEncoder` — Spring's BCrypt encoder. | Passwords in the database must be hashed. We use the real encoder so that if a test ever needs to authenticate via login endpoint, it works. |
| [9] | Hash the password before inserting. | MySQL stores the hashed version. If we inserted plain text, the security chain would reject it during authentication. |
| [10] | Insert a real user row with explicit ID, username, role, and deleted flag. | **Real data in real MySQL.** We specify every column — no relying on defaults we might forget about. |
| [11] | Insert a real account linked to the user. | Balance of 1500.00, not soft-deleted. This is the account the test will fetch. |
| [12] | Generate a JWT for user "alex" with role CLIENT. | This token goes into the Authorization header. The security filter will validate it for real. |
| [13] | `mockMvc.perform(get("/api/accounts/1"))` — send a GET request to the endpoint. | This is the action. It goes through: security filter → controller → service → repository → MySQL → back up the chain. |
| [14] | `.header("Authorization", "Bearer " + clientToken)` — attach the JWT. | Without this, the security filter rejects the request with 401. |
| [15] | `.andExpect(status().isOk())` — assert HTTP 200. | The owner is allowed to see their own account. |
| [16] | `.andExpect(jsonPath("$.balance").value(1500.00))` — check the JSON body. | Proves the data round-tripped through MySQL correctly. The balance we inserted is the balance we get back. |
| [17] | Assert `deleted` is false. | Confirms the response includes the soft-delete flag and it matches what we inserted. |

---

## Integration Test 2 — Client cannot access another client's account (RBAC)

```java
@Test
void getAccount_asDifferentClient_returns403() throws Exception {
    // Insert a second user who does NOT own account 1
    String hashedPw = passwordEncoder.encode("password456");
    jdbcTemplate.update(
        "INSERT INTO users (id, username, password, role, deleted) VALUES (?, ?, ?, ?, ?)",
        2L, "bob", hashedPw, "CLIENT", false                                    // [1]
    );

    String bobToken = jwtService.generateToken("bob", "CLIENT");                 // [2]

    mockMvc.perform(get("/api/accounts/1")                                       // [3]
            .header("Authorization", "Bearer " + bobToken))                      // [4]
        .andExpect(status().isForbidden());                                      // [5]
}
```

| Line | What it does | Why |
|------|-------------|-----|
| [1] | Insert a second user "bob" with role CLIENT. | Bob exists but does NOT own account 1 (that belongs to "alex" from `@BeforeEach`). |
| [2] | Generate a JWT for bob. | Valid token, valid user, valid role — but wrong ownership. |
| [3]-[4] | Bob requests alex's account. | The request is authenticated (valid JWT) but should be **unauthorized** for this specific resource. |
| [5] | Expect 403 Forbidden. | **This is the core RBAC assertion.** A CLIENT can only see their own accounts. The security layer must enforce ownership checks, not just role checks. If this returns 200, you have a serious security bug. |

---

## Integration Test 3 — No token returns 401

```java
@Test
void getAccount_noToken_returns401() throws Exception {
    mockMvc.perform(get("/api/accounts/1"))                                      // [1]
        .andExpect(status().isUnauthorized());                                   // [2]
}
```

| Line | What it does | Why |
|------|-------------|-----|
| [1] | Send a request with NO Authorization header. | Simulates an anonymous/unauthenticated user. |
| [2] | Expect 401 Unauthorized. | The security filter must reject unauthenticated requests before they reach the controller. If this returns 200 or 403, your filter chain has a gap. This is the simplest security test but one of the most important. |

---

## Integration Test 4 — Successful transfer modifies both balances in MySQL

```java
@Test
void transfer_validAmount_updatesBothBalances() throws Exception {
    // GIVEN — second user + account
    String hashedPw = passwordEncoder.encode("pass");
    jdbcTemplate.update(
        "INSERT INTO users (id, username, password, role, deleted) VALUES (?, ?, ?, ?, ?)",
        2L, "bob", hashedPw, "CLIENT", false                                    // [1]
    );
    jdbcTemplate.update(
        "INSERT INTO accounts (id, user_id, balance, deleted) VALUES (?, ?, ?, ?)",
        2L, 2L, new BigDecimal("300.00"), false                                  // [2]
    );

    String transferJson = """
        {
            "fromAccountId": 1,
            "toAccountId": 2,
            "amount": 200.00
        }
        """;                                                                     // [3]

    // WHEN
    mockMvc.perform(post("/api/transactions/transfer")                           // [4]
            .header("Authorization", "Bearer " + clientToken)                    // [5]
            .contentType(MediaType.APPLICATION_JSON)                             // [6]
            .content(transferJson))                                              // [7]
        .andExpect(status().isOk());                                             // [8]

    // THEN — verify real data in MySQL
    BigDecimal senderBalance = jdbcTemplate.queryForObject(                      // [9]
        "SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, 1L
    );
    BigDecimal receiverBalance = jdbcTemplate.queryForObject(                    // [10]
        "SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, 2L
    );

    assertThat(senderBalance).isEqualByComparingTo("1300.00");                   // [11]
    assertThat(receiverBalance).isEqualByComparingTo("500.00");                  // [12]
}
```

| Line | What it does | Why |
|------|-------------|-----|
| [1] | Insert a second user "bob". | We need a recipient for the transfer. |
| [2] | Insert bob's account with balance 300.00. | Receiver starts at 300. After receiving 200, should be 500. |
| [3] | Build the JSON request body. | Text block for readability. Contains sender, receiver, and amount. |
| [4] | `post("/api/transactions/transfer")` — send a POST to the transfer endpoint. | This goes through the full chain: security → controller → service (with pessimistic locking) → repository → MySQL. |
| [5] | Use alex's token (CLIENT who owns account 1). | Alex is the sender. The security layer must verify that the authenticated user owns the source account. |
| [6] | `.contentType(MediaType.APPLICATION_JSON)` — tell the server we're sending JSON. | Without this, Spring may not deserialize the request body correctly and return 415 Unsupported Media Type. |
| [7] | `.content(transferJson)` — attach the JSON body. | The controller reads this, maps it to a DTO, and passes it to the service. |
| [8] | Expect 200 OK. | The transfer should succeed — alex has 1500, transferring 200. |
| [9]-[10] | **Query MySQL directly** to read the actual balances after the transfer. | **This is the key.** We don't trust the HTTP response body alone. We go straight to the database and check that the real rows were updated. This catches bugs where the response says "success" but the SQL was wrong. |
| [11] | Sender: 1500 - 200 = 1300.00. | `isEqualByComparingTo` for BigDecimal comparison (ignores scale differences). |
| [12] | Receiver: 300 + 200 = 500.00. | Both sides of the transfer are verified against real MySQL data. If pessimistic locking or transaction boundaries are broken, these assertions will catch it. |

---

## Summary — Unit vs Integration at a Glance

| Aspect | Unit Tests | Integration Tests |
|--------|-----------|-------------------|
| Spring context | None | Full (`@SpringBootTest`) |
| Database | None — plain Java objects | Real MySQL (`simplybank_test`) |
| Dependencies | All mocked with Mockito | All real, autowired by Spring |
| HTTP layer | Not involved | MockMvc through the full filter chain |
| Speed | Milliseconds per test | Slower (context boot + MySQL I/O) |
| What it catches | Logic bugs in one class | Wiring bugs, SQL bugs, security gaps, RBAC holes |
| Data setup | Constructor calls | `JdbcTemplate.update()` INSERT into MySQL |
| Cleanup | Nothing to clean (no state) | `@Transactional` rollback |
