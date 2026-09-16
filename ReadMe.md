# 💰 PayMoney — Digital Wallet & Payment Processing Backend

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot" />
  <img src="https://img.shields.io/badge/Spring%20Security-JWT-blue?style=flat-square&logo=springsecurity" />
  <img src="https://img.shields.io/badge/PostgreSQL-Database-336791?style=flat-square&logo=postgresql" />
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=flat-square&logo=apachemaven" />
  <img src="https://img.shields.io/badge/Status-In%20Progress-yellow?style=flat-square" />
</p>

<p align="center">
  <b>A production-grade, secure, and scalable digital wallet & payment backend</b><br/>
  built with Spring Boot, PostgreSQL, JWT authentication, and role-based access control —
  designed the way real fintech systems (Paytm, Razorpay, Stripe-style engines) are architected.
</p>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Core Features](#-core-features)
- [Authentication & Security Flow](#-authentication--security-flow)
- [Database Design (ER Diagram)](#-database-design-er-diagram)
- [Role-Based Access Control](#-role-based-access-control-rbac)
- [Transaction Lifecycle](#-transaction-lifecycle)
- [Project Structure](#-project-structure)
- [Development Roadmap](#-development-roadmap)
- [Getting Started](#-getting-started)
- [Skills Demonstrated](#-skills-demonstrated)

---

## 🧭 Overview

**PayMoney** is a backend system that simulates the core of a real-world digital wallet
and payment platform — similar in spirit to systems like Paytm or a simplified Stripe.
It is being built module-by-module (phase-by-phase) with production practices:
clean layering, DTO-based contracts, centralized exception handling, JWT-secured
authentication, RBAC-based authorization, and ACID-safe wallet transactions.

The project is in **active development** — core banking primitives (users, wallets,
authentication, authorization, transactions) are complete, and payment-gateway-grade
features (refunds, fraud detection, idempotency, webhooks, audit logs) are next.

---

## 🏗 System Architecture

```mermaid
flowchart TB
    subgraph Client["🌐 Client Layer"]
        A[Web / Mobile / Postman Client]
    end

    subgraph API["⚙️ API Layer — Spring Boot"]
        B[Controller Layer<br/>REST Endpoints]
        C[DTO Layer<br/>Request / Response Validation]
        D[Exception Handler<br/>Global @ControllerAdvice]
    end

    subgraph Security["🔐 Security Layer"]
        E[JWT Filter Chain]
        F[Spring Security<br/>Authentication Manager]
        G[RBAC Authorization<br/>Role & Permission Checks]
    end

    subgraph Business["🧠 Business Logic Layer"]
        H[User Service]
        I[Wallet Service]
        J[Transaction Service<br/>@Transactional]
        K[Payment Gateway Service<br/>🚧 Upcoming]
        L[Fraud Detection Engine<br/>🚧 Upcoming]
    end

    subgraph Persistence["🗄 Persistence Layer"]
        M[Spring Data JPA / Hibernate]
        N[(PostgreSQL Database)]
        O[Flyway Migrations<br/>🚧 Upcoming]
    end

    subgraph CrossCutting["📋 Cross-Cutting Concerns"]
        P[Audit Logging 🚧]
        Q[Idempotency Keys 🚧]
        R[Webhooks 🚧]
        S[Swagger / Actuator 🚧]
    end

    A --> B --> C --> E --> F --> G --> H
    G --> I --> J --> M
    J --> K --> L
    M --> N
    O -.-> N
    J -.-> P
    J -.-> Q
    K -.-> R
    B -.-> S
    D -.-> B
```

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.x |
| **Build Tool** | Maven |
| **Security** | Spring Security + JWT (JSON Web Tokens) |
| **Authorization** | Role-Based Access Control (RBAC) |
| **Database** | PostgreSQL |
| **ORM** | Spring Data JPA / Hibernate |
| **Validation** | Jakarta Bean Validation (`@Valid`, DTOs) |
| **Error Handling** | Global Exception Handling (`@ControllerAdvice`) |
| **Transactions** | Spring `@Transactional` (ACID-safe wallet operations) |
| **Version Control** | Git & GitHub |
| **Planned** | Flyway, Swagger/OpenAPI, Spring Actuator, Scheduler, Docker, JUnit + Mockito |

---

## ✅ Core Features

### Implemented

| # | Module | Description |
|---|---|---|
| 1 | **User Management** | User registration, profile management, PostgreSQL persistence via JPA |
| 2 | **DTO + Validation** | Clean request/response contracts, input validation, centralized exception handling |
| 3 | **Authentication (JWT)** | Stateless login/signup flow secured with Spring Security + JWT access tokens |
| 4 | **Authorization (RBAC)** | Role-based endpoint protection (e.g. `USER`, `ADMIN`) using method/URL-level security |
| 5 | **Wallet Management** | Create & manage user wallets, balance tracking, wallet-to-user linkage |
| 6 | **Transactions** | Atomic fund transfers using `@Transactional`, preventing race conditions & partial updates |

### 🚧 In Progress / Upcoming

| # | Module | Purpose |
|---|---|---|
| 7 | **Payment Gateway Integration** | Connect wallet to external payment providers (e.g. Razorpay/Stripe-style flow) |
| 8 | **Refunds** | Reverse transactions safely with full audit trail |
| 9 | **Fraud Detection** | Rule-based checks (velocity limits, suspicious patterns) before transaction approval |
| 10 | **Idempotency** | Idempotency-key mechanism to make retried payment requests safe |
| 11 | **Webhooks** | Async event notifications for payment status changes |
| 12 | **Audit Logging** | Immutable log of every sensitive action (who did what, when) |
| 13 | **Flyway + Swagger + Actuator + Scheduler** | DB versioning, live API docs, health monitoring, scheduled jobs |
| 14 | **Testing + Docker + Polish** | Unit/integration tests (JUnit + Mockito), containerization, final hardening |

---

## 🔐 Authentication & Security Flow

```mermaid
sequenceDiagram
    actor U as User
    participant C as Client App
    participant AC as AuthController
    participant SS as Spring Security
    participant JWT as JWT Provider
    participant DB as PostgreSQL

    U->>C: Enter credentials
    C->>AC: POST /api/auth/login
    AC->>SS: Authenticate(username, password)
    SS->>DB: Fetch user & roles
    DB-->>SS: User details + hashed password
    SS-->>AC: Authentication success
    AC->>JWT: Generate signed JWT (claims: userId, roles)
    JWT-->>AC: Access Token
    AC-->>C: 200 OK + JWT Token

    Note over C,AC: Subsequent requests
    C->>AC: GET /api/wallet (Authorization: Bearer <JWT>)
    AC->>SS: JWT Filter validates token
    SS->>SS: Extract roles → RBAC check
    alt Authorized
        SS-->>AC: Proceed to Controller
        AC-->>C: 200 OK + Data
    else Unauthorized
        SS-->>C: 403 Forbidden
    end
```

---

## 🗄 Database Design (ER Diagram)

```mermaid
erDiagram
    USER ||--o{ WALLET : owns
    USER ||--o{ ROLE : "assigned via"
    WALLET ||--o{ TRANSACTION : "sender in"
    WALLET ||--o{ TRANSACTION : "receiver in"

    USER {
        UUID id PK
        string fullName
        string email
        string password
        datetime createdAt
    }

    ROLE {
        UUID id PK
        string name
    }

    WALLET {
        UUID id PK
        UUID userId FK
        decimal balance
        string currency
        datetime updatedAt
    }

    TRANSACTION {
        UUID id PK
        UUID senderWalletId FK
        UUID receiverWalletId FK
        decimal amount
        string status
        string type
        datetime createdAt
    }
```

---

## 🛡 Role-Based Access Control (RBAC)

```mermaid
flowchart LR
    subgraph Roles
        ADMIN["👑 ADMIN"]
        USER["🙋 USER"]
    end

    subgraph Permissions
        P1[Manage All Users]
        P2[View All Transactions]
        P3[Manage Own Wallet]
        P4[Send / Receive Money]
        P5[View Own Transaction History]
    end

    ADMIN --> P1
    ADMIN --> P2
    ADMIN --> P3
    ADMIN --> P4
    ADMIN --> P5

    USER --> P3
    USER --> P4
    USER --> P5
```

---

## 💸 Transaction Lifecycle

```mermaid
stateDiagram-v2
    [*] --> INITIATED : User requests transfer
    INITIATED --> VALIDATED : Balance & recipient checked
    VALIDATED --> PROCESSING : @Transactional block begins
    PROCESSING --> SUCCESS : Debit + Credit committed atomically
    PROCESSING --> FAILED : Exception → rollback
    SUCCESS --> [*]
    FAILED --> [*]

    note right of PROCESSING
        Fraud check, idempotency key
        validation, and audit logging
        will plug in here (upcoming)
    end note
```

---

## 📁 Project Structure

```
paymoney/
├── src/main/java/com/paymoney/
│   ├── config/            # Security config, JWT filters, beans
│   ├── controller/        # REST controllers (User, Wallet, Auth, Transaction)
│   ├── dto/                # Request/Response DTOs
│   ├── entity/            # JPA entities (User, Wallet, Transaction, Role)
│   ├── exception/         # Custom exceptions + GlobalExceptionHandler
│   ├── repository/        # Spring Data JPA repositories
│   ├── security/          # JWT provider, filters, UserDetailsService
│   ├── service/           # Business logic (User, Wallet, Transaction)
│   └── PayMoneyApplication.java
├── src/main/resources/
│   └── application.yml
├── src/test/               # 🚧 JUnit + Mockito tests (upcoming)
├── pom.xml
└── README.md
```

---

## 🗺 Development Roadmap

| Phase | Module | Status |
|---|---|---|
| 1 | Project Setup + Git | ✅ Done |
| 2 | User Management + PostgreSQL + JPA | ✅ Done |
| 3 | DTO + Validation + Exception Handling | ✅ Done |
| 4 | Authentication + Spring Security + JWT | ✅ Done |
| 5 | Authorization + RBAC | ✅ Done |
| 6 | Wallet Management | ✅ Done |
| 7 | Transactions + `@Transactional` | ✅ Done |
| 8 | Payment Gateway | ⬜ Planned |
| 9 | Refunds | ⬜ Planned |
| 10 | Fraud Detection | ⬜ Planned |
| 11 | Idempotency | ⬜ Planned |
| 12 | Webhooks | ⬜ Planned |
| 13 | Audit Logging | ⬜ Planned |
| 14 | Flyway + Swagger + Actuator + Scheduler | ⬜ Planned |
| 15 | Testing + Docker + Final Polish | ⬜ Planned |

**Progress: 7 / 15 phases complete (~47%)**

```mermaid
gantt
    title PayMoney Development Progress
    dateFormat  X
    axisFormat %s
    section Completed
    Project Setup + Git            :done, p1, 0, 1
    User Management + JPA          :done, p2, 1, 2
    DTO + Validation                :done, p3, 2, 3
    Auth + JWT                      :done, p4, 3, 4
    RBAC                            :done, p5, 4, 5
    Wallet Management                :done, p6, 5, 6
    Transactions                    :done, p7, 6, 7
    section Upcoming
    Payment Gateway                  :active, p8, 7, 8
    Refunds                          :p9, 8, 9
    Fraud Detection                  :p10, 9, 10
    Idempotency                      :p11, 10, 11
    Webhooks                         :p12, 11, 12
    Audit Logging                    :p13, 12, 13
    Flyway+Swagger+Actuator          :p14, 13, 14
    Testing+Docker+Polish            :p15, 14, 15
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL 14+

### Setup

```bash
# Clone the repository
git clone https://github.com/<your-username>/paymoney.git
cd paymoney

# Configure database credentials in src/main/resources/application.yml

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Sample Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/auth/signup` | Register a new user | ❌ |
| POST | `/api/auth/login` | Login and receive JWT | ❌ |
| GET | `/api/wallet` | Get current user's wallet | ✅ |
| POST | `/api/transactions/transfer` | Transfer funds between wallets | ✅ |
| GET | `/api/admin/users` | View all users (Admin only) | ✅ (ADMIN) |

> Update the table above once your actual controller endpoints are finalized.

---

## 🧠 Skills Demonstrated

- **Backend Architecture** — layered design (Controller → Service → Repository)
- **Security Engineering** — stateless JWT auth, password hashing, RBAC authorization
- **Data Modeling** — relational schema design for financial data integrity
- **Transaction Management** — ACID-safe money transfers using `@Transactional`
- **API Design** — RESTful conventions, DTO-based contracts, input validation
- **Error Handling** — centralized, consistent exception responses
- **Fintech Domain Knowledge** — wallets, ledgers, refunds, fraud checks, idempotency, webhooks
- **Clean Code Practices** — separation of concerns, Git-based version control

---

<p align="center">
  <i>Built as a hands-on deep dive into how real-world fintech backends are engineered.</i>
</p>
