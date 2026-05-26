# WalletWise - Personal Finance Manager

WalletWise is a Spring Boot 3 personal finance management API built for the company assignment. It supports session-based authentication, transaction CRUD, default and custom categories, savings goals, and monthly/yearly reports.

## Unique improvements added

- Custom WalletWise branding and landing page at `/`
- H2 database fallback for one-click free deployment
- Optional MySQL support through environment variables
- Session-based Spring Security authentication
- BCrypt password hashing
- BigDecimal money handling instead of floating point
- Recurring transaction metadata and notes fields
- Global exception handling with clean 400/401/403/404/409 responses
- Unit test structure with JaCoCo coverage plugin

## Tech stack

- Java 17
- Spring Boot 3.3.7
- Spring Security
- Spring Data JPA
- H2 by default, MySQL optional
- Maven
- JUnit 5 and Mockito

## Main assignment endpoints

```text
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/logout
GET    /api/categories
POST   /api/categories
DELETE /api/categories/{name}
GET    /api/transactions
POST   /api/transactions
PUT    /api/transactions/{id}
DELETE /api/transactions/{id}
POST   /api/goals
GET    /api/goals
GET    /api/goals/{id}
PUT    /api/goals/{id}
DELETE /api/goals/{id}
GET    /api/reports/monthly/{year}/{month}
GET    /api/reports/yearly/{year}
```

## Quick deploy

Read `DEPLOY.md` for beginner-friendly deployment steps.

Render settings:

```text
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/pfm-1.0.0.jar
Environment: JAVA_VERSION=17
```

Base API URL after deployment:

```text
https://YOUR_RENDER_APP.onrender.com/api
```

## Local run

If Maven is installed:

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080/
http://localhost:8080/health
http://localhost:8080/api
```

## Example API flow

Register:

```bash
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123","fullName":"John Doe","phoneNumber":"+1234567890"}'
```

Login:

```bash
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123"}'
```

Create transaction:

```bash
curl -b cookies.txt -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"amount":50000,"date":"2024-01-15","category":"Salary","description":"January Salary"}'
```
