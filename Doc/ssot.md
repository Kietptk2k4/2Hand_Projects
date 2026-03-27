# 🧠 SSOT – 2Hands System (Single Source of Truth)

## 🎯 Mục tiêu

Tài liệu này là **nguồn sự thật duy nhất (SSOT)** cho toàn bộ hệ thống 2Hands.

Mọi AI và developer phải:

- Tuân thủ kiến trúc
- Không tự ý thay đổi contract
- Không phá vỡ flow giữa các service

---

# 🧱 1. System Overview

## 🧭 Kiến trúc tổng thể

```text
Client (Web/Mobile)
        ↓
   API Gateway
        ↓
 ┌──────────────────────────────────────────────┐
 │                Microservices                 │
 │                                              │
 │ Auth Service         Social Service          │
 │ Commerce Service     Payment Service         │
 │ Notification Service Moderation Service      │
 │ Trust & Dispute      Admin Service           │
 │                                              │
 └──────────────────────────────────────────────┘
        ↓
      Kafka (Event Bus)
        ↓
   Async Processing / Notification
```

---

# 🧩 2. Tech Stack

## Backend

- Java 21
- Spring Boot 3
- PostgreSQL (Neon)
- Redis (Cloud)
- Kafka (Docker / Cloud)

---

## Frontend

- React / Next.js
- TypeScript
- REST API

---

# 🧱 3. Microservices Responsibility

---

## 🔐 Auth Service

### Responsibility

- Authentication (login, register)
- Authorization (RBAC)
- JWT + Refresh Token
- OTP (email/phone)
- Rate limiting (Redis)
- Publish auth events

---

## 👤 Social Service

### Responsibility

- User profile
- Follow / unfollow
- Social graph
- User settings
- Activity feed (basic)

---

## 🛒 Commerce Service

### Responsibility

- Product listing
- Inventory
- Order creation
- Cart
- Pricing logic

---

## 💳 Payment Service

### Responsibility

- Payment processing
- Transaction tracking
- Integration với payment gateway
- Refund logic

---

## ⚖️ Trust & Dispute Service

### Responsibility

- Handle disputes
- Fraud detection (basic rule-based)
- Case management
- Flag suspicious activity

---

## 🛡️ Moderation Service

### Responsibility

- Content moderation
- Report system
- Ban / restrict content
- AI moderation integration (optional)

---

## 🔔 Notification Service

### Responsibility

- Email
- SMS
- Push notification
- Consume Kafka events

---

## 🧑‍💼 Admin Service

### Responsibility

- Admin dashboard
- Manage users
- Manage reports
- System monitoring (basic)

---

# 📐 4. Architecture (Clean Architecture per service)

```text
delivery → application → domain
infrastructure → domain
```

---

## 📁 Standard Structure (mọi service)

```text
com.twohands.{service}
│
├── domain/
├── application/
├── infrastructure/
├── delivery/
└── shared/
```

---

# 🧠 5. Communication

## 🔹 Sync (REST)

```text
Client → API Gateway → Service
```

---

## 🔹 Async (Kafka) 🔥

```text
Service → Kafka → Service
```

---

# 📩 6. Kafka Contract

## Topic

```text
system.events
```

---

## Event format

```json
{
  "eventId": "uuid",
  "eventType": "auth.user.created",
  "timestamp": "ISO",
  "source": "auth-service",
  "data": {}
}
```

---

## Event Naming

```text
{service}.{entity}.{action}
```

---

## Core Events

```text
auth.user.created
auth.user.logged_in
commerce.order.created
payment.transaction.success
notification.sent
moderation.content.flagged
dispute.case.created
```

---

# ⚡ 7. Redis Contract

## Key format

```text
{service}:{feature}:{identifier}
```

---

## Examples

```text
auth:otp:{email}
auth:rate_limit:{ip}
auth:session:{userId}

commerce:cart:{userId}

payment:lock:{orderId}

moderation:rate_limit:{userId}
```

---

# 🔐 8. Security Rules

- Password phải hash (bcrypt/argon2)
- Không lưu raw token
- Rate limit login (Redis)
- OTP expire
- Validate input
- Không expose sensitive data
- JWT expire ngắn (15–30 phút)

---

# 🌐 9. API Contract (Auth)

## POST /auth/register

```json
{
  "email": "string",
  "password": "string"
}
```

---

## POST /auth/login

```json
{
  "email": "string",
  "password": "string"
}
```

---

## Response

```json
{
  "accessToken": "string",
  "refreshToken": "string"
}
```

---

# 🧠 10. Design Patterns

## Bắt buộc

- Repository
- UseCase
- Dependency Injection

---

## Khuyến khích

- Strategy (login/payment)
- Factory
- Builder
- Event-driven (Kafka)
- Decorator (Redis cache)

---

# 🔄 11. Data Ownership (RẤT QUAN TRỌNG)

👉 Mỗi service có DB riêng

| Service    | Own Data       |
| ---------- | -------------- |
| Auth       | user, token    |
| Social     | profile        |
| Commerce   | product, order |
| Payment    | transaction    |
| Moderation | report         |
| Dispute    | case           |

---

❌ Không được query DB service khác

---

# 🔔 12. Notification Flow (Example)

```text
auth.user.created
→ Kafka
→ Notification Service
→ gửi email
```

---

# 🧪 13. Testing

- Unit test (usecase)
- Integration test (API)
- Mock external services

---

# ⚠️ 14. Non-negotiable Rules

- Không viết business logic trong controller
- Không bypass Redis trong auth
- Không hardcode config
- Không coupling giữa service
- Không sync call khi có thể dùng Kafka

---

# 🎨 15. Frontend Contract (Stitch)

- Gọi API qua API Gateway
- Không gọi trực tiếp service
- Token:
  - access token → memory
  - refresh token → httpOnly cookie

---

# 🚀 16. Scalability Rules

- Stateless service
- Horizontal scaling
- Event-driven
- Cache bằng Redis

---

# 🧭 17. AI Instruction

AI phải:

- Generate code theo Clean Architecture
- Tuân thủ SOLID
- Ưu tiên security
- Không viết code demo
- Kiểm tra lỗi tiềm ẩn

---

# 🔥 18. Final Goal

Hệ thống phải:

- Microservices-ready
- Event-driven
- Secure
- Scalable
- Production-ready

---

# 📌 19. SSOT Rule

👉 Nếu có mâu thuẫn:

> Tài liệu này là nguồn sự thật duy nhất

---
