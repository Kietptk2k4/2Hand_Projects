# ⚡ Redis Strategy – Auth Service (2Hands)

## 🎯 Mục tiêu

Redis được sử dụng để:

* Tăng performance
* Giảm load DB
* Bảo mật (rate limit, OTP, blacklist)
* Quản lý session tạm thời

---

# 🧠 1. Nguyên tắc thiết kế

## ✅ Redis dùng cho:

* Dữ liệu tạm thời (TTL)
* Cache
* Security control

---

## ❌ KHÔNG dùng Redis cho:

* Dữ liệu quan trọng (source of truth)
* Lưu user chính

---

## 🔑 Key design rule

```text
{service}:{feature}:{identifier}
```

Ví dụ:

```text
auth:otp:email@example.com
auth:rate_limit:192.168.1.1
auth:session:userId
```

---

# 🔐 2. OTP (Email Verification)

## 📌 Key

```text
auth:otp:{email}
```

## 📦 Value

```json
{
  "code": "123456",
  "attempts": 0
}
```

## ⏳ TTL

```text
5 phút
```

---

## 🔄 Flow

```text
SET auth:otp:{email} {code} EX 300

GET → verify

DEL → nếu thành công
```

---

## 🛡️ Security

* Giới hạn số lần nhập sai (max 5)
* Sau 5 lần → block tạm

---

# 🚫 3. Rate Limiting (Anti brute-force)

## 📌 Key

```text
auth:rate_limit:{ip}
```

---

## 🔄 Strategy

### Sliding window hoặc fixed window

```text
INCR auth:rate_limit:{ip}
EXPIRE 60s
```

---

## 📊 Rule

* 5 request / phút → login
* vượt quá → block

---

## 🛡️ Advanced

```text
auth:rate_limit:email:{email}
```

👉 chống attack theo account

---

# 🔁 4. Session Cache

## 📌 Key

```text
auth:session:{userId}
```

## 📦 Value

```json
{
  "device": "chrome",
  "ip": "1.2.3.4"
}
```

---

## ⏳ TTL

* bằng access token (15–30 phút)

---

## 🎯 Mục đích

* quick lookup
* detect multi-login
* revoke session nhanh

---

# ⛔ 5. Token Blacklist (Logout)

## 📌 Key

```text
auth:blacklist:{accessToken}
```

## ⏳ TTL

* bằng thời gian còn lại của token

---

## 🔄 Flow

```text
SET blacklist token EX ttl
```

---

## 🎯 Mục đích

* Ngăn token bị reuse sau logout

---

# 🔐 6. Refresh Token Protection

## 📌 Key

```text
auth:refresh:{tokenId}
```

## 📦 Value

```text
valid / revoked
```

---

## 🎯 Mục đích

* detect reuse
* chống hijack

---

# 🚨 7. Brute-force Protection (QUAN TRỌNG)

## 📌 Key

```text
auth:login_fail:{email}
```

---

## 🔄 Flow

```text
INCR fail count
EXPIRE 15 phút
```

---

## 📊 Rule

* 5 lần sai → lock account tạm

---

## 📌 Key lock

```text
auth:lock:{email}
```

TTL: 15 phút

---

# ⚡ 8. Redis Data Lifecycle

| Data       | TTL        |
| ---------- | ---------- |
| OTP        | 5 phút     |
| Rate limit | 1 phút     |
| Session    | 15–30 phút |
| Blacklist  | theo token |
| Login fail | 15 phút    |

---

# 🧠 9. Best Practices

## ✅ Luôn set TTL

```text
SET key value EX seconds
```

---

## ✅ Prefix rõ ràng

```text
auth:...
```

---

## ✅ Không lưu data lớn

---

## ✅ Idempotent

* cùng request → không gây lỗi

---

# ⚠️ 10. Anti-pattern

❌ Không set TTL
❌ Dùng Redis thay DB
❌ Key không có prefix
❌ Không validate trước khi cache

---

# 🔐 11. Security Checklist

* Rate limit login
* OTP expire
* Blacklist token
* Detect suspicious activity
* Limit retry

---

# 🚀 12. Advanced (khuyến khích)

## 🔥 Sliding Window (chuẩn hơn)

```text
ZADD + timestamp
ZREMRANGEBYSCORE
```

---

## 🔥 Distributed lock (nếu cần)

```text
SETNX
```

---

## 🔥 Cache aside pattern

```text
check cache → miss → DB → set cache
```

---

# 🧩 13. Design Pattern áp dụng

| Use case   | Pattern                  |
| ---------- | ------------------------ |
| Cache      | Decorator                |
| Rate limit | Strategy                 |
| Session    | Singleton (Redis client) |

---

# 🎯 14. Kết luận

Redis trong auth-service phải:

* Nhanh
* Có TTL
* Phục vụ security
* Không thay thế DB

---

# 🔥 15. Final mindset

Redis không chỉ là cache.

👉 Nó là:

> 🔐 **Lớp bảo vệ đầu tiên của hệ thống**

---
