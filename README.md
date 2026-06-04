# SimplyBank
A full-stack personal banking application built with Java Spring Boot, MySQL, and JavaScript/jQuery.
## About
SimplyBank is a portfolio project that simulates a real-world banking system with role-based access control, secure authentication, and financial transaction handling. It was built from scratch to demonstrate backend engineering skills including security, data integrity, and API design.
## Tech Stack
- **Backend:** Java, Spring Boot, Spring Security
- **Database:** MySQL with JdbcTemplate
- **Frontend:** JavaScript, jQuery, HTML/CSS, Thymeleaf
- **Authentication:** Stateless JWT (JwtUtil, JwtAuthFilter, SecurityConfig, AuthController)
- **Build Tool:** Maven
## Features
- **Role-Based Access Control** — Three user roles (CLIENT, EMPLOYEE, ADMIN) with per-role permissions and profile pages
- **JWT Authentication** — Stateless token-based auth with Spring Security integration via custom UserDetailsService
- **Secure Password Handling** — BCrypt hashing with `matches()` comparison and a gradual migration strategy from plaintext passwords
- **CSRF Protection** — Configured to protect against cross-site request forgery
- **Financial Transfers with Race Condition Prevention** — Pessimistic locking (SELECT FOR UPDATE) and atomic SQL updates to ensure data integrity during concurrent transactions
- **Full REST API** — Backend validation classes (TransactionValidation, BankAccountValidation, CurrencyValidation, PasswordValidation) mirrored by frontend AJAX service wrappers
- **Soft-Delete Pattern** — Records use `delete_date` fields instead of hard deletes
- **Optimized Queries** — LEFT JOINs to avoid N+1 query problems
- **12-Digit Account Numbers** — Randomly generated unique account numbers using SecureRandom
## Project Structure
```
src/main/
├── java/
│   └── ...
│       ├── controller/       # REST controllers & AuthController
│       ├── model/            # Entities (Client, UserAccount, BankAccount, Transaction)
│       ├── repository/       # JdbcTemplate repositories with RowMapper patterns
│       ├── service/          # Business logic layer
│       ├── security/         # JwtUtil, JwtAuthFilter, SecurityConfig
│       └── validation/       # Server-side validation classes
├── resources/
│   ├── static/               # JavaScript, CSS, jQuery frontend
│   └── templates/            # Thymeleaf templates (login, dashboard, transactions)
db/
└── ...                       # Database schema and SQL scripts
```
## Database
MySQL with JdbcTemplate for direct SQL control. Key design decisions:
- Manual junction table management for ManyToMany relationships (Client ↔ UserAccount)
- RowMapper patterns with enum parsing using `valueOf()` and a `Raw` suffix convention for unvalidated strings
- `BankAccountPair` record for paired return values
- `@PatchMapping` for soft-delete endpoints
## Roadmap
- [ ] **Write test suite** — Unit and integration tests for repositories, services, and controllers
- [ ] **Recreate with Hibernate/JPA** — After tests are complete, rebuild the data access layer using Hibernate/JPA to compare ORM vs. JdbcTemplate approaches and deepen ORM experience
## Running Locally
### Prerequisites
- Java 17+
- MySQL
- Maven
### Setup
1. Clone the repository
   ```bash
   git clone https://github.com/aleksanderWitek/SimplyBank.git
   cd SimplyBank
   ```
2. Set up the MySQL database using the scripts in the `db/` folder
3. Configure required environment variables (see [Configuration](#configuration))
4. Run the application
   ```bash
   ./mvnw spring-boot:run
   ```
## Configuration
Secrets are **not** committed to the repository — the application reads them from
environment variables and will refuse to start if they are missing. Copy `.env.example`
and provide real values:

| Variable | Purpose |
| --- | --- |
| `DB_USERNAME` | MySQL datasource username |
| `DB_PASSWORD` | MySQL datasource password |
| `JWT_SECRET` | JWT signing secret — must be at least 32 bytes and unique per environment |

Provide them in whichever way suits your workflow:
- Export them in your shell before `./mvnw spring-boot:run`, or
- Set them in your IDE run configuration, or
- Create a gitignored `src/main/resources/application-local.properties` (activated with
  `--spring.profiles.active=local`) holding the values for local development only.

> The JWT secret rejects the old hard-coded default at startup. Generate a fresh one,
> e.g. `openssl rand -base64 48`. Tests use a self-contained `application-test.properties`
> and need no environment setup.
## Author
**Aleksander Witek** — [GitHub](https://github.com/aleksanderWitek) · [LinkedIn](https://www.linkedin.com/in/aleksander-witek-dev/)
