# PayMoney – Digital Wallet & Payments Platform

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring_Cloud-2025.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Cloud"/>
  <img src="https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis"/>
  <img src="https://img.shields.io/badge/Apache_Kafka-4.0-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" alt="Kafka"/>
  <img src="https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white" alt="Flyway"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/Razorpay-Payments-0C2451?style=for-the-badge&logo=razorpay&logoColor=white" alt="Razorpay"/>
  <img src="https://img.shields.io/badge/Maven-Multi--Module-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger"/>
  <img src="https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT"/>
  <img src="https://img.shields.io/badge/Architecture-Microservices-blueviolet?style=for-the-badge" alt="Microservices"/>
</p>

A backend digital-wallet system built with **Java 21, Spring Boot 4, PostgreSQL, Redis, Kafka and Docker**
using a **microservices architecture**. Users can register, add money through Razorpay, transfer money to
other wallets, withdraw, and view their transaction history – with the safety guarantees a money system needs
(idempotent requests, row-level locking, fraud checks, audit trail, rate limiting).

## Features

* **Authentication & authorization** – JWT login, `USER` / `ADMIN` roles, account deactivation & reactivation, auto-created admin account
* **Wallets** – one wallet per user, balance lookup (cached in Redis)
* **Money transfer** – wallet-to-wallet transfers with an `Idempotency-Key` header, so retries never move money twice
* **Withdrawals** – idempotent withdrawals with balance validation
* **Add money** – Razorpay Payment Links with server-side signature + amount + currency verification before the wallet is credited
* **Transaction history** – paginated, newest first, with ownership checks
* **Fraud check** – blocks a wallet that makes too many transfers within a minute
* **Concurrency safety** – pessimistic row locks, acquired in a fixed order to avoid deadlocks
* **Rate limiting** – Redis-backed limiter at the API Gateway for login, register, transfers, withdrawals and payments
* **Audit logging** – transfers, withdrawals, deposits and payment-link events are published to Kafka (after the DB commit) and stored by a dedicated audit service
* **API docs** – Swagger / OpenAPI with a JWT bearer scheme
* **Database versioning** – Flyway migrations in every service

## Tech stack

| Area | Technology |
|---|---|
| Language / framework | Java 21, Spring Boot 4.1, Spring MVC, Spring Data JPA, Spring Security |
| Microservices | Spring Cloud Gateway, Eureka (service discovery), Config Server, OpenFeign |
| Messaging | Apache Kafka |
| Data | PostgreSQL (database per service), Redis (cache + rate limiting), Flyway |
| Payments | Razorpay Java SDK |
| Auth | JWT (jjwt), BCrypt |
| DevOps | Docker, Docker Compose, Maven multi-module |

## Architecture

```mermaid
flowchart LR
  C[Client] --> GW[API Gateway :8080<br/>routing + rate limiting]
  GW --> US[user-service :8081]
  GW --> WS[wallet-service :8082]
  GW --> PS[payment-service :8083]

  US -- user-registered / user-updated --> K[(Kafka)]
  K --> WS
  WS -- audit-log --> K
  PS -- audit-log --> K
  K --> AS[audit-service :8084]

  WS -. Feign .-> US
  PS -. Feign .-> US
  PS -. Feign .-> WS

  US --- DB1[(paymoney_user)]
  WS --- DB2[(paymoney_wallet)]
  PS --- DB3[(paymoney_payment)]
  AS --- DB4[(paymoney_audit)]
  WS --- R[(Redis)]
  GW --- R

  CS[Config Server :8888] -.-> GW & US & WS & PS & AS
  EU[Eureka :8761] -.-> GW & US & WS & PS & AS
```

