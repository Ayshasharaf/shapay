# ShaPay — Webhook Delivery Service

A payment webhook delivery platform built with Java and Spring Boot, focused on reliable event delivery, automatic retries, idempotency, and secure webhook communication.

ShaPay simulates how a payment provider notifies merchant applications when a payment succeeds.

## Overview

Imagine an online store receives a customer's RM150 payment.

Once the payment succeeds, ShaPay generates a `payment.succeeded` event and sends an HTTP POST request to the merchant's registered webhook endpoint.

But what happens if the merchant's server is unavailable?

ShaPay handles delivery failures through automatic retries, exponential backoff, delivery history, and manual replay.

**Payment processing and webhook delivery are treated as separate operations.** A failed notification does not change a successful payment into a failed payment.

> This project simulates payment notification infrastructure. It does not process or transfer real money.

## Features

- **Payment creation** — Create payment records and generate corresponding webhook events.
- **Idempotency** — Prevent duplicate payments when clients retry requests using the same idempotency key.
- **Transactional event creation** — Persist payment, webhook event, and delivery records within one database transaction.
- **HMAC-SHA256 signing** — Sign webhook payloads and verify signatures using a shared secret.
- **Automatic retries** — Retry unsuccessful webhook deliveries with exponential backoff.
- **Delivery tracking** — Store individual delivery attempts, HTTP response codes, response bodies, and execution durations.
- **Database-level job claiming** — Use PostgreSQL `FOR UPDATE SKIP LOCKED` to claim deliveries safely.
- **Dead-letter handling** — Mark deliveries as `FAILED` after reaching the maximum number of attempts.
- **Manual replay** — Replay failed deliveries while preserving their previous attempt history.
- **Stale delivery recovery** — Recover deliveries that remain in `PROCESSING` beyond a configured timeout.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend development |
| Spring Boot 4.2 | Application framework |
| Spring Data JPA | Database persistence |
| PostgreSQL 17 | Relational database |
| Flyway | Database migrations |
| Docker Compose | Local database environment |
| Maven | Dependency management |
| JUnit 5 | Testing |

## Architecture

The application follows a modular package structure:

```text
com.shapay
├── payment
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── webhook
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── delivery
│   ├── controller
│   ├── entity
│   ├── repository
│   ├── service
│   └── worker
│
└── common
    ├── config
    ├── exception
    └── service
```

### Delivery lifecycle

```text
Payment Created
      |
      v
Webhook Event Created
      |
      v
Delivery Created (PENDING)
      |
      v
Worker Claims Delivery
      |
      v
    PROCESSING
      |
      v
HTTP POST to Merchant
      |
      +------------------+
      |                  |
   Success             Failure
      |                  |
      v                  v
   SUCCESS          Retry Scheduled
                         |
                         v
                  Exponential Backoff
                         |
                         v
                   Retry Delivery
                         |
                         v
                 Maximum Attempts?
                    /       \
                  No         Yes
                  |           |
                  v           v
                Retry       FAILED
                              |
                              v
                         Manual Replay
```

## Engineering Concepts

### 1. Idempotency

Clients send an `Idempotency-Key` when creating payments.

ShaPay uses a database-level unique constraint and PostgreSQL's `INSERT ... ON CONFLICT DO NOTHING` to prevent duplicate payment creation, including concurrent requests.

The request hash is used to detect when the same key is reused with different payment details.

### 2. Transactional consistency

Payment creation, webhook event creation, and initial delivery creation happen within a single database transaction.

This prevents a payment from being committed without its corresponding webhook event and delivery record.

### 3. HMAC request signing

Every outgoing webhook includes an `X-ShaPay-Signature` header generated using HMAC-SHA256.

The receiving endpoint independently calculates and verifies the signature using the shared secret.

### 4. Automatic retries

Failed deliveries are retried using exponential backoff:

| Attempt | Retry delay |
|---|---:|
| 1 | 5 seconds |
| 2 | 10 seconds |
| 3 | 20 seconds |
| 4 | 40 seconds |
| 5 | Marked FAILED |

### 5. Concurrent delivery claiming

PostgreSQL's `FOR UPDATE SKIP LOCKED` allows workers to claim different delivery records without waiting on rows already locked by another worker.

This provides a foundation for concurrent background processing.

### 6. Manual replay and delivery history

Failed deliveries can be manually replayed through an API endpoint.

Previous delivery attempts are retained for auditing. Attempt numbers remain unique for each delivery, including across replays.

## Database Schema

| Table | Responsibility |
|---|---|
| `payments` | Stores payment records |
| `webhook_endpoints` | Stores merchant webhook endpoints and signing secrets |
| `webhook_endpoint_events` | Stores subscribed event types |
| `webhook_events` | Stores generated webhook events and payloads |
| `webhook_deliveries` | Tracks delivery status and retry scheduling |
| `delivery_attempts` | Stores individual HTTP delivery attempts |

Database schema changes are managed using Flyway migrations.

## Getting Started

### Prerequisites

- Java 21
- Docker Desktop
- Maven (optional, as the project includes Maven Wrapper)

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/shapay.git
cd shapay
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

### 3. Configure application.yml

Configure your PostgreSQL connection and local test merchant webhook secret.

Example:

```yaml
spring:
  application:
    name: shapay

  datasource:
    url: jdbc:postgresql://localhost:5432/shapay
    username: shapay_user
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
    locations: classpath:db/migration

test-merchant:
  webhook-secret: ${WEBHOOK_SECRET}

server:
  port: 8080
```

Set the required environment variables locally. Never commit real credentials or webhook secrets.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

Flyway automatically applies pending database migrations during startup.

### 5. Run tests

```bash
./mvnw clean test
```

## API Examples

### Create a payment

```bash
curl -i -X POST \
  "http://localhost:8080/api/payments?amount=150.00&currency=MYR" \
  -H "Idempotency-Key: payment-demo-001"
```

The idempotency key ensures repeated requests with identical payment details do not create duplicate payments.

### Replay a failed delivery

```bash
curl -i -X POST \
  "http://localhost:8080/api/deliveries/6/replay"
```

A successful replay request returns HTTP `202 Accepted`.

### Test merchant webhook

```text
POST http://localhost:8080/test-merchant/webhook
```

The test merchant verifies the incoming HMAC signature before accepting the webhook.

A simulated failure can be triggered using:

```text
POST http://localhost:8080/test-merchant/webhook?fail=true
```

## Current Scope and Limitations

ShaPay is a portfolio project demonstrating payment notification infrastructure rather than a production payment gateway.

Current implementation limitations:

- Payment creation simulates a successful payment rather than integrating with a real payment processor.
- Event delivery currently targets all active subscribed endpoints; merchant-specific payment ownership and endpoint isolation are future improvements.
- Worker concurrency and stale-delivery recovery require further automated testing.
- Authentication, authorization, rate limiting, and production-grade secret management are not yet implemented.
- A frontend dashboard is planned.

## Future Improvements

- React and TypeScript monitoring dashboard.
- Merchant-specific event routing.
- Automated worker concurrency tests.
- More comprehensive retry and replay-cycle tracking.
- Authentication and authorization.
- Production-grade configuration and secret management.

---

**ShaPay** — Exploring reliable payment event delivery through practical backend engineering.