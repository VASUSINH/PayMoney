---

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
  built with Spring Boot, PostgreSQL, JWT authentication, and role-based access control — designed the way real fintech systems (Paytm, Razorpay, Stripe-style engines) are architected.
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
- [Key Backend Design Decisions](#-key-backend-design-decisions)
- [Project Completion Snapshot](#-project-completion-snapshot)
- [Skills Demonstrated](#-skills-demonstrated)

---

## 🧭 Overview

**PayMoney** is a backend system simulating core operations of a real-world digital wallet and payment platform. It mirrors principles from leaders like Paytm or Stripe through modular, production-grade implementations: clear layering, secure JWT authentication, role-based authorization, and ACID-compliant wallet transactions.

The project is actively evolving — foundational features (users, wallets, auth, transactions) are complete, with advanced payment gateway, refunds, fraud detection, and audit logging slated next.

---

## 🏗 System Architecture

Requests flow top-down: **Client → API → Security → Business Logic → Persistence**, with cross-cutting modules integrated separately to maintain clean paths.

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

    classDef client fill:#3B82F680,stroke:#2563EB,stroke-width:2px,color:#0F172A;
    classDef api fill:#10B98180,stroke:#065F46,stroke-width:2px,color:#0F172A;
    classDef security fill:#F9731680,stroke:#7C2D12,stroke-width:2px,color:#0F172A;
    classDef business fill:#8B5CF680,stroke:#4C1D95,stroke-width:2px,color:#0F172A;
    classDef persistence fill:#2563EB80,stroke:#1D4ED8,stroke-width:2px,color:#0F172A;
    classDef cross fill:#64748B80,stroke:#475569,stroke-width:2px,color:#0F172A;
    classDef upcoming fill:#FACC1580,stroke:#B45309,stroke-width:2px,color:#0F172A;

    class A client;
    class B,C,D api;
    class E,F,G security;
    class H,I,J,K,L business;
    class M,N,O persistence;
    class P,Q,R,S cross;
    class K,L,O,P,Q,R,S upcoming;
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

Two phases, read flow from top-left to bottom-right. Solid colored bands indicate phases.

```mermaid
sequenceDiagram
    autonumber
    actor U as 👤 User
    participant C as 📱 Client App
    participant AC as ⚙️ AuthController
    participant SS as 🔐 Spring Security
    participant JWT as 🔑 JWT Provider
    participant DB as 🗄 PostgreSQL

    rect rgb(59,130,246,0.2)
    Note over U,DB: Phase 1 — Login & Token Issuance
    U->>C: Enter credentials
    C->>AC: POST /api/auth/login
    AC->>SS: Authenticate(username, password)
    SS->>DB: Fetch user & roles
    DB-->>SS: User details + hashed password
    SS-->>AC: Authentication success
    AC->>JWT: Generate signed JWT (claims: userId, roles)
    JWT-->>AC: Access Token
    AC-->>C: 200 OK + JWT Token
    end

    rect rgb(249,115,22,0.15)
    Note over C,SS: Phase 2 — Every Protected Request
    C->>AC: GET /api/wallet (Authorization: Bearer <JWT>)
    AC->>SS: JWT Filter validates token
    SS->>SS: Extract roles → RBAC check
    alt Authorized
        SS-->>AC: Proceed to Controller
        AC-->>C: 200 OK + Data
    else Unauthorized
        SS-->>C: 403 Forbidden
    end
    end
```

> The blue band covers the initial identity proofing (token issue), the orange band covers continuous authorization checks ensuring secure access control.

---

## 🗄 Database Design (ER Diagram)

Clear solid shapes in diagrams ensure relationships stand out.

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

Improved fill colors maximize clarity with strong contrast:

```mermaid
flowchart LR
    subgraph Roles["👥 Roles"]
        ADMIN["👑 ADMIN"]
        USER["🙋 USER"]
    end

    subgraph Permissions["🔑 Permissions"]
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

    classDef admin fill:#fee2e2,stroke:#dc2626,stroke-width:2px,color:#991B1B;
    classDef user fill:#dbeafe,stroke:#2563eb,stroke-width:2px,color:#1e40af;
    classDef permission fill:#d1fae5,stroke:#16a34a,stroke-width:2px,color:#14532d;

    class ADMIN admin;
    class USER user;
    class P1,P2,P3,P4,P5 permission;
```

> The red shade highlights admin-only privileges, blue marks normal user access, and green signals accessible permissions for either role.

---

## 💸 Transaction Lifecycle

Distinct solid fills highlight flow states.

```mermaid
stateDiagram-v2
    direction LR
    [*] --> INITIATED : User requests transfer
    INITIATED --> VALIDATED : Balance & recipient checked
    VALIDATED --> PROCESSING : @Transactional block begins
    PROCESSING --> SUCCESS : Debit + Credit committed atomically
    PROCESSING --> FAILED : Exception → rollback
    SUCCESS --> [*]
    FAILED --> [*]

    classDef inflight fill:#fef3c7,stroke:#b45309,stroke-width:2px,color:#78350f;
    classDef success fill:#d1fae5,stroke:#15803d,stroke-width:2px,color:#14532d;
    classDef failure fill:#fee2e2,stroke:#b91c1c,stroke-width:2px,color:#7f1d1d;

    class INITIATED,VALIDATED,PROCESSING inflight;
    class SUCCESS success;
    class FAILED failure;

    note right of PROCESSING
        Fraud check, idempotency key
        validation, and audit logging
        will plug in here (upcoming)
    end note
```

---
## 📁 Project Structure

```text

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
---
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

---

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

## 🔎 Key Backend Design Decisions

With improved diagrams using solid fills for better visual impact and clarifying notes:

### 1. Request → Security → Business Logic → Database

```mermaid
flowchart LR
    A["🌐 Client Request"] --> B["⚙️ Controller"]
    B --> C["🧾 DTO + Validation"]
    C --> D["🔐 JWT + RBAC"]
    D --> E["🧠 Service Layer"]
    E --> F["🗂 Repository / JPA"]
    F --> G[("🗄 PostgreSQL")]

    classDef request fill:#3b82f680,stroke:#1e40af,stroke-width:2px,color:#111827;
    classDef validation fill:#10b98180,stroke:#047857,stroke-width:2px,color:#111827;
    classDef security fill:#f9731680,stroke:#b45309,stroke-width:2px,color:#111827;
    classDef service fill:#8b5cf680,stroke:#5b21b6,stroke-width:2px,color:#111827;
    classDef db fill:#2563eb80,stroke:#1e40af,stroke-width:2px,color:#111827;

    class A request;
    class B,C validation;
    class D security;
    class E service;
    class F,G db;
```

### 2. Money Transfer: Atomic Debit + Credit

```mermaid
flowchart LR
    A["📝 Transfer Request"] --> B["🔎 Validate Sender + Receiver"]
    B --> C["💰 Check Balance"]
    C --> D["🔒 Begin @Transactional"]
    D --> E["➖ Debit Sender"]
    E --> F["➕ Credit Receiver"]
    F --> G["✅ Commit"]
    G --> H["🎉 Transfer SUCCESS"]

    D -. "Any exception" .-> I["↩️ Rollback"]
    I --> J["❌ Transfer FAILED"]

    classDef request fill:#3b82f680,stroke:#1e40af,stroke-width:2px,color:#111827;
    classDef validation fill:#10b98180,stroke:#047857,stroke-width:2px,color:#111827;
    classDef transaction fill:#f9731680,stroke:#b45309,stroke-width:2px,color:#111827;
    classDef success fill:#d1fae580,stroke:#166534,stroke-width:2px,color:#111827;
    classDef failure fill:#fee2e280,stroke:#991b1b,stroke-width:2px,color:#111827;

    class A request;
    class B,C validation;
    class D,E,F transaction;
    class G,H success;
    class I,J failure;
```

### 3. Authentication vs Authorization

(unchanged — concise table)

### 4. Upcoming Fintech Safety Layer

Clear blocks emphasize different stages.

```mermaid
flowchart TB
    A["💳 Payment / Transfer Request"]
    B["🔁 Idempotency Check"]
    C["✅ Validation"]
    D["🚨 Fraud Rules"]
    E["🔒 Transactional Processing"]
    F["🌐 External Gateway / Payment Provider"]
    G["📡 Webhook"]
    H["📋 Audit Log"]
    I["🏁 Final Payment State"]

    A --> B --> C --> D --> E
    E --> F
    F --> G --> I
    E --> H
    I --> H

    classDef request fill:#3b82f680,stroke:#1e40af,stroke-width:2px,color:#111827;
    classDef control fill:#f9731680,stroke:#b45309,stroke-width:2px,color:#111827;
    classDef processing fill:#8b5cf680,stroke:#5b21b6,stroke-width:2px,color:#111827;
    classDef external fill:#e0e7ff80,stroke:#4338ca,stroke-width:2px,color:#111827;
    classDef audit fill:#d1fae580,stroke:#166534,stroke-width:2px,color:#111827;

    class A request;
    class B,C,D control;
    class E,I processing;
    class F,G external;
    class H audit;
```

---

## 📊 Project Completion Snapshot

(unchanged)

---

### 🎯 What Makes the Project End-to-End

Added clarity below:

This end-to-end fintech backend roadmap reflects a production mindset, starting with identity and security pillars immediately followed by robust wallet and transaction handling — then progressively layering payment integrations, safety checks, retry mechanisms, asynchronous events, comprehensive audit trails, and finally testing and deployment readiness.

This approach guarantees a strong foundation that supports real-world financial use cases with compliance and operational safety in mind.
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
  <i>Built as a hands-on deep dive into how real-world fintech backends are engineered, emphasizing clarity, safety, and extensibility.</i>
</p>


---

## 👨‍💻 Author

**Ayush Sinha**

This project is designed and developed by **Ayush Sinha** as a hands-on
implementation of a production-oriented fintech backend using Java and Spring Boot.

- GitHub: [VASUSINH](https://github.com/VASUSINH)
- Project: [PayMoney](https://github.com/VASUSINH/PayMoney)

---

## ©️ Copyright & Usage

**Copyright © 2026 Ayush Sinha. All Rights Reserved.**

For permission or licensing inquiries, please contact the author through
GitHub.

**All Rights Reserved.**
---