| Service | Responsibility | Database |
|---|---|---|
| `api-gateway` | Single entry point, routing via Eureka, Redis rate limiting | – |
| `user-service` | Registration, login/JWT, profile & admin APIs, account status | `paymoney_user` |
| `wallet-service` | Wallets, transfers, withdrawals, deposits, transaction history, idempotency, fraud check, Redis cache | `paymoney_wallet` |
| `payment-service` | Razorpay payment links and verification | `paymoney_payment` |
| `audit-service` | Consumes audit events from Kafka and stores them | `paymoney_audit` |
| `discovery-server` | Eureka service registry | – |
| `config-server` | Centralised configuration for all services | – |

Each service is an independent Spring Boot app (embedded Tomcat), has its own Dockerfile and owns its own database.
Services talk **asynchronously through Kafka** (events) and **synchronously through OpenFeign** (lookups, wallet credit).

## Key flows

**Registration**
```mermaid
sequenceDiagram
  participant C as Client
  participant U as user-service
  participant K as Kafka
  participant W as wallet-service
  C->>U: POST /api/v1/register-user
  U->>U: save user (BCrypt password)
  U->>K: user-registered
  K->>W: user-registered
  W->>W: create wallet (balance 0)
```

**Transfer**
```mermaid
sequenceDiagram
  participant C as Client
  participant G as Gateway
  participant W as wallet-service
  participant U as user-service
  participant K as Kafka
  C->>G: POST /api/v1/transfers (JWT + Idempotency-Key)
  G->>G: rate limit (Redis)
  G->>W: route via Eureka
  W->>U: validate user / account status
  W->>W: idempotency check, lock both wallets, fraud check, balance check
  W->>W: debit + credit + save transaction (one DB transaction)
  W-->>K: audit-log
  W-->>C: transfer result
```

**Add money (Razorpay)**
```mermaid
sequenceDiagram
  participant C as Client
  participant P as payment-service
  participant R as Razorpay
  participant W as wallet-service
  C->>P: POST /api/v1/payment-links
  P->>R: create payment link
  P-->>C: payment link URL
  C->>R: pays on Razorpay
  R-->>P: callback (signed)
  P->>P: verify signature, amount, currency, captured status
  P->>W: deposit (idempotent per payment)
  W->>W: credit wallet + DEPOSIT transaction
```

**Login & request authentication (JWT)**
```mermaid
sequenceDiagram
  participant C as Client
  participant G as Gateway
  participant U as user-service
  participant S as wallet / payment service
  C->>G: POST /api/v1/auth/login
  G->>U: route
  U->>U: verify BCrypt password + account ACTIVE
  U-->>C: JWT
  C->>G: any API call with Authorization: Bearer JWT
  G->>S: route via Eureka
  S->>S: verify JWT signature
  S->>U: Feign - load user by email
  U-->>S: role + account status
  S->>S: reject if user missing / deactivated, else set security context
```

**Rate limiting at the gateway**
```mermaid
flowchart TD
  A[Incoming request] --> B{Rate-limited endpoint?}
  B -- No --> F[Forward to service]
  B -- Yes --> C[Identify caller: IP or hashed JWT]
  C --> D[Redis INCR key, 60s window]
  D --> E{Count above limit?}
  E -- No --> F
  E -- Yes --> G[429 Too Many Requests]
  D -. Redis down .-> H[503 Rate limiting unavailable]
```

**Transfer safety checks inside wallet-service**
```mermaid
flowchart TD
  A[Transfer request + Idempotency-Key] --> B{Key already used?}
  B -- Same request --> R[Return the original result]
  B -- Different request --> X[Reject]
  B -- New --> C[Lock both wallets in ID order]
  C --> D{Sender wallet belongs to user?}
  D -- No --> X
  D -- Yes --> E{Fraud check: 5+ transfers in 1 min?}
  E -- Yes --> X
  E -- No --> F{Enough balance?}
  F -- No --> X
  F -- Yes --> G[Debit + credit + save transaction + save idempotency key]
  G --> H[Publish audit-log event]
```

