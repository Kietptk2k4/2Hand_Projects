# Kafka – Email verification OTP payload

## Topic

`auth.email.verification_requested` → Notification event type `EMAIL_VERIFICATION_REQUESTED`.

## Outbox / Kafka payload (Auth)

| Field | Mô tả |
|-------|--------|
| `user_id` | UUID user |
| `email` | Email người nhận |
| `verification_code` | **OTP 6 chữ số** (plaintext trong outbox; hash lưu `VERIFICATION_TOKENS`) |
| `verification_token` | Cùng giá trị OTP — tên field legacy trên envelope |
| `verification_token_type` | `EMAIL_VERIFY` |

Notification ingest map → `recipient_email` + `verification_code`. **Không** tạo `verification_link`.

## Email template (Notification)

- Subject: `Verify your 2Hands email`
- Body: hiển thị `{{verification_code}}`, gợi ý TTL (ví dụ 15 phút).
- Không dùng `{{verification_link}}` cho flow này.

## Breaking change (client cũ)

- Email verify **không còn** link `?token=32hex` / magic link.
- API verify vẫn field `token` nhưng chỉ chấp nhận **6 chữ số**.
- `PASSWORD_RESET_REQUESTED` vẫn dùng reset link như FR Forgot Password (không đổi trong tài liệu này).

## Tham chiếu

- [kafka_section_2.md](kafka_section_2.md) — Kafka 2A/2B, MailHog, env đầy đủ
- `docs/feature_requirements/auth/FR_Register_Email.md`
- `docs/feature_requirements/notification/FR_SendEmailVerificationEmail.md`
- `docs/api_fe_behavior/notification_api_fe_behavior/SendEmailVerificationEmail-internal-and-behavior.md`

---

## Checklist manual test E2E (2A + 2B + OTP)

Checklist thống nhất cho **register → Kafka → notification worker → MailHog**. Cần bật đủ env theo `.env.example` (không chỉ `application.yml` mặc định).

### 1. Infrastructure

```bash
cd Infrastructure
docker compose up -d kafka kafka-ui mailhog postgres-auth postgres-notification redis
```

| Dịch vụ | Port / URL |
|---------|------------|
| Kafka | `localhost:9092` |
| Kafka UI | http://localhost:8080 |
| MailHog SMTP | `localhost:1025` |
| MailHog UI | http://localhost:8025 |
| postgres-auth | `localhost:5432` → DB `auth_db` |
| postgres-notification | `localhost:5435` → DB `notification_db` |
| Redis | `localhost:6379` |

### 2. Biến môi trường (copy `.env.example` → `.env`, không commit)

**Auth** — `Services/auth-service/.env.example`

| Biến | Gợi ý dev |
|------|-----------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/auth_db` |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `123456` |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` |
| `JWT_ACCESS_SECRET` / `JWT_REFRESH_SECRET` | ≥32 ký tự |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `AUTH_KAFKA_PRODUCER_ENABLED` | `true` |
| `AUTH_OUTBOX_PUBLISH_ENABLED` | `true` |
| `AUTH_OUTBOX_RETRY_ENABLED` | `true` |
| `AUTH_VERIFY_EMAIL_RATE_LIMIT_MAX_ATTEMPTS` | `10` (optional) |
| `AUTH_VERIFY_EMAIL_RATE_LIMIT_WINDOW_SECONDS` | `900` (optional) |

**Notification** — `Services/notification-service/.env.example`

| Biến | Gợi ý dev |
|------|-----------|
| `SERVER_PORT` | `3005` |
| `DB_URL` | `jdbc:postgresql://localhost:5435/notification_db` |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` |
| `NOTIFICATION_KAFKA_CONSUMER_ENABLED` | `true` |
| `NOTIFICATION_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `NOTIFICATION_KAFKA_CONSUMER_GROUP_ID` | `notification-domain-events` |
| `NOTIFICATION_PROCESS_EVENTS_ENABLED` | `true` |
| `NOTIFICATION_RETRY_EVENTS_ENABLED` | `true` |
| `NOTIFICATION_EMAIL_ENABLED` | `true` |
| `NOTIFICATION_EMAIL_PROVIDER` | `smtp` (2B) hoặc `logging` (2A) |
| `SPRING_MAIL_HOST` / `SPRING_MAIL_PORT` | `localhost` / `1025` |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | `false` |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | `false` |
| `NOTIFICATION_EMAIL_FROM` | `noreply@2hands.local` |
| `NOTIFICATION_EMAIL_PASSWORD_RESET_LINK_BASE_URL` | `http://localhost:5173/auth/reset-password?token={{token}}` |

`NOTIFICATION_EMAIL_VERIFICATION_LINK_BASE_URL` — **không dùng** cho OTP verify (có thể comment / bỏ qua).

**Frontend** — `frontend/.env` (reference)

| Biến | Gợi ý |
|------|--------|
| `VITE_AUTH_SERVICE_BASE_URL` | `http://localhost:3001` |

### 3. Chạy services

```bash
cd Services/auth-service && ./gradlew bootRun
cd Services/notification-service && ./gradlew bootRun
cd frontend && npm run dev   # optional — UI verify / resend
```

### 4. Các bước kiểm tra

