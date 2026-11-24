# ShaPay — Payment Webhook Delivery Service

> Reliable payment event delivery, even when things fail.

ShaPay is a payment webhook delivery service built with Spring Boot and PostgreSQL. It simulates payment events and reliably delivers them to merchant applications, even when their servers are unavailable.

The project focuses on backend engineering concepts used in real payment infrastructure: asynchronous processing, retries, event persistence, idempotency, concurrency, and webhook security.

## Tech Stack

| Area | Technology |
|---|---|
| Backend | Java 21+, Spring Boot |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Database migrations | Flyway |
| HTTP delivery | Spring WebClient |
| Background processing | Spring Scheduler |
| Frontend | React, TypeScript |
| Testing | JUnit 5, Testcontainers |
| Containerisation | Docker, Docker Compose |

## How ShaPay Works

1. A simulated payment is created.
2. ShaPay saves the payment and generates a payment event.
3. The event is stored in PostgreSQL.
4. A background worker picks up pending events.
5. ShaPay sends an HTTP POST request to the merchant's webhook URL.
6. If delivery succeeds, the event is marked as delivered.
7. If delivery fails, ShaPay schedules another attempt using exponential backoff.
8. Every attempt is recorded and visible in the dashboard.
9. Events that exhaust their retry limit enter a dead-letter state and can be manually replayed.

## Project Architecture

```text
                 ┌─────────────────────┐
                 │   Payment Request   │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │   Payment Service   │
                 └──────────┬──────────┘
                            │
                   Save payment + event
                    in one transaction
                            │
                            ▼
                 ┌─────────────────────┐
                 │     PostgreSQL      │
                 │                     │
                 │  Payments           │
                 │  Webhook Endpoints  │
                 │  Webhook Events     │
                 │  Delivery Attempts  │
                 └──────────┬──────────┘
                            │
                     Pending events
                            │
                            ▼
                 ┌─────────────────────┐
                 │  Delivery Worker    │
                 └──────────┬──────────┘
                            │
                      HTTP POST
                            │
                            ▼
                 ┌─────────────────────┐
                 │ Merchant's Server   │
                 └──────────┬──────────┘
                            │
                    HTTP response
                            │
                            ▼
                 ┌─────────────────────┐
                 │ Update event status │
                 │ Record attempt      │
                 │ Schedule retry      │
                 └─────────────────────┘
```

---

# Development Roadmap

Complete the milestones in order. Do not move to the next milestone until the current milestone's acceptance criteria are met.

## Milestone 1 — Foundation & Project Setup

**Goal:** Build the foundation of the application and connect Spring Boot to PostgreSQL.

### What you're building

A working Spring Boot application with a properly configured database, database migrations, and a clean project structure.

### Concepts to understand

- Spring Boot project structure
- Controller, service, repository and entity responsibilities
- Dependency injection
- Spring Data JPA and Hibernate
- Flyway database migrations
- PostgreSQL connections
- Environment variables and application configuration

### Tasks

- [ ] Create a Spring Boot project.
- [ ] Configure Java 21 or later.
- [ ] Add Spring Web.
- [ ] Add Spring Data JPA.
- [ ] Add PostgreSQL driver.
- [ ] Add Flyway.
- [ ] Add validation dependencies.
- [ ] Add Docker Compose for PostgreSQL.
- [ ] Configure database connection in `application.yml`.
- [ ] Configure Hibernate to validate the database schema.
- [ ] Create an organised package structure.

### Initial package structure

```text
src/main/java/com/shapay/
│
├── payment/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── webhook/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── delivery/
│   ├── service/
│   └── worker/
│
└── common/
    ├── config/
    └── exception/
```

### Database tables

Create the initial Flyway migration for these four tables:

**1. `payments`**

Stores simulated payment information.

- ID
- Amount
- Currency
- Payment status
- Created timestamp

**2. `webhook_endpoints`**

Stores merchant webhook destinations.

- ID
- URL
- Signing secret
- Subscribed event types
- Created timestamp
- Active status

**3. `webhook_events`**

Stores events that need to be delivered.

- ID
- Payment ID
- Event type
- Event status
- Attempt count
- Next attempt timestamp
- Created timestamp

