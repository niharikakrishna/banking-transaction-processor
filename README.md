# Banking Transaction Processor

A Spring Boot REST API that simulates a simple banking system. It supports account creation, deposits, withdrawals, fund transfers, balance inquiries, and transaction history while ensuring transactional consistency and proper exception handling.

---

## Features

- Create a new bank account
- Deposit money into an account
- Withdraw money from an account
- Transfer funds between accounts
- Retrieve account balance
- View transaction history
- Global exception handling
- Input validation
- Unit tested using JUnit 5 and Mockito

---

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- H2 In-Memory Database
- Maven
- JUnit 5
- Mockito
- Lombok

---

## Project Structure

```
src
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
├── service
└── test
```

---

## API Endpoints

### Create Account

```
POST /accounts
```

Response

```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

### Deposit Money

```
POST /accounts/{accountId}/deposit
```

Request

```json
{
  "amount": 500
}
```

Response

```
204 No Content
```

---

### Withdraw Money

```
POST /accounts/{accountId}/withdraw
```

Request

```json
{
  "amount": 200
}
```

Response

```
204 No Content
```

---

### Transfer Funds

```
POST /accounts/transfer
```

Request

```json
{
  "fromAccount": "UUID",
  "toAccount": "UUID",
  "amount": 300
}
```

Response

```
204 No Content
```

---

### Get Account Balance

```
GET /accounts/{accountId}/balance
```

Response

```json
{
  "balance": 1300
}
```

---

### Get Transaction History

```
GET /accounts/{accountId}/transactions
```

Response

```json
[
  {
    "transactionId": "UUID",
    "transactionType": "DEPOSIT",
    "amount": 500,
    "timestamp": "2026-07-22T15:10:30"
  }
]
```

---

## Transaction Types

- DEPOSIT
- WITHDRAWAL
- TRANSFER_IN
- TRANSFER_OUT

---

## Exception Handling

The application returns meaningful HTTP status codes for common error scenarios.

| Exception | HTTP Status |
|-----------|-------------|
| AccountNotFoundException | 404 Not Found |
| InvalidAmountException | 400 Bad Request |
| InsufficientFundsException | 400 Bad Request |
| Any Other Exception | 500 Internal Server Error |

---

## Running the Application

Clone the repository

```bash
git clone <repository-url>
```

Navigate to the project

```bash
cd banking-transaction-processor
```

Run the application

```bash
mvn spring-boot:run
```

The application starts on

```
http://localhost:8080
```

---

## API Documentation

Swagger UI is available after starting the application.

```
http://localhost:8080/swagger-ui/index.html
```

---

## Running Tests

Run all unit tests

```bash
mvn test
```

Run tests with coverage from your IDE for detailed coverage metrics.

---

## Design Decisions

- UUID is used as the account identifier to avoid predictable IDs.
- BigDecimal is used for all monetary operations to prevent floating-point precision issues.
- Business logic resides in the service layer.
- Controllers are responsible only for request handling and response generation.
- Custom exceptions provide meaningful error messages and HTTP status codes.
- Database operations are wrapped in a transaction to ensure consistency during fund transfers.
- DTOs are used to decouple the REST API from persistence entities.

---

## Future Enhancements

- Account owner details
- Authentication and authorization using Spring Security
- Audit logging
- Pagination for transaction history
- Docker support
- PostgreSQL/MySQL integration
- Redis caching
- Integration tests using Testcontainers

---

## Author

**Niharika Krishna**

Java Backend Developer
