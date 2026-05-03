# SimplyBank — Project Architecture

Quick reference so a future session can orient itself without re-reading every file.

## Stack

- Java 21, Spring Boot 3.5.3 (Maven wrapper)
- Spring Web MVC + Thymeleaf (server-rendered pages) + Spring Security
- MySQL 8 via **JdbcTemplate only** (no JPA / Hibernate)
- BCrypt passwords, form login + HTTP sessions (Thymeleaf) **and** JWT (REST `/api/**`)
- JUnit 5 + Mockito + Spring Security Test + JaCoCo (tests only)

## Top-level layout

```
src/main/java/com/alex/
  SimplyBank.java              — @SpringBootApplication entry point
  BankAccountType / Currency
  TransactionType / UserAccountRole  — enums, see below
  config/                      — security, filters, handlers, JWT
  controller/                  — HTTP endpoints (Thymeleaf + REST)
  dto/                         — records & POJOs passed across layers
  exception/                   — 9 runtime exceptions + GlobalExceptionHandler
  repository/                  — JdbcTemplate repos + interfaces
  repository/mapper/           — RowMapper<T> implementations
  service/                     — business logic + interfaces
  service/validation/          — static validators that throw runtime exceptions
src/main/resources/
  application.properties       — DB, JWT, session config
  claude/                      — Claude-facing rules + architecture docs
  static/, templates/          — Thymeleaf assets
db/simply_bank_db_script.sql   — production schema (also source for schema-test.sql)
```

## Enums

| Enum | Values |
| --- | --- |
| `UserAccountRole` | `CLIENT`, `EMPLOYEE`, `ADMIN` |
| `BankAccountType` | `CHECKING`, `BUSINESS`, `SAVING`, `FOREIGN_CURRENCY` |
| `Currency` | `PLN`, `EUR`, `GBP`, `USD` |
| `TransactionType` | `TRANSFER`, `DEPOSIT`, `WITHDRAWAL` |

## Config (`com.alex.config`)

| Class | Role |
| --- | --- |
| `SecurityConfig` | Filter chain: form login for Thymeleaf, JWT filter for `/api/**`, rate-limit filter, password encoder, AuthenticationManager. |
| `LoginRateLimitFilter` | Per-username lockout after N failed logins (delegates to `LoginAttemptService`). |
| `CustomAuthenticationSuccessHandler` | Clears failed-attempt counter on success. |
| `CustomAuthenticationFailureHandler` | Increments failed-attempt counter, redirects with reason. |
| `JwtService` | HMAC-SHA signed JWTs — `generateToken`, `extractUsername`, `extractRole`, `isTokenValid`. Secret from `jwt.secret`. |
| `JwtAuthenticationFilter` | `OncePerRequestFilter`; only for `/api/**`. Parses `Authorization: Bearer …`, loads user via `UserAccountService.loadUserByUsername` and populates `SecurityContext`. Falls through on missing/invalid header so session auth still works for non-API paths. |

## Controllers (`com.alex.controller`)

| Controller | Purpose |
| --- | --- |
| `ApplicationController` | Thymeleaf pages: `/`, `/login`, `/management`, `/transactions`, `/accounts`, `/user-profile`. |
| `AuthController` | `GET /api/auth/me` — returns the current user's profile (client / employee / admin) based on role. |
| `ClientController` | `/api/client/**` — CRUD + profile + admin lookup by id. |
| `EmployeeController` | `/api/employee/**` — CRUD + employee self-profile + admin-profile. |
| `BankAccountController` | `/api/bank_account/**` — CRUD with ownership & soft-delete semantics. |
| `TransactionController` | `/api/transaction/**` — transfer / deposit / withdraw + finders (by from / to / between). |
| `UserAccountController` | `/api/user_account/**` — list (ADMIN), find, password change, soft-delete. |

## DTOs (`com.alex.dto`)

Plain POJOs used across layers. `UserAccount`, `Client`, `Employee`, `BankAccount`, `Transaction` are JDBC-row equivalents. `ClientProfile`, `EmployeeProfile`, `AdminProfile` are `/auth/me` view models. `Password`, `TransferRequest`, `DepositRequest`, `WithdrawRequest`, `SaveBankAccountRequest` are request payloads.

## Exceptions (`com.alex.exception`)

All extend `RuntimeException`. `GlobalExceptionHandler` maps each to the correct HTTP status (400/401/403/404/409/500).

| Exception | HTTP |
| --- | --- |
| `IllegalArgumentRuntimeException` | 400 |
| `NullPointerRuntimeException` | 400 |
| `SecurityRuntimeException` | 401 |
| `AccessDeniedRuntimeException` | 403 |
| `UserAccountNotFoundRuntimeException` | 404 |
| `ClientNotFoundRuntimeException` | 404 |
| `EmployeeNotFoundRuntimeException` | 404 |
| `BankAccountNotFoundRuntimeException` | 404 |
| `TransactionNotFoundRuntimeException` | 404 |
| `IllegalStateRuntimeException` | 409 |
| `SQLRuntimeException` / `DataAccessRuntimeException` | 500 |

## Repositories (`com.alex.repository`)

All extend `CommonJdbcRepository` which wraps `JdbcTemplate` calls and converts `DataAccessException` into `DataAccessRuntimeException`. Every domain has an `I<Name>Repository` + impl:

- `UserAccountRepository` — login lookup, save, soft delete, password update.
- `ClientRepository` / `EmployeeRepository` — CRUD with soft delete.
- `BankAccountRepository` — CRUD; `findByIdForUpdate` for pessimistic locking during transfers.
- `TransactionRepository` — insert + find by from / to / id / date range (**no update** — immutable).
- `UserAccountClientRepository`, `UserAccountEmployeeRepository`, `BankAccountClientRepository` — link tables joining user accounts to clients / employees / bank accounts.

