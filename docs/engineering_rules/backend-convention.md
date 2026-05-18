# backend-convention.md

# Backend Development Convention - 2Hands

Version: 1.0
Architecture Style:

* Domain-Centric Architecture
* Clean Architecture (DDD-lite)
* Modular Monolith
* Microservice-ready
* Event-Driven Architecture

---

# 1. Purpose

Tài liệu này quy định:

* coding standards
* package structure
* architecture rules
* naming conventions
* engineering practices

cho toàn bộ backend system của dự án 2Hands.

Mục tiêu:

* consistency giữa services
* maintainability
* scalability
* clean code
* dễ onboarding
* dễ tách microservice trong tương lai

---

# 2. Standard Project Structure

Mỗi service phải tuân theo structure sau:

```txt
src/main/java/com/twohands/{service-name}

├── application
├── delivery
├── domain
├── infrastructure
├── common
├── config
├── constant
├── exception
└── env
```

---

# 3. Layer Responsibilities

## 3.1 application

Application layer chứa:

* use case
* orchestration logic
* transaction boundary
* coordination giữa domain và infrastructure

Ví dụ:

```txt
application/auth
 ├── login
 ├── register
 ├── verify
 └── refresh
```

Ví dụ class:

```txt
LoginUseCase
RegisterUserUseCase
VerifyEmailUseCase
```

---

## 3.2 domain

Domain layer là trung tâm business system.

Chỉ chứa:

* domain entity
* value object
* business rule
* domain service
* repository interface

Ví dụ:

```txt
domain
 ├── user
 ├── role
 ├── permission
 ├── session
 └── oauth
```

Domain layer KHÔNG được phụ thuộc:

* Spring Framework
* Kafka
* Redis
* HTTP
* Security Framework
* Infrastructure implementation

---

## 3.3 delivery

Delivery layer expose API ra bên ngoài.

Ví dụ:

```txt
delivery/http
 ├── auth
 │    ├── request
 │    ├── response
 │    ├── mapper
 │    └── AuthController
```

Controller chỉ được:

* nhận request
* validate request format
* gọi application layer
* trả response

Controller KHÔNG được:

* chứa business logic
* gọi repository trực tiếp
* thao tác transaction

---

## 3.4 infrastructure

Infrastructure layer chứa technical implementation.

Ví dụ:

```txt
infrastructure
 ├── persistence
 ├── cache
 ├── security
 ├── message
 └── external
```

Bao gồm:

* JPA
* Redis
* Kafka/RabbitMQ
* JWT
* External API
* Security implementation

---

# 4. Dependency Rules

Dependency chỉ được đi theo chiều:

```txt
delivery
   ↓
application
   ↓
domain
   ↓
infrastructure
```

Forbidden:

* delivery gọi database trực tiếp
* domain phụ thuộc framework
* infrastructure gọi delivery
* controller gọi repository

---

# 5. DTO Convention

DTO chỉ được tồn tại trong delivery layer.

Ví dụ:

```txt
delivery/http/auth/request/LoginRequest
delivery/http/auth/response/LoginResponse
```

Naming:

* Request DTO → `SomethingRequest`
* Response DTO → `SomethingResponse`

Forbidden:

* business logic trong DTO
* validation business trong DTO
* expose entity trực tiếp

---

# 6. Repository Convention

Repository interface đặt trong domain.

Ví dụ:

```txt
domain/user/UserRepository
```

Repository implementation đặt trong infrastructure.

Ví dụ:

```txt
infrastructure/persistence/repository/JpaUserRepository
```

---

# 7. Mapper Convention

Khuyến khích sử dụng:

* MapStruct

Ví dụ:

```txt
delivery/http/auth/mapper
infrastructure/persistence/mapper
```

Mapper chỉ dùng để:

* DTO ↔ Domain
* Domain ↔ Persistence Entity

Không chứa business logic.

---

# 8. Exception Handling

Mỗi service bắt buộc có:

```txt
GlobalExceptionHandler
```

Response format chuẩn:

```json
{
  "success": false,
  "code": "AUTH_INVALID_CREDENTIAL",
  "message": "Invalid credentials",
  "timestamp": "2026-05-14T10:00:00Z"
}
```

Custom Exception naming:

```txt
UserNotFoundException
InvalidOtpException
AccessDeniedException
```

Không sử dụng:

* RuntimeException chung chung
* Exception không có meaning business

---

# 9. Transaction Convention

Mọi use case:

* save/update/delete
* publish event

bắt buộc dùng:

```java
@Transactional
```

Transaction boundary phải nằm ở application layer.

Ví dụ:

```txt
RegisterUserUseCase
CreateOrderUseCase
```

---

# 10. Outbox Pattern Convention

Flow chuẩn:

```txt
BEGIN TRANSACTION
    ↓
Save Business Entity
    ↓
Save Outbox Event
    ↓
COMMIT
```

Event publisher không được publish trực tiếp trong controller.

Worker riêng sẽ:

* poll outbox table
* publish event
* update status

---

# 11. Security Convention

## Password

* Password phải hash bằng BCrypt
* Không lưu plaintext password

---

## JWT

Mọi protected API phải dùng JWT authentication.

---

## Authorization

Bắt buộc validate:

* ownership
* role
* permission

Ví dụ:

* User A không được update resource của User B

---

## Structure

```txt
infrastructure/security
 ├── jwt
 ├── filter
 ├── config
 ├── password
 └── context
```

---

# 12. Logging Convention

Framework:

* SLF4J

Log levels:

| Level | Usage                 |
| ----- | --------------------- |
| INFO  | Business flow chính   |
| WARN  | Hành vi bất thường    |
| ERROR | Exception/failure     |
| DEBUG | Development debugging |

Không log:

* password
* token
* refresh token
* otp
* secret key

---

# 13. Naming Convention

| Component  | Convention       |
| ---------- | ---------------- |
| Class      | PascalCase       |
| Method     | camelCase        |
| Variable   | camelCase        |
| Package    | lowercase        |
| Constant   | UPPER_SNAKE_CASE |
| Enum       | PascalCase       |
| Enum Value | UPPER_SNAKE_CASE |

---

# 14. Use Case Naming

Ví dụ:

```txt
LoginUseCase
RegisterUserUseCase
CreateOrderUseCase
VerifyEmailUseCase
```

Không dùng:

* GenericService
* BaseService
* AbstractCrudManager

trừ khi có use-case thực tế.

---

# 15. Event Naming Convention

Domain event:

```txt
UserRegisteredEvent
OrderCreatedEvent
PaymentCompletedEvent
```

Kafka/RabbitMQ topic:

```txt
auth.user.registered
commerce.order.created
notification.email.sent
```

---

# 16. Redis Key Convention

Format:

```txt
{service}:{domain}:{purpose}:{id}
```

Ví dụ:

```txt
auth:otp:email:123
auth:ratelimit:login:ip
commerce:product:stock:15
```

---

# 17. API Response Convention

Success response:

```json
{
  "success": true,
  "data": {},
  "meta": {}
}
```

Error response:

```json
{
  "success": false,
  "code": "AUTH_INVALID_OTP",
  "message": "OTP is invalid",
  "timestamp": "2026-05-14T10:00:00Z"
}
```

---

# 18. Testing Convention

Structure:

```txt
src/test
 ├── unit
 ├── integration
 └── e2e
```

Rules:

* unit test cho business logic quan trọng
* integration test cho DB/message broker
* không test getter/setter trivial

---

# 19. Clean Code Rules

Bắt buộc:

* class đúng responsibility
* method ngắn gọn
* không God Service
* không business logic trong controller
* không generic abstraction vô nghĩa

Không over-engineer:

* không interface mọi thứ
* không abstraction giả
* không BaseEverything pattern

---

# 20. Final Goal

Codebase phải:

* clean
* scalable
* maintainable
* consistent
* microservice-ready
* dễ onboarding
* dễ evolve trong tương lai

Mọi service trong hệ thống phải tuân thủ convention này để đảm bảo consistency toàn hệ thống.