**4. `delivery_attempts`**

Stores every individual delivery attempt.

- ID
- Webhook event ID
- Attempt number
- HTTP status code
- Response body
- Response duration
- Attempt timestamp

### Acceptance criteria

- [ ] Spring Boot starts successfully.
- [ ] PostgreSQL runs through Docker Compose.
- [ ] Spring Boot connects to PostgreSQL.
- [ ] Flyway creates all four tables.
- [ ] Hibernate validates the schema successfully.
- [ ] A test payment can be saved and retrieved from PostgreSQL.

**Milestone complete when:** You can run the application, connect to the database, and persist a payment.

---

## Milestone 2 — Payment Simulation & Event Creation

**Goal:** Simulate a payment and automatically generate a webhook event.

### What you're building

A simple payment API that creates simulated payments and generates corresponding events.

No real payment gateway or money movement is involved.

### Concepts to understand

- REST API design
- HTTP methods and status codes
- DTOs and entity mapping
- Service-layer responsibilities
- Database transactions with `@Transactional`
- Transactional outbox pattern
- UUIDs and unique identifiers

### Tasks

- [ ] Create a payment request DTO.
- [ ] Create a payment response DTO.
- [ ] Implement `POST /api/payments`.
- [ ] Accept an amount and currency.
- [ ] Simulate payment success or failure.
- [ ] Generate a unique payment ID.
- [ ] Save the payment.
- [ ] Generate a unique webhook event ID.
- [ ] Create `payment.succeeded` or `payment.failed` events.
- [ ] Save the payment and event in the same database transaction.
- [ ] Implement `GET /api/payments/{id}`.
- [ ] Implement `GET /api/payments` for listing payments.

### Example request

```http
POST /api/payments
Content-Type: application/json
```

```json
{
  "amount": 100.00,
  "currency": "MYR"
}
```

### Example response

```json
{
  "paymentId": "pay_123",
  "amount": 100.00,
  "currency": "MYR",
  "status": "SUCCEEDED"
}
```

A successful payment should automatically create an event:

```json
{
  "eventId": "evt_123",
  "type": "payment.succeeded",
  "status": "PENDING",
  "paymentId": "pay_123"
}
```

### Important: Transactional consistency

The payment and its event must be saved in the same database transaction.

Why?

Imagine the payment is saved successfully, but the application crashes before creating the webhook event. The payment exists, but the merchant will never be notified.

Saving both together prevents this inconsistency.

### Acceptance criteria

- [ ] A payment can be created through the API.
- [ ] A unique payment ID is generated.
- [ ] A webhook event is generated automatically.
- [ ] Payment and event are persisted together.
- [ ] A failed transaction does not leave behind only one record.
- [ ] Payment and event information can be retrieved from the database.

**Milestone complete when:** Creating one simulated payment automatically creates its corresponding persistent webhook event.

---

## Milestone 3 — Webhook Endpoint Management

**Goal:** Allow merchants to register their webhook URLs and choose which events they want to receive.

### What you're building

An endpoint management API that stores merchant destinations and their event subscriptions.

### Concepts to understand

- REST resource design
- Request validation
- One-to-many relationships
- Entity relationships in JPA
- Secret generation
- API input validation

### Tasks

- [ ] Create an endpoint request DTO.
- [ ] Create an endpoint response DTO.
- [ ] Implement `POST /api/webhook-endpoints`.
- [ ] Accept a webhook URL.
- [ ] Accept subscribed event types.
- [ ] Generate a unique endpoint ID.
- [ ] Generate a signing secret.
- [ ] Store the endpoint in PostgreSQL.
- [ ] Implement `GET /api/webhook-endpoints`.
- [ ] Implement `GET /api/webhook-endpoints/{id}`.
- [ ] Implement endpoint activation/deactivation.
- [ ] Validate URL format.
- [ ] Validate supported event types.
- [ ] Ensure inactive endpoints are not selected for delivery.

### Example request

```json
{
  "url": "https://merchant.example.com/webhooks",
  "subscribedEvents": [
    "payment.succeeded",
    "payment.failed"
  ]
}
```

