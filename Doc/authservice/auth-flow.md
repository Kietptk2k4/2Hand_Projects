# 🔐 Auth Flow – 2Hands Auth Service

## 🎯 Mục tiêu

Tài liệu mô tả toàn bộ flow:

* Register
* Login
* Refresh Token
* Logout
* OAuth (mở rộng)

Đảm bảo:

* Secure
* Scalable
* Event-driven
* Stateless

---

# 🧱 1. Tổng quan hệ thống

```text
Client → API Gateway → Auth Service
                         ↓
            PostgreSQL + Redis + Kafka
```

---

# 🧾 2. REGISTER FLOW

## 📌 Mục tiêu:

* Tạo user mới
* Xác thực email (OTP)
* Publish event

---

## 🔄 Flow chi tiết

```text
1. Client gửi request register

2. Validate input
   - email format
   - password strength

3. Check tồn tại user (DB)

4. Hash password (bcrypt/argon2)

5. Lưu user (status = PENDING_VERIFICATION)

6. Tạo OTP → lưu Redis
   key: auth:otp:{email}
   TTL: 5 phút

7. Publish Kafka event:
   auth.user.created

8. Notification service gửi email OTP
```

---

## 📦 Database

* users
* password_history

---

## ⚠️ Lưu ý

* Không activate user nếu chưa verify
* Không gửi password qua Kafka

---

# 🔑 3. VERIFY EMAIL FLOW

```text
1. Client gửi OTP

2. Lấy OTP từ Redis

3. So sánh OTP

4. Nếu đúng:
   - update user → ACTIVE
   - delete OTP

5. Nếu sai:
   - tăng counter (chống brute force)
```

---

# 🔐 4. LOGIN FLOW (CORE)

## 📌 Mục tiêu:

* Xác thực user
* Sinh token
* Log activity
* Cache session

---

## 🔄 Flow chi tiết

```text
1. Client gửi login request

2. Rate limit (Redis)
   key: auth:rate_limit:{ip}

3. Tìm user (DB)

4. Kiểm tra:
   - status (ACTIVE?)
   - email_verified

5. Verify password

6. Generate:
   - Access Token (JWT)
   - Refresh Token (random string)

7. Hash refresh token → lưu DB

8. Lưu session Redis:
   key: auth:session:{userId}

9. Ghi log login_logs

10. Publish Kafka:
    auth.user.logged_in

11. Trả response
```

---

## 📦 Database

* users
* refresh_token_sessions
* login_logs

---

## ⚠️ Security

* Không lưu refresh token dạng raw
* Hash trước khi lưu DB
* Rate limit login

---

# 🔁 5. REFRESH TOKEN FLOW

## 📌 Mục tiêu:

* Cấp lại access token
* Rotation refresh token

---

## 🔄 Flow chi tiết

```text
1. Client gửi refresh token

2. Hash token → tìm DB

3. Kiểm tra:
   - tồn tại
   - chưa revoked
   - chưa expired

4. Generate:
   - access token mới
   - refresh token mới

5. Update DB:
   - revoke token cũ
   - lưu token mới

6. Publish Kafka:
   auth.token.refreshed

7. Trả token mới
```

---

## ⚠️ Security

* Bắt buộc rotation
* Nếu token bị reuse → mark COMPROMISED

---

# 🚪 6. LOGOUT FLOW

## 📌 Mục tiêu:

* Huỷ session
* Ngăn reuse token

---

## 🔄 Flow

```text
1. Client gửi logout

2. Revoke refresh token (DB)

3. Blacklist access token (Redis)
   key: auth:blacklist:{token}

4. Xoá session Redis

5. Publish Kafka:
   auth.user.logged_out
```

---

# 🔗 7. OAUTH FLOW (Google, Facebook)

## 🔄 Flow

```text
1. Client login qua provider

2. Auth service nhận token từ provider

3. Verify với provider

4. Nếu user chưa tồn tại:
   - tạo user mới

5. Nếu đã tồn tại:
   - link account

6. Generate JWT + refresh token

7. Publish Kafka:
   auth.user.oauth_login
```

---

# ⚡ 8. REDIS STRATEGY

| Purpose    | Key                    |
| ---------- | ---------------------- |
| OTP        | auth:otp:{email}       |
| Rate limit | auth:rate_limit:{ip}   |
| Session    | auth:session:{userId}  |
| Blacklist  | auth:blacklist:{token} |

---

# 📩 9. KAFKA EVENTS

```text
auth.user.created
auth.user.logged_in
auth.user.logged_out
auth.token.refreshed
auth.user.oauth_login
```

---

# 🔐 10. SECURITY CHECKLIST

* Password hash (bcrypt/argon2)
* Refresh token rotation
* Rate limiting
* Input validation
* Không expose sensitive data
* Log login activity
* Detect suspicious activity

---

# 🧠 11. DESIGN PATTERN ÁP DỤNG

| Feature  | Pattern            |
| -------- | ------------------ |
| Login    | Strategy + Factory |
| DB       | Repository         |
| Business | UseCase            |
| Token    | Builder            |
| Kafka    | Event-driven       |
| Redis    | Decorator          |

---

# 🚀 12. FINAL GOAL

Auth service phải:

* Stateless
* Secure
* Scalable
* Event-driven
* Production-ready

---
