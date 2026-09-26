<div align="center">

# 💸 PayMoney
### A Secure FinTech Wallet & Payments Backend — Java 21 · Spring Boot · PostgreSQL

<img src="https://skillicons.dev/icons?i=java,spring,postgres,docker,maven,git,github,idea,postman" alt="Java, Spring, PostgreSQL, Docker, Maven, Git, GitHub, IntelliJ, Postman" />

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

A wallet-and-payments backend covering the parts of fintech engineering that are actually hard: JWT auth with role-based access, atomic wallet transfers, Razorpay-backed payments, fraud screening, idempotent retries, and a full audit trail — built end-to-end and deployed live.

**[🚀 Live API](https://paymoney-backend-ybl6.onrender.com) · [📘 Swagger Docs](https://paymoney-backend-ybl6.onrender.com/swagger-ui/index.html) · [❤️ Health Check](https://paymoney-backend-ybl6.onrender.com/actuator/health)**

### 📫 Let's Connect

I'm actively looking for Java devloper/Backend/SDE opportunities — always happy to talk about system design, payments infrastructure, or this project in detail.

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/ayush-sinha-7611572a1/)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:ayush21052003@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/VASUSINH)
[![About Me](https://img.shields.io/badge/About%20Me-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/VASUSINH/VASUSINH)


</div>

---

## 📸 Preview

<table>
  <tr>
    <td align="center" width="50%">
      <b><h3>🔐 Login</h3></b>
      <img src="./screenshots/01-login.png" width="400" height="260" alt="Login API response"/>
    </td>
    <td align="center" width="50%">
      <b><h3>💰 Wallet Deposit</h3></b>
      <img src="./screenshots/02-wallet-deposit.png" width="400" height="260" alt="Wallet deposit API response"/>
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <b><h3>🔁 Wallet Transfer</h3></b>
      <img src="./screenshots/03-transfer.png" width="400" height="260" alt="Wallet transfer API response"/>
    </td>
    <td align="center" width="50%">
      <b><h3>💳 Payment Order</h3></b>
      <img src="./screenshots/04-payment-order.png" width="400" height="260" alt="Payment order creation API response"/>
    </td>
  </tr>
</table>

---

## ⭐ Highlights

*A quick, scannable summary of what this project demonstrates:*

- 🔐 **Secure auth** — stateless JWT login with role-based access control (`SecurityConfig`, `JwtAuthenticationFilter`)
- 💸 **Real money-movement logic** — atomic wallet-to-wallet transfers, deposits, and withdrawals with a full transaction ledger
- 💳 **Live payment gateway integration** — Razorpay order creation, payment verification, and refunds (`razorpayConfig`, `paymentService`, `refundController`)
- 🛡️ **Risk-aware design** — a dedicated `fraudService` and idempotency handling (`idempotencyEntity`/`idempotencyRepository`) to guard against duplicate or suspicious transactions
- 📜 **Audit-ready** — an append-only audit log (`auditLogEntity`, `auditLogService`) for compliance-style traceability
- 🗃️ **Managed schema evolution** — versioned SQL migrations via Flyway (`V1__`, `V2__`)
- 🐳 **Deployed, not just demoed** — Dockerized, live on Render, with Swagger docs and an Actuator health check publicly reachable
- 🧱 **Clean layered architecture** — Controller → Service → Repository, with dedicated Mapper, DTO, and centralized Exception-handling layers

**Keywords:** Java 21 · Spring Boot · Spring Security · Spring Data JPA · JWT · PostgreSQL · Flyway · Docker · Razorpay · REST API · Postman · RBAC

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language & Framework | Java 21, Spring Boot 4.1.1, Spring Web MVC |
| Security | Spring Security, custom JWT filter, role-based access control |
| Data | Spring Data JPA (Hibernate), PostgreSQL, Flyway migrations |
| Payments | Razorpay (order creation, verification, refunds) |
| Reliability | Custom fraud-screening service, idempotency keys, audit logging |
| Docs & Ops | Swagger / OpenAPI, Spring Boot Actuator |
| Build & Infra | Maven, Docker, Git/GitHub |
| Testing & Tooling | Postman (API collection), IntelliJ IDEA |

---

## 🚀 Development Phases

Built in structured, incremental phases rather than all at once:

- ✅ Phase 1 — Project Setup & Git
- ✅ Phase 2 — User Management & PostgreSQL + JPA
- ✅ Phase 3 — DTO + Validation + Exception Handling
- ✅ Phase 4 — Authentication + JWT
- ✅ Phase 5 — Authorization + RBAC
- ✅ Phase 6 — Wallet Management
- ✅ Phase 7 — Transactions + `@Transactional`
- ✅ Phase 8 — Razorpay Payment Gateway
- ✅ Phase 9 — Refunds
- ✅ Phase 10 — Fraud Detection
- ✅ Phase 11 — Idempotency
- ✅ Phase 12 — Audit Logging
- ✅ Phase 13 — Flyway + Swagger + Actuator
- ✅ Phase 14 — Testing + Docker + Deployment

---

## 📡 API Reference

> Full interactive documentation is available at the [Swagger UI](https://paymoney-backend-ybl6.onrender.com/swagger-ui/index.html) — the source of truth for exact request/response schemas.

| Method | Endpoint | Auth | Description |
|---|---|:---:|---|
| `POST` | `/api/login` | ❌ | Authenticate and receive a JWT |
| `POST` | `/api/wallet/deposit` | ✅ | Deposit into the authenticated user's wallet |
| `POST` | `/api/transactions/transfer` | ✅ | Transfer funds between two wallets |
| `POST` | `/api/payments/create-order` | ✅ | Create a Razorpay order for a wallet top-up |
| — | Payment verification & refunds | ✅ | Exposed via `paymentController` / `refundController` — see Swagger for exact routes |
| `GET` | `/actuator/health` | ❌ | Service health/liveness check |

<details>
<summary><b>Example requests & responses (click to expand)</b></summary>

**Login**
```http
POST {{baseUrl}}/api/login
Content-Type: application/json

{ "email": "user@example.com", "password": "••••••••" }
```
```json
{ "token": "<jwt-access-token>" }
```

**Wallet Deposit**
```http
POST {{baseUrl}}/api/wallet/deposit
Authorization: Bearer <jwt-access-token>

{ "amount": 5000 }
```
```json
{
  "transactionId": 6,
  "senderWalletId": null,
  "receiverWalletId": 2,
  "amount": 5000,
  "type": "DEPOSIT",
  "status": "SUCCESS",
  "createdAt": "2026-09-26T07:54:42.677896541"
}
```

**Wallet Transfer**
```http
POST {{baseUrl}}/api/transactions/transfer
Authorization: Bearer <jwt-access-token>

{ "senderWalletId": 1, "receiverWalletId": 2, "amount": 100 }
```
```json
{
  "transactionId": 7,
  "senderWalletId": 1,
  "receiverWalletId": 2,
  "amount": 100,
  "type": "TRANSFER",
  "status": "SUCCESS",
  "createdAt": "2026-09-26T09:46:16.032932895"
}
```

**Create Payment Order**
```http
POST {{baseUrl}}/api/payments/create-order
Authorization: Bearer <jwt-access-token>

{ "amount": 100 }
```
```json
{ "orderId": "order_xxxxxxxxxxxxxx", "amount": 100, "currency": "INR" }
```

> 🔒 All tokens, order IDs, and credentials above are placeholders — no real secrets are stored in this repository.

</details>

---

## 🏗 Architecture & Design (deep dive)

<details>
<summary><b>System Architecture Diagram (click to expand)</b></summary>

```mermaid
flowchart TB
    subgraph Client["Client Layer"]
        A1[Web / Mobile App]
        A2[Postman / API Consumer]
    end

    subgraph BE["PayMoney Backend on Render"]
        direction TB
        B0["Security Filter Chain - JWT + RBAC"]

        subgraph Controllers["Controllers"]
            C1[userController]
            C2[walletController]
            C3[transactionController]
            C4[paymentController]
            C5[refundController]
        end

        subgraph Services["Services"]
            S1[authService / jwtService]
            S2[walletService]
            S3[transactionService]
            S4[paymentService]
            S5[fraudService]
            S6[auditLogService]
        end

        subgraph Repos["Repositories"]
            R1[userRepository]
            R2[walletRepository]
            R3[transactionRepository]
            R4[paymentRepository]
            R5[auditLogRepository]
        end

        M1[Actuator]
        M2[Swagger UI]
    end

    subgraph External["External"]
        E1[(PostgreSQL)]
        E2[Razorpay]
    end

    A1 --> B0
    A2 --> B0
    B0 --> C1 & C2 & C3 & C4 & C5

    C1 --> S1
    C2 --> S2
    C3 --> S3
    C4 --> S4
    C5 --> S4

    S3 --> S5
    S4 --> S5
    S1 & S2 & S3 & S4 --> S6

    S1 --> R1
    S2 --> R2
    S3 --> R3
    S4 --> R4
    S6 --> R5
    S4 --> E2

    R1 & R2 & R3 & R4 & R5 --> E1

    B0 -.-> M1
    B0 -.-> M2
```

**Flow:** `Client → JWT + RBAC filter → Controller → Service → Repository → PostgreSQL`, with fraud checks and audit logging as cross-cutting concerns and an outbound call from the payment service to Razorpay.

</details>

<details>
<summary><b>Database Design / ERD (click to expand)</b></summary>

```mermaid
erDiagram
    USERS ||--|| WALLETS : owns
    WALLETS ||--o{ TRANSACTIONS : "sends"
    WALLETS ||--o{ TRANSACTIONS : "receives"
    USERS ||--o{ PAYMENTS : creates
    PAYMENTS ||--o{ TRANSACTIONS : settles
    USERS ||--o{ AUDIT_LOGS : "acted by"

    USERS {
        bigint id PK
        varchar email UK
        varchar password
        varchar role
    }
    WALLETS {
        bigint id PK
        bigint user_id FK
        decimal balance
        varchar currency
    }
    TRANSACTIONS {
        bigint transaction_id PK
        bigint sender_wallet_id FK
        bigint receiver_wallet_id FK
        decimal amount
        varchar type
        varchar status
        timestamp created_at
    }
    PAYMENTS {
        varchar order_id PK
        bigint user_id FK
        decimal amount
        varchar currency
        varchar status
    }
    AUDIT_LOGS {
        bigint id PK
        bigint user_id FK
        varchar action
        timestamp created_at
    }
```

</details>

<details>
<summary><b>API Flow Diagrams — Login, Deposit, Transfer, Payment Order (click to expand)</b></summary>

**Login**
```mermaid
sequenceDiagram
    actor U as User
    participant C as userController
    participant S as authService
    participant DB as PostgreSQL

    U->>C: POST /api/login
    C->>S: authenticate(email, password)
    S->>DB: findUserByEmail(email)
    DB-->>S: user record
    S->>S: verify password, generate JWT
    S-->>C: JWT token
    C-->>U: 200 OK - token
```

**Wallet Deposit**
```mermaid
sequenceDiagram
    actor U as User
    participant SEC as JwtAuthenticationFilter
    participant C as walletController
    participant WS as walletService
    participant TS as transactionService
    participant DB as PostgreSQL

    U->>SEC: POST /api/wallet/deposit
    SEC->>C: forward, validated
    C->>WS: deposit(walletId, amount)
    WS->>DB: increase balance
    WS->>TS: recordTransaction(DEPOSIT)
    TS->>DB: insert Transaction
    TS-->>C: TransactionResponse
    C-->>U: 200 OK
```

**Wallet Transfer**
```mermaid
sequenceDiagram
    actor U as User
    participant C as transactionController
    participant TS as transactionService
    participant FS as fraudService
    participant WS as walletService
    participant DB as PostgreSQL

    U->>C: POST /api/transactions/transfer
    C->>TS: transfer(senderId, receiverId, amount)
    TS->>FS: screen transaction
    FS-->>TS: risk verdict
    alt looks safe
        TS->>WS: debit sender, credit receiver
        WS->>DB: update balances atomically
        TS->>DB: insert Transaction - SUCCESS
        TS-->>C: TransactionResponse
        C-->>U: 200 OK
    else flagged or insufficient balance
        TS->>DB: insert Transaction - FAILED
        TS-->>C: error
        C-->>U: 4xx
    end
```

**Payment Order Creation**
```mermaid
sequenceDiagram
    actor U as User
    participant C as paymentController
    participant PS as paymentService
    participant PG as Razorpay
    participant DB as PostgreSQL

    U->>C: POST /api/payments/create-order
    C->>PS: createOrder(userId, amount, currency)
    PS->>PG: create order
    PG-->>PS: orderId
    PS->>DB: persist Payment - CREATED
    PS-->>C: OrderResponse
    C-->>U: 200 OK - orderId, amount, currency
    Note over U,PG: Client completes checkout on the gateway
    Note over PS,DB: Verification later confirms payment
    Note over PS,DB: and credits the wallet via a DEPOSIT transaction
```

</details>

<details>
<summary><b>Project Structure (click to expand)</b></summary>

```
PayMoney/
├── src/main/java/com/PayMoney/
│   ├── Config/          # adminInitializer, openApiConfig, razorpayConfig, SecurityConfig
│   ├── Controller/       # payment, refund, transaction, user, walletController
│   ├── DTO/              # request/response payloads for every endpoint
│   ├── Entity/           # auditLog, idempotency, payment, transaction, user, walletEntity (+ status/type enums)
│   ├── Exception/        # globalExceptionHandler + domain-specific exceptions
│   ├── Mapper/           # transaction, user, walletMapper
│   ├── Repository/       # Spring Data JPA repositories
│   ├── Service/          # authService, fraudService, jwtService, payment/transaction/user/walletService
│   └── PayMoneyApplication.java
├── src/main/resources/
│   ├── db.migration/     # V1__create_initial_schema.sql, V2__create_audit_logs.sql (Flyway)
│   └── application.properties   # reads secrets from environment, not hardcoded
├── Dockerfile
├── .gitignore            # keeps environmentFile.env & target/ out of version control
└── pom.xml
```

> ⚠️ `environmentFile.env` is a local-only file for environment variables (DB credentials, JWT secret, Razorpay keys) and is git-ignored — no real keys appear in this repository or this README.

</details>

---

## 🔐 Security Model

- Stateless JWT authentication — every protected request needs `Authorization: Bearer <JWT>`
- Role-based access control enforced through `SecurityConfig` + a custom `JwtAuthenticationFilter`
- Fraud screening (`fraudService`) and idempotency handling sit in front of payment-sensitive operations
- Every sensitive action is written to an append-only audit log
- `globalExceptionHandler` returns consistent error responses — no stack traces leaked to clients
- No secrets are committed to source control; all keys are injected via environment variables at runtime

---

## 🚀 Getting Started

```bash
git clone https://github.com/VASUSINH/PayMoney.git
cd PayMoney

# set DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`; Flyway auto-applies migrations on boot. Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

**With Docker:**
```bash
docker build -t paymoney-backend .
docker run --env-file environmentFile.env -p 8080:8080 paymoney-backend
```

---

## ✅ Production-Readiness Checklist

- ✅ Stateless JWT authentication with role-based access control
- ✅ Centralized, consistent error handling (`globalExceptionHandler`)
- ✅ Versioned, reviewable database migrations (Flyway)
- ✅ Fraud-screening layer decoupled from core transaction logic
- ✅ Idempotent payment handling to prevent duplicate charges
- ✅ Append-only audit trail for every sensitive action
- ✅ Containerized with Docker for consistent deployments
- ✅ Live health-check endpoint (Actuator) for uptime monitoring
- ✅ Fully documented, explorable API (Swagger/OpenAPI)
- ✅ Deployed and publicly reachable — not just a local demo

---

## 🙏 Acknowledgments

Built independently as a self-directed backend engineering project, referencing official documentation for [Spring Boot](https://spring.io/projects/spring-boot), [Spring Security](https://spring.io/projects/spring-security), and [Razorpay's API docs](https://razorpay.com/docs/) throughout.

## 📄 License

Currently unlicensed — add a `LICENSE` file if you intend to open-source this under a specific license (MIT, Apache-2.0, etc.).

---

<div align="center">

## 👤 Author

**Ayush Sinha**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/ayush-sinha-7611572a1/)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:ayush21052003@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-VASUSINH-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/VASUSINH)

*If this project is useful or interesting, consider ⭐ starring the repo — and feel free to reach out above!*

</div>