### Example response

```json
{
  "endpointId": "ep_123",
  "url": "https://merchant.example.com/webhooks",
  "subscribedEvents": [
    "payment.succeeded",
    "payment.failed"
  ],
  "status": "ACTIVE"
}
```

The signing secret should be generated by the backend, not supplied by the merchant in this initial version.

### Acceptance criteria

- [ ] A merchant endpoint can be registered.
- [ ] Its signing secret is generated.
- [ ] Subscribed event types are stored.
- [ ] Endpoints can be listed and retrieved.
- [ ] Inactive endpoints are excluded from delivery.
- [ ] Invalid URLs and unsupported event types are rejected.

**Milestone complete when:** You can register a merchant endpoint and configure which payment events it receives.

---

## Milestone 4 — Webhook Delivery Engine

**Goal:** Deliver stored webhook events to registered merchant endpoints.

### What you're building

A background worker that retrieves pending events and sends HTTP POST requests to merchant servers.

This is the core of ShaPay.

### Concepts to understand

- Asynchronous processing
- Background workers
- Spring scheduling
- HTTP clients and WebClient
- HTTP timeouts
- JSON serialization
- Event delivery lifecycle
- Database state management

### Tasks

- [ ] Create a delivery service.
- [ ] Configure Spring WebClient.
- [ ] Create a scheduled delivery worker.
- [ ] Fetch pending events from PostgreSQL.
- [ ] Find active endpoints subscribed to each event type.
- [ ] Construct the webhook payload.
- [ ] Send an HTTP POST request.
- [ ] Configure connection and response timeouts.
- [ ] Capture the HTTP response.
- [ ] Record the response status code.
- [ ] Record the response body.
- [ ] Record the delivery duration.
- [ ] Save a delivery attempt.
- [ ] Mark successful events as `DELIVERED`.
- [ ] Update unsuccessful events for retry handling in the next milestone.

### Example webhook payload

```json
{
  "id": "evt_123",
  "type": "payment.succeeded",
  "createdAt": "2026-11-24T10:00:00Z",
  "data": {
    "paymentId": "pay_123",
    "amount": 100.00,
    "currency": "MYR",
    "status": "SUCCEEDED"
  }
}
```

### Delivery states

```text
PENDING
   |
   v
PROCESSING
   |
   +------ HTTP 2xx ------> DELIVERED
   |
   +------ Failure -------> RETRYING
```

### Build a local test receiver

Create a small local HTTP receiver for testing. It should support:

- Always return HTTP 200.
- Always return HTTP 500.
- Fail twice, then return HTTP 200.
- Delay its response to simulate a timeout.

Keep this receiver controlled and local. Do not expose arbitrary internal URLs as destinations.

### Acceptance criteria

- [ ] The worker finds pending events.
- [ ] Events are sent to the correct subscribed endpoints.
- [ ] The receiver gets the expected JSON payload.
- [ ] HTTP responses are captured.
- [ ] Every attempt is saved in `delivery_attempts`.
- [ ] Successful deliveries are marked `DELIVERED`.
- [ ] Failed deliveries remain available for retry processing.

**Milestone complete when:** A simulated payment event reaches your local receiver, and ShaPay records the delivery result.

---

## Milestone 5 — Retry Mechanism & Failure Handling

**Goal:** Make ShaPay reliable when merchant servers are unavailable.

### What you're building

A retry engine that automatically attempts failed deliveries again, handles concurrent workers, and prevents events from becoming permanently stuck.

### Concepts to understand

- Exponential backoff
- At-least-once delivery
- Retry policies
- Dead-letter queues
- Database locking
- PostgreSQL `FOR UPDATE SKIP LOCKED`
- Concurrent processing
- Worker leases and crash recovery
- Manual replay

### Tasks

**1. Implement exponential backoff**

- [ ] Define a maximum number of attempts.
- [ ] Calculate the next retry time.
- [ ] Store `next_attempt_at` in PostgreSQL.
- [ ] Retry only when the scheduled time has arrived.
- [ ] Configure a maximum retry delay.

Example retry schedule:

| Attempt | Delay |
|---|---:|
| 1 | Immediate |
| 2 | 5 seconds |
| 3 | 10 seconds |
| 4 | 20 seconds |
| 5 | 40 seconds |

These are example values; make them configurable.

**2. Implement delivery states**

```text
PENDING
   |
   v
PROCESSING
   |
   +---- Success -------> DELIVERED
   |
   +---- Failure -------> RETRYING
                              |
                        Retry limit reached
                              |
                              v
                         DEAD_LETTER
```

- [ ] Add `RETRYING` state.
- [ ] Add `DEAD_LETTER` state.
- [ ] Update attempt count after each attempt.
- [ ] Stop automatic retries after the maximum attempt count.
- [ ] Preserve all historical delivery attempts.

**3. Handle concurrent workers**

Imagine two workers pick up the same pending event at the same time.

Both could send the same webhook simultaneously.

Prevent this using PostgreSQL row locking:

```sql
SELECT *
FROM webhook_events
WHERE status IN ('PENDING', 'RETRYING')
  AND next_attempt_at <= NOW()
ORDER BY created_at
FOR UPDATE SKIP LOCKED
LIMIT 10;
```

- [ ] Implement atomic event claiming.
- [ ] Use `FOR UPDATE SKIP LOCKED`.
- [ ] Ensure workers cannot claim the same event simultaneously.
- [ ] Keep the claim and state transition inside a transaction.

**4. Handle worker crashes**

- [ ] Track when an event was claimed.
- [ ] Introduce a processing lease or claim expiry.
- [ ] Recover events whose processing lease expires.
- [ ] Ensure crashed workers do not leave events stuck in `PROCESSING`.

**5. Implement manual replay**

- [ ] Add an API to replay a dead-letter event.
- [ ] Reset its delivery state appropriately.
- [ ] Schedule it for another attempt.
- [ ] Preserve its previous attempt history.
- [ ] Prevent replay from creating duplicate event records.

### Example replay endpoint

```http
POST /api/webhook-events/{eventId}/replay
```

### Acceptance criteria

- [ ] Failed deliveries are retried automatically.
- [ ] Retry delays increase using exponential backoff.
- [ ] Events stop retrying after the configured limit.
- [ ] Exhausted events enter `DEAD_LETTER`.
- [ ] Dead-letter events can be manually replayed.
- [ ] Concurrent workers do not claim the same event.
- [ ] Expired processing leases can be recovered.
- [ ] Previous delivery attempts remain available.

**Milestone complete when:** You can deliberately make a merchant server fail, watch ShaPay retry, restore the server, and see the event eventually delivered.

---

## Milestone 6 — Webhook Security & Idempotency

**Goal:** Secure webhook requests and handle duplicate deliveries safely.

### What you're building

A signing and verification mechanism that allows merchant applications to verify that webhook requests genuinely came from ShaPay.

You'll also implement receiver-side idempotency to prevent duplicate processing.

### Concepts to understand

- HMAC-SHA256
- Cryptographic signatures
- Request timestamps
- Replay attacks
- Idempotency
- At-least-once delivery
- SSRF (Server-Side Request Forgery)
- Secure secret handling

### Tasks

**1. Implement HMAC-SHA256 signing**

- [ ] Generate a signing secret for each endpoint.
- [ ] Generate a timestamp for each outgoing request.
- [ ] Create a signature using HMAC-SHA256.
- [ ] Include the signature in the request headers.
- [ ] Include the timestamp in the request headers.

Example headers:

```http
X-ShaPay-Timestamp: 1795514400
X-ShaPay-Signature: sha256=abc123...
X-ShaPay-Event-Id: evt_123
```

The signature should be calculated from a documented message format, such as the timestamp and exact serialized request body.

**2. Build signature verification**

- [ ] Create a verification method in the test receiver.
- [ ] Recalculate the signature using the shared secret.
- [ ] Compare signatures using a constant-time comparison.
- [ ] Reject invalid signatures.
- [ ] Reject requests with timestamps outside an allowed time window.

**3. Implement receiver-side idempotency**

At-least-once delivery means a merchant can receive the same event more than once.

The receiver must recognise events it has already processed.

