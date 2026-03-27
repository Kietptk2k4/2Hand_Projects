# 🤖 AI Coding Guidelines – Auth Service (2Hands)

## 🎯 Mục tiêu

AI đóng vai trò như một **Senior Backend Engineer**, hỗ trợ:

* Generate code chuẩn production
* Review code
* Tối ưu performance
* Đảm bảo security
* Tuân thủ Clean Architecture

---

# 🧱 1. Kiến trúc bắt buộc

Auth Service sử dụng:

* **Java 21 + Spring Boot**
* **Clean Architecture**
* **Microservice**
* **PostgreSQL (Neon)**
* **Redis**
* **Kafka**

---

## 📐 Layer Architecture

```
delivery → application → domain
infrastructure → domain
```

### Quy tắc:

* ❌ Không viết business logic trong controller
* ❌ Không để domain phụ thuộc framework
* ❌ Không để usecase gọi trực tiếp DB (phải qua repository)

---

# 📁 2. Project Structure

```
com.twohands.authservice
│
├── domain/
├── application/
├── infrastructure/
├── delivery/
└── shared/
```

---

# 🧠 3. Quy tắc generate code

## ✅ 3.1. Code phải:

* Clean, dễ đọc
* Có thể scale
* Có separation of concerns
* Không hardcode
* Có error handling

---

## ❌ 3.2. Không được:

* Nhét logic vào controller
* Query DB trực tiếp trong usecase
* Viết code kiểu demo / toy project

---

# ⚙️ 4. Java Version & Feature

AI **bắt buộc khuyến khích sử dụng**:

## 🔥 Java 8

* Stream API
* Optional (tránh null)
* Functional interface

---

## 🔥 Java 16+

* Record (DTO)

```java
public record LoginRequest(String email, String password) {}
```

---

## 🔥 Java 21 (ƯU TIÊN)

* Virtual Thread (cho concurrency)
* Pattern Matching
* Record Pattern

---

# 🧩 5. Design Patterns bắt buộc

## 🔹 1. Repository Pattern

* Tách DB khỏi business logic

---

## 🔹 2. Use Case Pattern

* Mỗi hành động = 1 class

---

## 🔹 3. Strategy Pattern

Dùng cho:

* Login (password, OAuth, OTP)

---

## 🔹 4. Factory Pattern

* Chọn strategy phù hợp

---

## 🔹 5. Builder Pattern

* Tạo object phức tạp (User, Token)

---

## 🔹 6. Event-driven Pattern (Kafka)

* Publish event:

  * user_created
  * user_logged_in

---

## 🔹 7. Decorator Pattern (khuyến khích)

* Cache layer (Redis)
* Logging

---

# 🔐 6. Security Requirements (RẤT QUAN TRỌNG)

AI phải đảm bảo:

## Password

* Hash bằng bcrypt hoặc argon2
* Không lưu plaintext

---

## Token

* Access token (JWT)
* Refresh token:

  * lưu DB
  * có rotation
  * có revoke

---

## Redis

Dùng cho:

* Rate limit
* OTP
* Token blacklist

---

## Anti attack

* Rate limiting
* Brute-force protection
* Validate input

---

# ⚡ 7. Redis Usage

## Key design:

```
auth:otp:{email}
auth:rate_limit:{ip}
auth:blacklist:{token}
auth:session:{userId}
```

---

# 📩 8. Kafka Usage

## Event naming:

```
auth.user.created
auth.user.logged_in
auth.user.logged_out
```

---

# 🔍 9. Code Review Rules

AI phải:

## 🔎 Phát hiện:

* Code smell
* Tight coupling
* Hardcode
* Logic sai layer
* Security issue

---

## 🚀 Đề xuất:

* Refactor
* Tối ưu performance
* Improve readability

---

# 🧪 10. Testing

* Unit test cho usecase
* Mock repository

---

# 🧠 11. Nguyên tắc quan trọng

## ❗ 1. Database per service

* Không share DB

---

## ❗ 2. Stateless service

* Không lưu state trong memory

---

## ❗ 3. Event-driven

* Ưu tiên Kafka thay vì gọi trực tiếp

---

# 🚫 12. Anti-pattern cần tránh

* Fat Controller
* God Service
* Anemic Domain
* Hard dependency vào framework

---

# 🚀 13. Mục tiêu cuối cùng

Code phải đạt:

