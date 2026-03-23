# 📩 Kafka Strategy – Auth Service (2Hands)

## 🎯 Mục tiêu

Kafka được sử dụng để:

* Giao tiếp giữa các microservices
* Decouple hệ thống
* Xử lý bất đồng bộ (async)
* Event-driven architecture

---

# 🧠 1. Nguyên tắc thiết kế

## ✅ Auth Service là Producer

* Không phụ thuộc service khác
* Chỉ publish event

---

## ✅ Service khác là Consumer

* notification-service
* user-service
* analytics-service

---

## ❌ Không dùng Kafka để:

* Gọi sync request (thay REST/gRPC)
* Truy vấn dữ liệu

---

# 📌 2. Event Naming Convention

```text
{domain}.{entity}.{action}
```

---

## Ví dụ:

```text
auth.user.created
auth.user.logged_in
auth.user.logged_out
auth.token.refreshed
auth.user.oauth_login
```

---

# 🧾 3. Event Structure (RẤT QUAN TRỌNG)

## ✅ Chuẩn event

```json
{
  "eventId": "uuid",
  "eventType": "auth.user.created",
  "timestamp": "2026-01-01T10:00:00Z",
  "source": "auth-service",
  "data": {
    "userId": "uuid",
    "email": "example@gmail.com"
  }
}
```

---

## 🔥 Rule

* Không gửi data nhạy cảm (password, token)
* Luôn có `eventId`
* Luôn có `timestamp`

---

# 📦 4. Topics Design

## 📌 Option 1 (recommended)

```text
auth.events
```

👉 1 topic → nhiều event type

---

## 📌 Option 2 (advanced)

```text
auth.user
auth.token
```

---

## 🎯 Khuyến nghị

👉 Dùng:

```text
auth.events
```

---

# 🔄 5. Flow trong Auth Service

## 🧾 Register

```text
User đăng ký
→ auth-service tạo user
→ publish: auth.user.created
→ notification-service gửi email
```

---

## 🔐 Login

```text
User login
→ auth-service verify
→ publish: auth.user.logged_in
→ analytics-service xử lý
```

---

## 🚪 Logout

```text
User logout
→ revoke token
→ publish: auth.user.logged_out
```

---

# ⚡ 6. Producer Design

## 🔧 Pattern

* Event-driven
* Builder pattern (event object)

---

## Ví dụ:

```java
Event event = Event.builder()
    .eventId(UUID.randomUUID())
    .eventType("auth.user.created")
    .timestamp(Instant.now())
    .data(data)
    .build();
```

---

## 🧠 Best practice

* Async send
* Retry nếu fail
* Log event

---

# 📥 7. Consumer Design

## 🔧 Rule

* Idempotent (QUAN TRỌNG)
* Không xử lý duplicate

---

## Ví dụ:

```text
Nếu event bị gửi lại
→ không tạo user 2 lần
```

---

## 🛡️ Cách làm

* lưu `eventId`
* check trước khi xử lý

---

# 🔁 8. Retry & Dead Letter Queue (DLQ)

## 🔄 Retry

* retry 3–5 lần

---

## ❌ Nếu vẫn fail

→ gửi vào DLQ

```text
auth.dlq
```

---

## 🎯 Mục đích

* debug
* không mất data

---

# ⚡ 9. Partition Strategy

## 📌 Key

```text
userId
```

---

## 🎯 Lợi ích

* giữ thứ tự event của 1 user
* scale consumer

---

# 🔐 10. Security

* Không gửi password
* Không gửi raw token
* Encrypt nếu cần

---

# 📊 11. Monitoring

AI phải:

* log event publish
* log error
* track fail rate

---

# 🧠 12. Best Practices

## ✅ Event nhỏ gọn

## ✅ Không coupling giữa service

## ✅ Versioning event

```text
auth.user.created.v1
```

---

## ✅ Backward compatible

---

# ⚠️ 13. Anti-pattern

❌ Gửi data quá lớn
❌ Gọi sync qua Kafka
❌ Không handle duplicate
❌ Không có retry

---

# 🧩 14. Design Pattern áp dụng

| Use case | Pattern      |
| -------- | ------------ |
| Event    | Builder      |
| Kafka    | Event-driven |
| Consumer | Observer     |
| Retry    | Strategy     |

---

# 🚀 15. Advanced (Senior-level)

## 🔥 Outbox Pattern (QUAN TRỌNG)

👉 Tránh mất event khi DB commit fail

---

## Flow:

```text
1. Save DB
2. Save event vào outbox table
3. Worker publish Kafka
```

---

## 🎯 Lợi ích

* đảm bảo consistency
* không mất event

---

# 🧠 16. Event Lifecycle

```text
Create → Publish → Consume → Retry → DLQ
```

---

# 🎯 17. Kết luận

Kafka trong auth-service phải:

* Event-driven
* Reliable
* Idempotent
* Decoupled

---

# 🔥 18. Final mindset

Kafka không phải chỉ để gửi message.

👉 Nó là:

> 🚀 **Xương sống giao tiếp của toàn hệ thống**

---