**Audit logging (event-driven)**
```mermaid
flowchart LR
  W[wallet-service<br/>TRANSFER / WITHDRAW] -->|audit-log| K[(Kafka)]
  P[payment-service<br/>PAYMENT_LINK_CREATED / VERIFIED] -->|audit-log| K
  K --> A[audit-service]
  A --> D[(paymoney_audit)]
```

**Kafka event map**
```mermaid
flowchart LR
  U[user-service] -->|user-registered| K[(Kafka)]
  U -->|user-updated| K
  K -->|create wallet| W[wallet-service]
  K -->|sync wallet name| W
  W -->|audit-log| K
  P[payment-service] -->|audit-log| K
  K -->|store audit log| A[audit-service]
```

**Startup order (Docker Compose)**
```mermaid
flowchart LR
  I[Postgres + Redis + Kafka] --> C[config-server + discovery-server]
  C --> S[user / wallet / payment / audit services]
  C --> G[api-gateway]
```

## API overview (via the gateway, `http://localhost:8080`)

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/register-user` | Public |
| POST | `/api/v1/auth/login` | Public |
| POST | `/api/v1/auth/reactivate` | Public |
| PATCH | `/api/v1/users/me/deactivate` | User |
| GET | `/api/v1/all-users` | Admin |
| GET / PUT | `/api/v1/users/{userId}` | Admin |
| GET | `/api/v1/wallet` | User |
| POST | `/api/v1/transfers` (`Idempotency-Key` header) | User |
| POST | `/api/v1/withdrawals` (`Idempotency-Key` header) | User |
| GET | `/api/v1/wallet/transactions?page=&size=` | User |
| GET | `/api/v1/transactions/{transactionId}` | User |
| POST | `/api/v1/payment-links` | User |
| POST | `/api/v1/payment-link-verifications` | User |
| GET | `/api/v1/payment-links/callback` | Razorpay callback |

Swagger UI per service (only reachable from your own machine): `http://localhost:8081/swagger-ui.html` (user), `:8082` (wallet), `:8083` (payment).

See `TESTING.md` for step-by-step endpoint testing.

## Reliability & security notes

* **Internal endpoints** (`/api/v1/internal/**`) need the `X-Internal-Key` header (value from `INTERNAL_API_KEY`). Feign adds it automatically; the gateway never routes these paths.
* **Service ports** 8081-8084 are bound to `127.0.0.1` only, so the outside world can only reach the gateway (8080).
* **Kafka after commit** – audit events are sent only after the DB transaction commits, with `max.block.ms=2000` so a Kafka outage cannot hang a transfer.
* **Retry + dead letter topic** – a failing Kafka listener retries 5 times, then the message goes to `<topic>.DLT`.
* **Redis down** – the wallet cache is skipped (database is used) instead of failing the request.
* **Feign timeouts** – 2s connect / 5s read.
* **Idempotency keys** are unique per user, not globally.

Known limitations (good discussion points): no outbox pattern for the user-registered event, no circuit breaker (Resilience4j), no Razorpay webhook, JWTs have no expiry.

## Run locally

Requirements: Docker Desktop.

```bash
cp environmentFile.env.example environmentFile.env      # fill in your values
docker compose --env-file environmentFile.env up --build
```

Databases are created automatically (init script) and tables are created by Flyway on startup.
Eureka dashboard: `http://localhost:8761`.

Environment variables (`environmentFile.env`): `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `PAYMONEY_ADMIN_EMAIL`,
`PAYMONEY_ADMIN_PASSWORD`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `APP_PUBLIC_BASE_URL`, `INTERNAL_API_KEY`.

## Project structure

```
PayMoney/
├── api-gateway/        ├── user-service/      ├── wallet-service/
├── payment-service/    ├── audit-service/     ├── discovery-server/
├── config-server/      ├── docker/            ├── docker-compose.yml
└── pom.xml             (each service: Config, Controller, DTO, Entity, Exception,
                         Mapper, Repository, Service, db/migration)
```