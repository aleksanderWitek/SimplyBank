# SimplyBank — Unit Testing Rules

## Scope

Unit tests verify **a single class in isolation**. Every collaborator is mocked. No Spring context, no database, no HTTP layer.

---

## Tech Stack

- **JUnit 5** + **Mockito** (`@ExtendWith(MockitoExtension.class)`)
- **AssertJ** for assertions (`assertThat(...)`)
- Project uses **JdbcTemplate** — there is NO JPA. Never use `@DataJpaTest`.
- Project does NOT use Lombok. Write constructors/getters by hand or use records.

---

## Core Principles

1. **Mock the repository, not JdbcTemplate.** The repository encapsulates SQL — the service layer only sees repository methods.
2. **One behaviour per test.** Name tests: `methodName_stateUnderTest_expectedBehaviour`.
3. **No real data.** Unit tests create plain Java objects in the test itself — no database, no SQL, no schema.
4. **No Spring context.** Never use `@SpringBootTest`, `@AutoConfigureMockMvc`, or any `@…Test` slice annotation in unit tests.
5. **Fast.** Every unit test must run in milliseconds. If it's slow, something is wrong.

---

## Structure Template

```java
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void findById_accountExists_returnsAccount() {
        // GIVEN
        Account account = new Account(1L, "Alex", new BigDecimal("1000.00"), false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // WHEN
        Account result = accountService.findById(1L);

        // THEN
        assertThat(result.getName()).isEqualTo("Alex");
        verify(accountRepository).findById(1L);
    }
}
```

---

## Rules Checklist

| # | Rule | Why |
|---|------|-----|
| 1 | `@ExtendWith(MockitoExtension.class)` — never `@SpringBootTest` | No context = fast |
| 2 | `@Mock` on repositories, `@InjectMocks` on the service under test | Isolate the unit |
| 3 | Never mock the class under test | You'd be testing Mockito, not your code |
| 4 | Use `when(...).thenReturn(...)` for stubs, `verify(...)` for interaction checks | Separate stubs from verifications |
| 5 | Use `assertThrows` / `assertThatThrownBy` for exception paths | Every sad path needs a test |
| 6 | No static mocking unless absolutely unavoidable | Keep tests simple |
| 7 | Test each RBAC-sensitive service method for allowed **and** denied roles if auth logic lives in the service | RBAC bugs are security bugs |
| 8 | Avoid `any()` matchers when you know the exact argument | Specific matchers catch regressions |
| 9 | Use `@BeforeEach` for shared setup; don't repeat identical object creation across 10 tests | DRY, but keep it readable |
| 10 | Never access the filesystem, network, or clock without abstracting it first | Unit = zero side effects |

---

## What to Test in a Unit Test

- **Service layer logic** — calculations, validation, branching, exception mapping.
- **Mapper / converter classes** — DTO ↔ entity transformations.
- **Utility / helper classes** — formatting, parsing, etc.

## What NOT to Test Here

- SQL correctness → integration tests.
- HTTP request/response mapping → integration tests with MockMvc.
- Spring Security filter chain → integration tests.
- Full transaction flows across multiple services → integration tests.

---

## Soft Delete Reminder

SimplyBank uses soft deletes. When testing `delete` or `deactivate`, assert that the service calls the repository's soft-delete method — never a hard delete. Verify the returned/modified entity has `deleted = true` (or equivalent flag).

---

## `TransactionRepository.update()` — OBSOLETE

`TransactionRepository.update()` is deprecated and scheduled for removal. Do NOT write new tests for it. If you encounter it, skip it and flag it for cleanup.
