# Event Ledger

A Java 17 / Spring Boot 3 take-home implementation with exactly two independently runnable services. `event-gateway` is the public API on port 8080; `account-service` is the internal ledger owner on port 8081.

## Architecture

```text
Client -> event-gateway :8080 -> account-service :8081
          own H2 database      own H2 database
```

The Gateway validates requests, generates or accepts `X-Trace-Id`, enforces gateway-level idempotency, calls Account Service synchronously, and stores only successfully applied events. Account Service independently enforces `eventId` uniqueness, stores transactions, and calculates balances dynamically. The services share neither a database nor in-process state.

## APIs

| Service | Method | Endpoint | Purpose |
|---|---|---|---|
| Gateway | POST | `/events` | Validate and apply an event |
| Gateway | GET | `/events/{eventId}` | Fetch a stored event |
| Gateway | GET | `/events?account={accountId}` | Chronological account events |
| Gateway | GET | `/health` | Gateway and database status |
| Account | POST | `/accounts/{accountId}/transactions` | Apply a transaction |
| Account | GET | `/accounts/{accountId}/balance` | Dynamic net balance |
| Account | GET | `/accounts/{accountId}` | Balance and chronological transactions |
| Account | GET | `/health` | Account Service and database status |

Actuator health and custom metrics are available below `/actuator`; Gateway counters include `events.submitted`, `events.duplicates`, and `events.failed`.

## Run locally

Prerequisites: Java 17+ and Maven 3.9+.

```bash
mvn clean test
cd account-service && mvn spring-boot:run
# in another terminal
cd event-gateway && mvn spring-boot:run
```

The parent reactor pins compilation to Java 17 even when Maven runs on a newer JDK.

## Run with Docker Compose

```bash
docker compose up --build
docker compose down
```

Compose builds both service images, waits for Account Service health, and configures the Gateway with the internal Compose hostname.

## Sample requests

```bash
curl -i -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: demo-trace-001" \
  -d '{"eventId":"evt-001","accountId":"acct-123","type":"CREDIT","amount":150.00,"currency":"USD","eventTimestamp":"2026-05-15T14:02:11Z","metadata":{"source":"mainframe-batch","batchId":"B-9042"}}'

curl -i -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{"eventId":"evt-002","accountId":"acct-123","type":"DEBIT","amount":50.00,"currency":"USD","eventTimestamp":"2026-05-15T13:00:00Z"}'

# Repeat evt-001: 200, duplicate=true, no second balance mutation
curl -i -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{"eventId":"evt-001","accountId":"acct-123","type":"CREDIT","amount":150.00,"currency":"USD","eventTimestamp":"2026-05-15T14:02:11Z"}'

curl -i "http://localhost:8080/events?account=acct-123"
curl -i http://localhost:8080/events/evt-001
curl -i http://localhost:8081/accounts/acct-123/balance
curl -i http://localhost:8081/accounts/acct-123
curl -i http://localhost:8080/actuator/metrics/events.submitted
```

## Design notes

**Idempotency.** `eventId` is the idempotency key and has a database unique constraint in both services. Gateway checks before making the downstream call and returns the original event with 200 on duplicates. Account Service repeats the check defensively, so a retried downstream request cannot change the balance twice.

**Out-of-order events.** Both services retain the original `eventTimestamp`; list queries sort it ascending rather than using arrival time. Balance is `sum(CREDIT) - sum(DEBIT)`, so arrival order cannot affect the result.

**Trace propagation and logs.** A servlet filter accepts `X-Trace-Id` or generates a UUID, stores it in MDC for the request, returns it in the response, and the Gateway forwards it downstream. Logback emits JSON containing timestamp, level, service, message, and MDC `traceId`.

**Resiliency.** Gateway uses a 2-second connection/read timeout and Resilience4j retry (three attempts, exponential backoff starting at 300 ms). This bounds slow calls while tolerating brief network faults. Exhausted attempts become a clear 503 response.

**Graceful degradation.** Gateway writes happen only after Account Service accepts the transaction. If Account Service is down, `POST /events` returns 503 and no Gateway event is marked `APPLIED`; Gateway GET endpoints and health remain local and usable.

## Tests

```bash
mvn test
```

Tests cover validation, credit/debit balance, duplicate suppression, timestamp ordering, 503 mapping, trace header propagation, and metrics-relevant branches. `FullFlowIntegrationTest` starts both real Spring Boot applications on random ports and verifies POST Gateway → Account transaction → balance query.

## Tradeoffs and future improvements

This deliberately uses in-memory H2 and synchronous REST to match the assignment. There is a small distributed consistency window: Account Service can commit and the Gateway process can fail before its local commit. Account-level idempotency makes a client retry safe, but a production design would use a durable event/outbox workflow and reconciliation. Other sensible extensions are currency-consistency rules, pagination for histories, OpenTelemetry/Jaeger, Prometheus, authentication, persistent databases, and concurrency-focused idempotency tests.
