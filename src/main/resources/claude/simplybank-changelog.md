# SimplyBank — Claude Change Log

Rolling log of changes made by Claude in this repo. Newest entry on top. Entries record **what file(s) were last touched and why** so a future session can pick up without re-deriving intent.

Branch: `claude/organize-resources-add-tests-GBdM2`

---

## Session 3 — management page modal & delete fixes (2026-04-28)

**Branch:** `claude/fix-modal-outside-click-byOlg`

**Goal:** Fix four UX/logic bugs on the `/management` page reported by the user.

**Issues addressed:**

1. Credentials modal (shown after creating a client/employee or resetting a password) was closing on outside click / ESC. It must only close via the explicit "Done" confirm button so admins don't accidentally lose unrecoverable credentials.
2. After editing, deleting, or resetting a password, the page kept stale form values. The page now reloads after a successful mutation (immediately for edit/delete, after the credentials modal is dismissed for create / password-reset).
3. Delete Client / Delete Employee did nothing after pressing the confirm button. Root cause: in the `#confirmModalOk` click handler, `closeConfirmModal()` was called first, and `closeConfirmModal()` itself was nulling out `pendingConfirmCallback` — so the subsequent `if (typeof pendingConfirmCallback === "function")` check was always false and the actual delete callback never ran.
4. The Delete confirmation modal only showed the entity ID. It now also shows the first + last name fetched via the existing GET endpoint, so the admin can sanity-check before destruction.

**Changed:**

- `src/main/resources/static/js/management.js`
  - `showCredentialsModal` / `closeCredentialsModal` — added a `reloadOnClose` flag; when true, `closeCredentialsModal` calls `window.location.reload()` after hiding the overlay.
  - `closeConfirmModal` — no longer mutates `pendingConfirmCallback`. The OK / Cancel / overlay / ESC handlers now manage that state themselves.
  - `$("#confirmModalOk")` click handler — captures the callback into a local before calling `closeConfirmModal()`, so the callback actually runs (this is the fix for "delete does nothing").
  - `deleteClient` / `deleteEmployee` — now first GET the entity, build a confirm message including `firstName + lastName`, then run the DELETE inside the confirm callback. On success, reloads the page after a short delay so the toast remains visible.
  - `submitEditClient` / `submitEditEmployee` / `deleteBankAccount` — reload after a successful mutation so all forms come back empty.
  - `submitClient` / `submitEmployee` / `resetPasswordFor` — pass `reloadOnClose=true` to `showCredentialsModal` so the page reloads only after the credentials are explicitly acknowledged.
  - Removed the credential-modal outside-click handler, ESC handler, and X close handler — leaving only the "Done" button as the dismiss path.
- `src/main/resources/templates/management.html`
  - Removed the X close button from the credentials modal header (the only dismiss path is now the "Done" button in the footer).
- `src/main/resources/claude/simplybank-project-architecture.md`
  - Added a "Static frontend (`src/main/resources/static`)" section so the JS / template structure is documented alongside the Java layout.
- `src/main/resources/claude/simplybank-changelog.md`
  - This entry.

**Not changed:** No backend code touched. The delete endpoints (`ClientController` / `EmployeeController` / their services) were already correct; the only delete bug was in the frontend confirm-modal callback wiring.

---

## Session 2 — resume test authoring (2026-04-15)

**Goal:** resume the approved plan `/root/.claude/plans/staged-spinning-cascade.md` after a compaction break.

**Status at resume:**

