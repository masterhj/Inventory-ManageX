# Inventory ManageX API

Inventory ManageX is a Spring Boot REST API for managing products, inventory movements, suppliers, customers, sales, and operational reporting for a small retail business.

The application is designed as a backend service: it exposes a documented HTTP API, persists operational data in MySQL, and enforces role-based access through JWT authentication.

## Highlights

- Product catalogue with brands, categories, suppliers, active-state controls, and barcode lookup.
- Inventory movement tracking for entries, exits, adjustments, and sales.
- Sales and sale-item management with payment status and stock validation.
- Customer addresses, administrator accounts, users, and permission-based access.
- Low-stock alerts and reporting endpoints for revenue, sales, payment methods, and top products.
- Database versioning through Flyway and integration tests backed by Testcontainers.

## Technology

| Area | Choice |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 4 |
| API | Spring MVC and springdoc-openapi |
| Security | Spring Security, JWT, refresh tokens |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Migrations | Flyway |
| Build | Maven |
| Testing | JUnit 5, Rest Assured, Spring Security Test, Testcontainers |
| Containers | Docker and Docker Compose |

## Getting started

### Run with Docker Compose

Docker Compose starts the API and a MySQL 8 database. Create a `.env` file at the repository root; it is ignored by Git.

```env
DB_PASSWORD=change-me
JWT_SECRET=use-a-long-random-secret
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
```

Start the stack:

```bash
docker compose up --build
```

The API is then available at `http://localhost:8080`. MySQL is published locally on port `3307`; inside Compose the application connects to the `db` service on port `3306`.

### Run locally

Requirements: Java 21, Maven 3.9+, and a reachable MySQL 8 instance.

Set the required environment variables before starting the application:

```bash
export DB_URL='jdbc:mysql://localhost:3306/stock_db?useTimezone=true&serverTimezone=UTC'
export DB_USERNAME='root'
export DB_PASSWORD='change-me'
export JWT_SECRET='use-a-long-random-secret'
export JWT_EXPIRATION='3600000'
export JWT_REFRESH_EXPIRATION='604800000'

mvn spring-boot:run
```

Flyway applies the schema migrations during startup. The application uses `ddl-auto: validate`, so schema changes belong in `src/main/resources/db/migration`, not in automatic Hibernate DDL.

## API documentation

OpenAPI documentation is served by the application:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI document: `http://localhost:8080/api-docs`

Authentication endpoints are available under `/auth` for login, token refresh, and logout. For protected routes, send the JWT as a Bearer token:

```http
Authorization: Bearer <access-token>
```

## Main resources

| Resource | Base path |
|---|---|
| Authentication | `/auth` |
| Products | `/products` |
| Categories and brands | `/categories`, `/brands` |
| Suppliers | `/suppliers` |
| Customers and addresses | `/clients`, `/clients/{id}/address` |
| Administrators and users | `/administrators`, `/users` |
| Sales and items | `/sales`, `/sales/{saleId}/items` |
| Stock movements | `/stock-movements` |
| Alerts | `/alerts` |
| Analytics | `/analytics` |

Access is governed by `ROLE_OWNER`, `ROLE_ADMIN`, and `ROLE_OPERATOR`. The security configuration defines the allowed operations for each route group.

## Tests

The test suite uses Testcontainers to run MySQL-backed integration tests. Docker must be available before running:

```bash
mvn test
```

To run one controller test class:

```bash
mvn -Dtest=AdministratorControllerTest test
```

## Project layout

```text
src/main/java/br/com/system
├── config          # application, web, and OpenAPI configuration
├── controllers     # HTTP endpoints and API contracts
├── data/dto        # request and response payloads
├── model           # JPA entities
├── repository      # persistence access
├── security        # JWT and authorization configuration
└── services        # application use cases

src/main/resources/db/migration
└── Flyway SQL migrations
```

## Development notes

- Keep secrets and environment-specific values outside version control. `.env` and `application-local.yml` are already ignored.
- Add a Flyway migration for every database change.
- Use the generated OpenAPI documentation as the source of truth for endpoint payloads and response shapes.
- Run the test suite before opening a pull request.