## Row mappers (`com.alex.repository.mapper`)

Each maps one table (or join) to its DTO and re-throws `SQLException` as `SQLRuntimeException`. `TransactionRowMapper` joins bank_account twice (aliases `baf_*` / `bat_*`) and uses `rs.wasNull()` to allow null from-account (DEPOSIT) or null to-account (WITHDRAWAL).

## Services (`com.alex.service`)

| Service | Role |
| --- | --- |
| `UserAccountService` | `UserDetailsService` + CRUD + password change + login generation. Throws `UserAccountNotFoundRuntimeException`. |
| `UserAccountProcessingService` | Pure helpers — `generateLogin(first, last)` builds 3+3+8-UUID lowercase handle. |
| `ClientService` | Client CRUD; on `save()` creates client → creates linked UserAccount(CLIENT) → links them. |
| `EmployeeService` | Employee CRUD; on `save()` creates employee → creates linked UserAccount(EMPLOYEE) → links them. |
| `BankAccountService` | CRUD + 12-digit account number generator (SecureRandom, up to 100 attempts for uniqueness). |
| `TransactionService` | `transfer()` — pessimistic lock in lower-ID-first order, validate currency match + sufficient funds, insert two balance updates + one transaction row. `deposit()` / `withdraw()` — single account + transaction. |
| `UserOwnershipService` | Resolves current user from `Principal`; computes owned bank-account IDs for CLIENT role. Used by controllers for RBAC. |
| `LoginAttemptService` | In-memory attempt counter; blocks after N failures for M minutes. |
| `CleanupService` | Currently empty placeholder. |

## Validation (`com.alex.service.validation`)

All static methods. Throw `NullPointerRuntimeException` / `IllegalArgumentRuntimeException` / `IllegalStateRuntimeException` on violation.

| Class | Validates |
| --- | --- |
| `IdValidation` | id not null. |
| `ClientValidation` / `EmployeeValidation` | object not null. |
| `BankAccountValidation` | not null / account_type one of the enum values. |
| `CurrencyValidation` | not null / blank / one of PLN / EUR / GBP / USD. |
| `UserAccountValidation` | object not null, first/last name present, role not null. |
| `PasswordValidation` | length ≥ 8, has upper, lower, digit, special; `authenticate` via `PasswordEncoder`; reject reusing current password. |
| `TransactionValidation` | description ≤ 255, amount > 0, accounts differ on transfer, balance sufficient, currencies match. |

## Static frontend (`src/main/resources/static` + `templates`)

Server-rendered Thymeleaf shells with vanilla jQuery on top — no build step, no SPA framework.

| Template | Page JS | Purpose |
| --- | --- | --- |
| `login.html` | `login.js` | Form login (Spring Security). |
| `dashboard.html` | `dashboard.js` | Landing page after login. |
| `accounts.html` / `account.html` | `accounts.js` / `account.js` | List of bank accounts; single-account drilldown. |
| `transactions.html` | `transactions.js` | Transaction history view. |
| `new-transaction.html` | `new-transaction.js` | Transfer / deposit / withdraw form. |
| `user-profile.html` | `user-profile.js` | Profile + change password. |
| `management.html` | `management.js` | ADMIN-only CRUD console (Clients / Employees / Bank Accounts tabs + password reset). |

Shared helpers live in `static/js/common.js`: `ajax(url, method, data)` (jQuery `$.ajax` wrapper, JSON in/out), `escapeHtml`, `notify(msg, level)`, `formatDate`, `formatCurrency`, `maskAccount`, `renderUserHeader`, `initProfileLinks`. jQuery is bundled at `static/js/libs/jquery-3.6.0.min.js`.

Per-page CSS lives in `static/css/<page>.css`. The management page additionally defines two reusable modal patterns:

- **Credentials modal** (`#credentialModalOverlay`) — shown after a successful create-client / create-employee / password-reset. Closes only via the "Done" button (no overlay-click, no ESC, no X) because credentials are shown exactly once.
- **Confirm modal** (`#confirmModalOverlay`) — generic confirm-before-destructive-action prompt. Wired through `confirmAction(title, message, callback)` + a single `pendingConfirmCallback` slot. Closes via OK (runs callback then reloads), Cancel, X, ESC, or overlay click (latter four cancel the action).

## Schema (`db/simply_bank_db_script.sql`)

Tables and FK order: `user_account`, `client`, `employee`, `user_account_client`, `user_account_employee`, `bank_account`, `bank_account_client`, `transaction`. Every table carries `create_date` / `modify_date` / `delete_date` for soft delete (except `transaction`, which is immutable).

## Tests layout

```
src/test/java/com/alex/
  BaseIntegrationTest.java            — abstract @SpringBootTest base (MockMvc + JdbcTemplate + JwtService)
  config/, controller/, repository/mapper/, service/, service/validation/
src/test/resources/
  application-test.properties         — spring profile "test", MySQL `simplybank_test`
  schema-test.sql                     — mirrors production schema, CREATE TABLE IF NOT EXISTS
  create-test-db.sql                  — one-off local setup: creates DB + user + grants
```

Integration tests expect a local MySQL with `simplybank_test` DB already created (run `create-test-db.sql` once as root). Unit tests require no DB.

## Runtime properties

- `jwt.secret`, `jwt.expiration-ms` — JWT signing & TTL. Different values for main vs test.
- `spring.datasource.*` — MySQL URL / user / password.
- `server.servlet.session.timeout=15m` — form-login sessions.