| # | Bước | Cách làm | Kỳ vọng |
|---|------|----------|---------|
| E1 | **Register** | `POST http://localhost:3001/api/v1/auth/register` body `email`, `password`, `confirm_password` hoặc UI Register | `201`, `status: PENDING_VERIFICATION` |
| E2 | **MailHog — OTP verify** | Sau ≤~60s (outbox + consumer + scheduler), mở http://localhost:8025 | Mail **Verify your 2Hands email**; body `Mã xác thực của bạn: ` + **6 chữ số**; **không** có URL verify-email / `verification_link` |
| E3 | **Verify OTP** | `POST /api/v1/auth/verify-email` `{ "token": "<6-digit>" }` hoặc UI Verify Email | `200`, user `ACTIVE` |
| E4 | **Resend** | `POST /api/v1/auth/resend-email-verification` `{ "email": "..." }` hoặc nút **Gửi lại mã** (`VerifyEmailPage.jsx`) | `200`; MailHog có mail mới, OTP mới; UI countdown ~90s |
| E5 | **Forgot password** | `POST /api/v1/auth/forgot-password` `{ "email": "..." }` | Mail **Reset your 2Hands password**; body có **reset link** (`http://localhost:5173/auth/reset-password?token=...`), không phải OTP 6 số |
| E6 | **USER_CREATED — no welcome mail** | Sau register, kiểm tra MailHog + log notification | Có thể có event `USER_CREATED` trong DB `COMPLETED`; **không** thêm mail welcome; log **không** có `SMTP email sent` / subject welcome cho `USER_CREATED` |
| E7 | **Rate limit verify** *(optional)* | >10 lần `POST /verify-email` sai cùng IP trong window | `429` `VERIFY_EMAIL_RATE_LIMITED` |

**SQL gợi ý (notification DB):**

```sql
SELECT event_type, status, created_at
FROM notification_events
ORDER BY created_at DESC
LIMIT 10;
```

Sau E1: `EMAIL_VERIFICATION_REQUESTED` → `COMPLETED`; `USER_CREATED` → `COMPLETED` (không email).

### 5. Verify code (read-only — trước/sau E2E)

| Kiểm tra | File / vị trí | Kỳ vọng |
|----------|---------------|---------|
| Template OTP | `EmailNotificationTemplatePolicy` — `EMAIL_VERIFICATION_REQUESTED` | `requiredFields`: chỉ `recipient_email`, `verification_code` |
| Normalizer | `AuthSecurityEmailNotificationPayloadNormalizer.normalizeVerificationDelivery` | Nếu có `verification_code` → `remove("verification_link")`; không gọi `buildLink` cho verify |
| USER_CREATED | `UserCreatedNotificationEventHandler` | Chỉ `InitializeDefaultNotificationSettingsUseCase`; không `SendEmailNotificationUseCase` |

```bash
# Grep nhanh (từ repo root)
rg "EMAIL_VERIFICATION_REQUESTED" Services/notification-service/src/main/java/com/twohands/notification_service/domain/email/EmailNotificationTemplatePolicy.java
rg "normalizeVerificationDelivery" Services/notification-service/src/main/java/com/twohands/notification_service/application/email/AuthSecurityEmailNotificationPayloadNormalizer.java
rg "SendEmailNotification" Services/notification-service/src/main/java/com/twohands/notification_service/application/handler/UserCreatedNotificationEventHandler.java
```

### 6. Troubleshooting

| Triệu chứng | Kiểm tra |
|-------------|----------|
| Không có mail MailHog | `NOTIFICATION_EMAIL_PROVIDER=smtp`, MailHog `up`, `NOTIFICATION_PROCESS_EVENTS_ENABLED=true` |
| `notification_events` mãi `PENDING` | `NOTIFICATION_PROCESS_EVENTS_ENABLED=true`; log scheduler — lỗi `notification_event_status = character varying` → cần CAST enum trong `NotificationEventRepositoryAdapter` (đã sửa) |
| `.env` local tắt consumer | So sánh với `.env.example`: `NOTIFICATION_KAFKA_CONSUMER_ENABLED` / `NOTIFICATION_PROCESS_EVENTS_ENABLED` phải `true` |
| OTP bị redact | `JacksonNotificationEventPayloadSanitizer` allowlist `verification_code` |

---

## Kết quả verify 2C (2026-06-04)

| # | Bước | Kết quả | Ghi chú |
|---|------|---------|---------|
| Infra | Docker: kafka, postgres-auth, postgres-notification, redis, mailhog | **PASS** | MailHog khởi động trong phiên test (`docker compose up -d mailhog`) |
| Env | Auth `.env` publish Kafka; Notification theo `.env.example` (2A+2B) | **PASS** / **WARN** | Notification chạy test với env override (`.env` local có `NOTIFICATION_KAFKA_CONSUMER_ENABLED=false` — cần sync với example) |
| E1 | Register `201` | **PASS** | |
| E2 | MailHog OTP 6 số, không verification link | **PASS** | Subject `Verify your 2Hands email`; body `Mã xác thực của bạn: 456769` |
| E3 | Verify OTP API `200` ACTIVE | **PASS** | |
| E4 | Resend API `200` + mail mới | **PASS** | HTTP 200; scheduler gửi lại (đếm nhiều verify mail khi drain backlog PENDING) |
| E5 | Forgot → reset link localhost:5173 | **PASS** | `RESET_LINK=YES`, không OTP trong mail reset |
| E6 | USER_CREATED không welcome mail | **PASS** | DB `USER_CREATED` → `COMPLETED`; MailHog không subject Welcome |
| Code | Template / normalizer / USER_CREATED grep | **PASS** | Xem §5 |
| Bugfix | Scheduler claim enum PostgreSQL | **FIX** | `NotificationEventRepositoryAdapter`: `CAST(:status AS notification_event_status)` |