* Production-ready
* Scalable
* Secure
* Maintainable

---

# 🧭 14. AI Behavior

AI phải:

* Luôn giải thích lý do khi generate code
* Không viết code mơ hồ
* Không bỏ qua security
* Ưu tiên best practice

---

# 🔥 15. Summary

AI cần:

* Tuân thủ Clean Architecture
* Áp dụng đúng Design Pattern
* Sử dụng Java 21 features
* Đảm bảo security + performance
* Code như Senior Engineer

---
# 🧠 16. Engineering Principles (BẮT BUỘC TUÂN THỦ)

AI phải tuân thủ các nguyên tắc thiết kế phần mềm sau:

---

# 🔥 16.1. SOLID Principles

## 🔹 S – Single Responsibility Principle (SRP)

👉 Mỗi class chỉ có **1 lý do để thay đổi**

### ✅ Đúng:

* `LoginUseCase` → chỉ xử lý login
* `UserRepository` → chỉ làm việc với DB

### ❌ Sai:

* Class vừa login + gửi email + lưu DB

---

## 🔹 O – Open/Closed Principle (OCP)

👉 Mở rộng được nhưng không sửa code cũ

### Áp dụng:

* Strategy Pattern cho login

```java
interface LoginStrategy {
    AuthResponse login(LoginRequest request);
}
```

---

## 🔹 L – Liskov Substitution Principle (LSP)

👉 Class con thay thế được class cha

### ❌ Sai:

* Override method nhưng thay đổi behavior

---

## 🔹 I – Interface Segregation Principle (ISP)

👉 Không tạo interface “to đùng”

### ❌ Sai:

```java
interface UserService {
    login();
    register();
    sendEmail();
    uploadAvatar();
}
```

### ✅ Đúng:

* Tách nhỏ interface

---

## 🔹 D – Dependency Inversion Principle (DIP)

👉 Phụ thuộc abstraction, không phụ thuộc implementation

### ✅ Đúng:

```java
private final UserRepository userRepository;
```

### ❌ Sai:

```java
private final UserRepositoryImpl userRepository;
```

---

# ⚡ 16.2. Clean Code Principles

AI phải đảm bảo:

* Tên biến rõ nghĩa
* Function ngắn (< 20-30 dòng)
* Không nested quá sâu
* Không magic number

---

# 🧱 16.3. Separation of Concerns

Mỗi layer có nhiệm vụ riêng:

| Layer          | Responsibility   |
| -------------- | ---------------- |
| delivery       | nhận request     |
| application    | xử lý business   |
| domain         | rule cốt lõi     |
| infrastructure | DB, Redis, Kafka |

---

# 🔄 16.4. DRY (Don't Repeat Yourself)

👉 Không lặp logic

### ❌ Sai:

* Validate email ở nhiều nơi

### ✅ Đúng:

* Tạo validator riêng

---

# 🧩 16.5. KISS (Keep It Simple, Stupid)

👉 Không over-engineer

### ❌ Sai:

* Dùng 5 pattern cho 1 logic đơn giản

---

# 🧠 16.6. YAGNI (You Aren’t Gonna Need It)

👉 Không code trước những thứ chưa cần

---

# ⚙️ 16.7. Fail Fast

👉 Validate sớm, fail sớm

```java
if (email == null) {
    throw new IllegalArgumentException("Email is required");
}
```

---

# 🔐 16.8. Security by Design

AI phải luôn:

* Validate input
* Sanitize dữ liệu
* Không trust client
* Không expose sensitive data

---

# 🧪 16.9. Testability

Code phải:

* Dễ mock
* Dễ test unit

---

# 📦 16.10. Idempotency (QUAN TRỌNG)

👉 Với API như:

* login
* refresh token

→ phải đảm bảo không gây lỗi khi gọi lại

---

# 🚀 16.11. Scalability Thinking

AI phải luôn nghĩ:

* Code này scale được không?
* Có bị bottleneck không?
* Có cần cache không?

---

# 🧭 16.12. Maintainability

Code phải:

* Dễ đọc
* Dễ sửa
* Dễ mở rộng

---

# 🔥 16.13. Tổng kết

AI phải luôn:

* Áp dụng SOLID
* Giữ code đơn giản (KISS)
* Tránh lặp (DRY)
* Không over-engineer (YAGNI)
* Tối ưu cho scale và security

---