- [ ] Use the event ID as the idempotency key.
- [ ] Store processed event IDs.
- [ ] Reject or safely ignore duplicate event IDs.
- [ ] Ensure the idempotency record and business action are handled atomically.

**4. Add SSRF protection**

Webhook delivery makes outbound HTTP requests to merchant-provided URLs. This introduces SSRF risk.

- [ ] Reject unsupported URL schemes.
- [ ] Block localhost and private/internal IP addresses.
- [ ] Block cloud metadata addresses.
- [ ] Validate resolved IP addresses, not only the URL hostname.
- [ ] Revalidate destinations to reduce DNS rebinding risks.
- [ ] Apply network-level outbound restrictions where possible.

Do not expose endpoint registration or arbitrary URL delivery publicly until these safeguards are in place.

**5. Protect signing secrets**

- [ ] Never return signing secrets in normal endpoint responses.
- [ ] Avoid logging secrets.
- [ ] Keep configuration and sensitive values out of Git.
- [ ] Use environment variables for local configuration.

### Acceptance criteria

- [ ] Outgoing webhook requests include HMAC signatures.
- [ ] The receiver can verify valid signatures.
- [ ] Invalid signatures are rejected.
- [ ] Old timestamps are rejected.
- [ ] Duplicate event IDs are not processed twice.
- [ ] Basic SSRF protections are implemented and tested.
- [ ] Signing secrets are not exposed through API responses or logs.

**Milestone complete when:** Your test receiver can verify genuine ShaPay requests and safely ignore duplicate or invalid requests.

---

## Milestone 7 — Frontend Monitoring Dashboard

**Goal:** Make webhook delivery activity easy to understand without checking the database or server logs.

### What you're building

A React and TypeScript dashboard that displays payment events, delivery statuses, endpoint information, and individual delivery attempts.

The frontend should be simple. The backend delivery engine is the main focus of this project.

### Concepts to understand

- React components
- TypeScript interfaces
- REST API integration
- State management
- Loading and error states
- Tables, filters and pagination
- Data visualisation

### Tasks

**1. Dashboard overview**

- [ ] Display total webhook events.
- [ ] Display delivered events.
- [ ] Display pending/retrying events.
- [ ] Display dead-letter events.
- [ ] Display delivery success rate.
- [ ] Display recent delivery activity.

**2. Events page**

- [ ] Display all webhook events.
- [ ] Show event ID and event type.
- [ ] Show payment ID.
- [ ] Show event status.
- [ ] Show attempt count.
- [ ] Add status filters.
- [ ] Add event type filters.
- [ ] Add pagination.

**3. Event details page**

- [ ] Display event information.
- [ ] Display the destination endpoint.
- [ ] Display current delivery status.
- [ ] Display the next scheduled retry time.
- [ ] Display the full delivery attempt history.
- [ ] Display HTTP status codes.
- [ ] Display response bodies.
- [ ] Display delivery durations.
- [ ] Add a manual replay action for eligible events.

**4. Endpoints page**

- [ ] Display registered endpoints.
- [ ] Display subscribed event types.
- [ ] Display endpoint status.
- [ ] Add endpoint registration.
- [ ] Add endpoint activation/deactivation.
- [ ] Add a test delivery action.

**5. Connect the frontend**

- [ ] Connect React to the Spring Boot APIs.
- [ ] Add loading states.
- [ ] Add error handling.
- [ ] Refresh event statuses when necessary.
- [ ] Ensure replay actions update the UI.

### Suggested dashboard layout

```text
┌────────────────────────────────────────────────────────┐
│ ShaPay                                  Dashboard       │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Total Events    Delivered    Retrying    Dead Letter  │
│      120             98           15           7       │
│                                                        │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Recent Webhook Events                                 │
│                                                        │
│  Event ID     Type                Status       Attempts│
│  evt_001      payment.succeeded   DELIVERED       1    │
│  evt_002      payment.failed      RETRYING        2    │
│  evt_003      payment.succeeded   DEAD_LETTER     5    │
│                                                        │
└────────────────────────────────────────────────────────┘
```

*The numbers above are illustrative, not actual project results.*

