# SimplyBank — Integration Testing Rules

## Scope

Integration tests verify that **multiple layers work together** — controller → service → repository → database. They boot a Spring context and hit a real MySQL database.

---

## Tech Stack

- **`@SpringBootTest`** with **`@AutoConfigureMockMvc`**
- **MockMvc** for HTTP layer testing (never use `RestTemplate`/`WebTestClient` unless testing a separate running server)
- **Dedicated MySQL test database** (`simplybank_test`) — same engine, same settings as production
- Project uses **JdbcTemplate** — there is NO JPA. Never use `@DataJpaTest`.
- Project does NOT use Lombok.

---

## Core Principles

1. **Work on real data.** Never mock the repository or JdbcTemplate in integration tests. Insert real rows into MySQL, execute the real SQL, read back real results.
2. **Each test creates its own data.** Use `@BeforeEach` or helper methods to `INSERT` the exact rows each test needs. Never rely on data left behind by another test.
3. **Clean up after yourself.** Use `@Transactional` on the test class so each test rolls back automatically, OR truncate tables in `@AfterEach`. `@Transactional` rollback is preferred — it's simpler and faster.
4. **Test through HTTP.** Use `MockMvc` to send real JSON requests to controller endpoints. Assert on HTTP status, response body, and headers.
5. **Test the full security chain.** JWT generation, RBAC, BCrypt — all real. Create users with real roles and real hashed passwords.

---

## Structure Template

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Insert real data — user, account, etc.
        jdbcTemplate.update(
            "INSERT INTO users (id, username, password, role, deleted) VALUES (?, ?, ?, ?, ?)",
            1L, "alex", "$2a$10$hashedPassword...", "CLIENT", false
        );
        jdbcTemplate.update(
            "INSERT INTO accounts (id, user_id, balance, deleted) VALUES (?, ?, ?, ?)",
            1L, 1L, new BigDecimal("1000.00"), false
        );

        // Generate a real JWT for this test user
        jwtToken = generateTestJwt("alex", "CLIENT");
    }

    @Test
    void getAccount_asOwner_returns200WithBalance() throws Exception {
        mockMvc.perform(get("/api/accounts/1")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void getAccount_asDifferentClient_returns403() throws Exception {
        String otherToken = generateTestJwt("other", "CLIENT");

        mockMvc.perform(get("/api/accounts/1")
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isForbidden());
    }
}
```

---

## Rules Checklist

| # | Rule | Why |
|---|------|-----|
| 1 | `@SpringBootTest` + `@AutoConfigureMockMvc` — never `@DataJpaTest` | Full context, JdbcTemplate, no JPA |
| 2 | `@ActiveProfiles("test")` pointing at MySQL `simplybank_test` DB | Same engine as prod, no H2 surprises |
| 3 | `@Transactional` on test class for automatic rollback | Clean slate per test |
| 4 | Insert data with `JdbcTemplate.update(...)` in `@BeforeEach` | Real data, explicit, no magic |
| 5 | Never mock repositories or services | The whole point is real wiring |
| 6 | Generate real JWTs with the app's own utility/service | Tests the actual security filter chain |
| 7 | Hash passwords with BCrypt when inserting test users | Spring Security will verify them for real |
| 8 | Assert HTTP status AND response body | Status alone hides wrong payloads |
| 9 | Test every RBAC combination: CLIENT, EMPLOYEE, ADMIN × each endpoint | RBAC is the most critical security layer |
| 10 | Test sad paths: 404 (not found), 403 (wrong role), 400 (bad input), 409 (conflicts) | Happy path alone gives false confidence |

---

## Data Setup Guidelines

- **Be explicit.** Write out every column — don't rely on defaults you might forget about.
- **Use helper methods.** If multiple tests need a user + account combo, extract `insertClientWithAccount(long userId, String username, BigDecimal balance)`.
- **Avoid `data.sql` or `import.sql` for test data.** It couples all tests to one shared dataset that becomes fragile. Each test owns its data.
- **ID strategy.** Use explicit IDs in test inserts. Don't rely on auto-generation so assertions are predictable.

---

## RBAC Testing Matrix

For every secured endpoint, test at minimum:

| Scenario | Expected |
|----------|----------|
| No token | 401 Unauthorized |
| Valid CLIENT accessing own resource | 200 OK |
| Valid CLIENT accessing other's resource | 403 Forbidden |
| Valid EMPLOYEE accessing client resource | 200 OK (or as per business rules) |
| Valid ADMIN | 200 OK |
| Soft-deleted user's token | 401 or 403 |

---

## Transaction / Transfer Tests

Since SimplyBank uses pessimistic locking for race condition prevention:

- Test a normal transfer: sender balance decreases, receiver balance increases, transaction record created.
- Test transfer with insufficient funds: expect 400/409, balances unchanged.
- Test transfer to a soft-deleted account: expect rejection.
- **Do NOT** write tests for `TransactionRepository.update()` — it is obsolete and pending removal.

---

## MySQL Test Database

Integration tests run against a **real MySQL instance** — the same engine and settings as production. No H2, no compatibility modes, no surprises.

### One-time setup: create the test database

Run `src/test/resources/create-test-db.sql` (see separate file) once on your local MySQL to create the `simplybank_test` database, user, and schema.

### Test Profile (`application-test.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/simplybank_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: simplybank_test
    password: test_password
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

- **Same driver, same dialect, same SQL** as the main `application.yml` — only the database name and credentials differ.
- `@Transactional` on test classes rolls back after each test, keeping the database clean without truncation scripts.
- If `@Transactional` rollback is not suitable for a specific test (e.g. testing commit behaviour), truncate tables in `@AfterEach` instead.

---

## What to Test in Integration Tests

- Full HTTP request → response cycles per endpoint.
- Security filter chain (JWT validation, role enforcement).
- Real SQL execution against the schema (catches typos, wrong column names).
- Transaction boundaries and rollback behaviour.
- Edge cases: concurrent access, soft-deleted entities, invalid input.

## What NOT to Test Here

- Pure business logic with no DB/HTTP involvement → unit tests.
- Third-party API calls → mock those even in integration tests (use `@MockBean` sparingly, only for external services).