- Already done in session 1:
  - Moved 3 md rules files into `src/main/resources/claude/`.
  - Added jjwt 0.12.6, spring-security-test, mockito-junit-jupiter and JaCoCo 0.8.12 to `pom.xml`.
  - Created `src/main/java/com/alex/config/JwtService.java` — HMAC-SHA signed tokens; `generateToken`, `extractUsername`, `extractRole`, `isTokenValid`; key built in `@PostConstruct` from `jwt.secret`.
  - Created `src/main/java/com/alex/config/JwtAuthenticationFilter.java` — `OncePerRequestFilter` active only for `/api/**`; reads `Authorization: Bearer …`, resolves user via `UserAccountService.loadUserByUsername`, populates `SecurityContext`; falls through on missing/invalid header so session auth stays intact.
  - Edited `src/main/java/com/alex/config/SecurityConfig.java` — constructor-injected `JwtAuthenticationFilter`; registered `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)` after the existing rate-limit filter. Form login / session management / authorize rules unchanged.
  - Edited `src/main/resources/application.properties` — added `jwt.secret` and `jwt.expiration-ms`.
  - Fixed a pre-existing generics compile error at `src/main/java/com/alex/service/UserOwnershipService.java:53` by adding explicit `(Set<Long>)` cast on the `HashSet` branch so it matches the `Collections.emptySet()` branch.
  - Created `src/test/resources/application-test.properties` — test profile pointing at MySQL `simplybank_test`.
  - Created `src/test/resources/create-test-db.sql` — one-off local setup (CREATE DATABASE + CREATE USER + GRANT).
  - Created `src/test/resources/schema-test.sql` — mirrors `db/simply_bank_db_script.sql` exactly using `CREATE TABLE IF NOT EXISTS`, FK-safe order.
  - Created empty test package dirs: `src/test/java/com/alex/{config,controller,repository/mapper,service/validation}`.

- Sandbox gotchas encountered:
  - Maven DNS fails inside this sandbox; outbound goes through `$GLOBAL_AGENT_HTTP_PROXY`. Written `~/.m2/settings.xml` with proxy creds.
  - Default Maven resolver returns 407 through the auth proxy — must pass `-Dmaven.resolver.transport=wagon`. Helper wrapper at `/tmp/mvnrun.sh`.
  - `mysql` binary not installed in sandbox → integration tests can only be run on the user's own machine after `create-test-db.sql` has been executed against local MySQL.

- Plan still outstanding after resume:
  1. Create `src/test/java/com/alex/BaseIntegrationTest.java` (autowires MockMvc / JdbcTemplate / PasswordEncoder / JwtService; helper methods for token generation and DB fixture inserts).
  2. **Unit tests** (27 files):
     - `service/validation/` (8)
     - `repository/mapper/` (7)
     - `service/` (9) — simple first (`UserAccountProcessingServiceTest`, `LoginAttemptServiceTest`, `CleanupServiceTest`), then `BankAccountServiceTest`, `ClientServiceTest`, `EmployeeServiceTest`, `TransactionServiceTest`, `UserAccountServiceTest`, `UserOwnershipServiceTest`
     - `config/` (5) — 3 existing handlers + `JwtServiceTest` + `JwtAuthenticationFilterTest`
  3. **Integration tests** (7 files, extend `BaseIntegrationTest`):
     - `ApplicationControllerIntegrationTest`
     - `AuthControllerIntegrationTest`
     - `UserAccountControllerIntegrationTest`
     - `ClientControllerIntegrationTest`
     - `EmployeeControllerIntegrationTest`
     - `BankAccountControllerIntegrationTest`
     - `TransactionControllerIntegrationTest`
  4. Run `./mvnw clean test jacoco:report`; patch coverage gaps until 100% line coverage is hit.
  5. Commit and push to `claude/organize-resources-add-tests-GBdM2`. **No PR unless the user explicitly asks.**

- New in session 2:
  - Created `src/main/resources/claude/simplybank-project-architecture.md` — one-page map of every class & package (requested by user).
  - Created `src/main/resources/claude/simplybank-changelog.md` — this file (requested by user).
  - Next edit will be `src/test/java/com/alex/BaseIntegrationTest.java`.

## How future sessions should update this file

When you end a batch of edits, append a new `## Session N — <short title> (<date>)` block at the **top** of the file (below the header). Inside it:
- `Goal:` one-liner.
- `Changed:` bullet list of **every file you wrote or edited**, each with a one-sentence why.
- `Why this matters:` optional context a future Claude needs.
- `Still pending:` explicit punch list of unfinished plan items.

Keep entries terse — this is an orientation log, not a commit message.