### Acceptance criteria

- [ ] Dashboard displays live data from the backend.
- [ ] Event statuses are easy to understand.
- [ ] Users can filter events.
- [ ] Users can inspect individual delivery attempts.
- [ ] Users can manually replay eligible events.
- [ ] Users can manage webhook endpoints.
- [ ] Loading and error states are handled properly.

**Milestone complete when:** Someone can understand what happened to a payment webhook just by looking at the dashboard.

---

## Milestone 8 — Testing, Deployment & Documentation

**Goal:** Make ShaPay reliable, runnable, documented, and ready to demonstrate.

### What you're building

A tested and deployable application with clear documentation, architecture diagrams, and a working demonstration of failure recovery.

### Concepts to understand

- Unit testing
- Integration testing
- Testcontainers
- Mock HTTP servers
- Docker Compose
- Application configuration
- Structured logging
- Correlation IDs
- Basic load testing
- Deployment

### Tasks

**1. Backend testing**

- [ ] Test payment creation.
- [ ] Test automatic event creation.
- [ ] Test transaction rollback.
- [ ] Test successful webhook delivery.
- [ ] Test HTTP 500 responses.
- [ ] Test connection failures.
- [ ] Test request timeouts.
- [ ] Test exponential retry scheduling.
- [ ] Test retry exhaustion.
- [ ] Test manual replay.
- [ ] Test concurrent event claiming.
- [ ] Test expired processing leases.
- [ ] Test HMAC signature verification.
- [ ] Test duplicate event handling.
- [ ] Test SSRF validation.

**2. Integration testing**

- [ ] Add Testcontainers for PostgreSQL.
- [ ] Test database migrations.
- [ ] Test delivery using a mock HTTP server.
- [ ] Test the full payment-to-delivery flow.
- [ ] Ensure tests do not depend on an existing local database.

**3. Observability**

- [ ] Add structured logging.
- [ ] Include event IDs in relevant logs.
- [ ] Include payment IDs in relevant logs.
- [ ] Add correlation IDs to trace operations.
- [ ] Log delivery failures without exposing sensitive information.

**4. Docker and deployment**

- [ ] Create a Dockerfile for the backend.
- [ ] Create a Dockerfile for the frontend.
- [ ] Configure Docker Compose.
- [ ] Ensure PostgreSQL, backend and frontend can run together.
- [ ] Separate local and production configuration.
- [ ] Deploy the application.
- [ ] Verify the deployed application works.

**5. Documentation**

- [ ] Write a clear project overview.
- [ ] Add the architecture diagram.
- [ ] Document the payment-to-webhook flow.
- [ ] Document API endpoints.
- [ ] Document webhook payload structure.
- [ ] Document signature generation and verification.
- [ ] Document retry behaviour.
- [ ] Document event lifecycle states.
- [ ] Add setup instructions.
- [ ] Add environment variable examples.
- [ ] Add screenshots of the dashboard.
- [ ] Add a short demonstration video.

**6. Optional: Load testing**

- [ ] Use a tool such as k6.
- [ ] Simulate multiple payment events.
- [ ] Measure delivery throughput.
- [ ] Measure response times.
- [ ] Observe how the worker behaves under load.
- [ ] Document actual results and test conditions.

Only publish performance numbers that you have measured.

### Acceptance criteria

- [ ] Automated tests cover the main delivery lifecycle.
- [ ] Integration tests run against a real PostgreSQL container.
- [ ] Docker Compose starts the application.
- [ ] The deployed application works.
- [ ] README explains the architecture and setup.
- [ ] A complete demonstration shows both successful delivery and failure recovery.

**Milestone complete when:** Someone can clone ShaPay, understand the architecture, run it, and see how it handles failed webhook deliveries.

---

# Final Demonstration Scenario

This is the main scenario to demonstrate in your portfolio and interview.

### Scenario: Merchant server is unavailable

1. Register a merchant endpoint in ShaPay.
2. Configure the test receiver to return HTTP 500.
3. Create a simulated payment for RM100.
4. ShaPay generates a `payment.succeeded` event.
5. The delivery worker sends the event.
6. The merchant returns HTTP 500.
