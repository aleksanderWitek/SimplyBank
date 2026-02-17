# Code Review: Frontend View PR (Commits a36a44e, 5ef5daf)

## Files Reviewed
- `ApplicationController.java`
- `SecurityConfig.java`
- `dashboard.js` (941 lines)
- `new-transaction.js` (821 lines)
- `transactions.js` (568 lines)
- `dashboard.html`, `new-transaction.html`, `transactions.html`
- `dashboard.css`, `new-transaction.css`, `transactions.css`

---

## 1. BUG — Wrong Import in ApplicationController.java (Critical)

**File:** `ApplicationController.java:3`

```java
import ch.qos.logback.core.model.Model;  // WRONG — Logback's Model, not Spring MVC's
```

Should be:
```java
import org.springframework.ui.Model;
```

The `Model model` parameter is also never used in the `home()` method.

---

## 2. Unused Code

| Location | Issue |
|---|---|
| `ApplicationController.java:11` | `Model model` parameter never used |
| `dashboard.js:15-16` | `API.TRANSACTIONS_RECENT` and `API.ACCOUNTS_SUMMARY` — endpoints don't exist in backend |
| `dashboard.js:14` | `API.USER_ACCOUNT` / `UserAccountService` — defined but never called |
| `dashboard.js:19,21,22` | `BANK_ACCOUNT_TYPES`, `USER_ACCOUNT_ROLES`, `PASSWORD_MIN_LENGTH` — never used |
| `dashboard.js` Validation | ~20 validation methods defined but never called |
| `dashboard.js:449-569` | Entire `EmployeeService` — never called |
| `new-transaction.js:27` | `PASSWORD_MIN_LENGTH` — "kept for parity" but never used |
| `transactions.js:46-62` | `validateTransactionAccounts`, `validateAmount`, `validateSufficientBalance` — never called |

---

## 3. DRY Violations

Duplicated across all 3 JS files:
- `formatCurrency()` — 3 slightly different implementations
- `escapeHtml()` — 3 copies
- `capitalize()` — 3 copies
- `maskAccount()` / `maskAccountNumber()` — 3 copies with different names
- `ajax()` / `ajaxRequest()` — 3 copies with different names
- `notify()` / `showNotification()` — 3 copies with different timeout values and escaping behavior
- `Validation` module — 3 different versions
- `renderUserHeader()` — duplicated in new-transaction.js and transactions.js

**Fix:** Extract into a shared `common.js` loaded by all pages.

---

## 4. YAGNI Violations

- `dashboard.js` defines full CRUD services for Client, Employee, UserAccount, BankAccount, and Transfer — dashboard only needs to load and display data
- `dashboard.js` Validation mirrors the entire backend validation suite (password rules, employee validation, etc.) for a page that doesn't create/edit entities
- 500+ lines of unreachable code in `dashboard.js`

---

## 5. KISS Violations

- `configureStep2ForType()` in `new-transaction.js:377-435` has redundant label resets after the switch block
- `loadAccounts()` in `new-transaction.js:441-480` has 3-level nested `.fail()` callback pyramid

---

## 6. SOLID Violations

**SRP:** `dashboard.js` is a 941-line monolith handling validation, AJAX, notifications, formatting, 5 service modules, DOM rendering, navigation, and initialization.

---

## 7. Validation Correctness Issues in JS

| File | Line | Issue |
|---|---|---|
| `dashboard.js:231` | **XSS**: `showNotification` injects message directly into HTML without escaping |
| `dashboard.js:668` | **XSS via onclick**: `account.id` interpolated into onclick handler |
| `dashboard.js:151-155` | `validateAmount` doesn't check for NaN — `parseFloat("abc")` passes |
| `dashboard.js:146` | `validateTransactionAccounts` uses strict `===` — `1 === "1"` fails to catch same-account |
| `transactions.js:47-49` | Same strict `===` issue in `validateTransactionAccounts` |
| `new-transaction.js:625-626` | Dead code: checks `Validation.errors.amount` after validation passes |
| All 3 files | `const API` and `const Validation` at global scope — would conflict if loaded together |

---

## 8. API Endpoint Mismatches

| JS Endpoint | Backend Reality |
|---|---|
| `POST /api/transaction` | Backend has `/api/transaction/transfer`, `/deposit`, `/withdraw` |
| `GET /api/transactions/recent` | Does not exist |
| `GET /api/accounts/summary` | Does not exist |
| `GET /api/transaction/user/{id}` | Does not exist |
| `GET /api/auth/me` | Does not exist |

---

## 9. Security Concern

`SecurityConfig.java:41` — All security is disabled (`.anyRequest().permitAll()`), CSRF disabled, proper rules commented out. Fine for testing, must not reach production.

---

## Priority Actions

1. **Fix** wrong `Model` import in `ApplicationController.java`
2. **Fix** XSS in `dashboard.js` `showNotification()`
3. **Fix** API endpoint mismatches (JS vs backend)
4. **Extract** shared code into `common.js` (DRY)
5. **Remove** all unused services/validation/constants (YAGNI)
6. **Fix** type coercion in `validateTransactionAccounts`
7. **Fix** `validateAmount` NaN handling in `dashboard.js`
