# Kafka — Hạng mục 2A: Auth → Notification (consume + process)

Tài liệu bật luồng **Auth publish → Notification consume → worker xử lý event → email (logging provider)** trên local. Phụ thuộc:

- [Hạng mục 0 — broker](kafka_section_0.md)
- [Hạng mục 1 — outbox publisher](kafka_section_1.md)
- [Email verify OTP](kafka_section_email_otp.md)

**2B (SMTP / MailHog):** xem [mục 2B](#hạng-mục-2b-smtp--mailhog) bên dưới. Consumer social/commerce/admin vẫn subscribe sẵn trong code; verify 2A/2B tập trung **3 topic auth**.

---

## Mục tiêu 2A

| Bước | Thành phần | Kết quả |
|------|------------|---------|
| 1 | Auth outbox scheduler | `outbox_events` → Kafka |
| 2 | `DomainEventKafkaListener` | INSERT `notification_events` (`PENDING`) |
| 3 | `ProcessNotificationEventsScheduler` | Handlers → `COMPLETED` / `FAILED` |
| 4 | Email channel | 2A: `LoggingEmailNotificationProvider` · 2B: `SmtpEmailNotificationProvider` → MailHog |

### Topic Auth (2A)

| Kafka topic | `event_type` | Handler chính |
|-------------|--------------|-----------------|
| `auth.email.verification_requested` | `EMAIL_VERIFICATION_REQUESTED` | `EmailVerificationNotificationEventHandler` → OTP email (`verification_code`) |
| `auth.password.reset_requested` | `PASSWORD_RESET_REQUESTED` | `PasswordResetNotificationEventHandler` → reset **link** email |
| `auth.user.created` | `USER_CREATED` | `UserCreatedNotificationEventHandler` → **chỉ** init `user_notification_settings` |

**`USER_CREATED`:** Không gửi welcome email. Handler `@Order(0)` gọi `InitializeDefaultNotificationSettingsUseCase` — không qua `SendEmailNotificationUseCase`.

**Email verify OTP:** Template bắt buộc `recipient_email` + `verification_code`. `NOTIFICATION_EMAIL_VERIFICATION_LINK_BASE_URL` **không** dùng cho flow này (xem [kafka_section_email_otp.md](kafka_section_email_otp.md)).

---

## Luồng end-to-end

```text
POST /api/v1/auth/register | resend-email-verification | forgot-password
  → Auth use case (transaction)
  → outbox_events PENDING
  → commit

Auth scheduler (AUTH_OUTBOX_PUBLISH_ENABLED=true)
  → KafkaOutboxEventPublisher
  → topic auth.email.verification_requested | auth.password.reset_requested | auth.user.created

Notification DomainEventKafkaListener (NOTIFICATION_KAFKA_CONSUMER_ENABLED=true)
  → ConsumeDomainEventUseCase
  → notification_events PENDING (idempotent theo source_event_id)

ProcessNotificationEventsScheduler (NOTIFICATION_PROCESS_EVENTS_ENABLED=true, cron ~30s)
  → ProcessNotificationEventUseCase
  → Handler theo event_type
  → EMAIL_VERIFICATION_REQUESTED / PASSWORD_RESET_REQUESTED:
       SendEmailNotificationUseCase → logging (2A) hoặc SMTP → MailHog (2B)
  → USER_CREATED: init settings only → COMPLETED (no email log)
```

Dev vẫn có thể ingest qua `POST /internal/events` khi consumer tắt — 2A ưu tiên **Kafka path**.

---

## Biến môi trường

### Notification (`Services/notification-service/.env` — copy từ `.env.example`)

| Biến | 2A (gợi ý) | Vai trò |
|------|------------|---------|
| `NOTIFICATION_KAFKA_CONSUMER_ENABLED` | `true` | Bật `@KafkaListener` |
| `NOTIFICATION_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Broker (host) |
| `NOTIFICATION_KAFKA_CONSUMER_GROUP_ID` | `notification-domain-events-local` (dev: **mỗi máy một group**) | Consumer group |
| `NOTIFICATION_PROCESS_EVENTS_ENABLED` | `true` | Scheduler xử lý `PENDING` |
| `NOTIFICATION_EMAIL_ENABLED` | `true` | Cho phép gửi email (logging provider) |
| `NOTIFICATION_RETRY_EVENTS_ENABLED` | `true` | Retry event `FAILED` (khuyến nghị dev) |
| `NOTIFICATION_EMAIL_PASSWORD_RESET_LINK_BASE_URL` | `http://localhost:5173/auth/reset-password?token={{token}}` | Build `reset_link` cho forgot-password (FE local; prod dùng URL thật) |
| `NOTIFICATION_EMAIL_VERIFICATION_LINK_BASE_URL` | *(optional / comment)* | **Unused** cho OTP verify |

Có thể giữ `NOTIFICATION_INTERNAL_INGEST_ENABLED=true` để debug; không bắt buộc khi Kafka consumer bật.

### Auth (reference — đã có trong `auth-service/.env.example`)

| Biến | 2A (gợi ý) |
|------|------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `AUTH_KAFKA_PRODUCER_ENABLED` | `true` |
| `AUTH_OUTBOX_PUBLISH_ENABLED` | `true` |

---

## Class / file tham chiếu (đã implement — không sửa logic trong 2A doc)

| Thành phần | File |
|------------|------|
| Kafka listener | `DomainEventKafkaListener.java` |
| Topic → event type | `DomainEventTopicResolver.java` |
| Ingest | `ConsumeDomainEventUseCase.java` |
| Process worker | `ProcessNotificationEventsScheduler`, `ProcessNotificationEventUseCase` |
| OTP email | `EmailVerificationNotificationEventHandler`, `EmailNotificationTemplatePolicy` |
| Reset email | `PasswordResetNotificationEventHandler` |
| USER_CREATED | `UserCreatedNotificationEventHandler` |
| Email dev provider | `LoggingEmailNotificationProvider.java` |

---

## Verify 2A (manual checklist)

**Chuẩn bị**

```bash
cd Infrastructure
docker compose up -d kafka kafka-ui postgres-auth postgres-notification redis
```

1. Copy `.env` từ `Services/auth-service/.env.example` và `Services/notification-service/.env.example` (không commit `.env`).
2. `cd Services/auth-service && ./gradlew bootRun`
3. `cd Services/notification-service && ./gradlew bootRun`

| # | Bước | Kỳ vọng |
|---|------|---------|
| 1 | `POST /api/v1/auth/register` (body email/password hợp lệ) | Auth `201`; outbox → `PUBLISHED` |
| 2 | Kafka UI http://localhost:8080 → topic `auth.email.verification_requested` | Message envelope có `payload.verification_code` (6 chữ số) |
| 3 | SQL notification DB: `SELECT event_type, status FROM notification_events ORDER BY created_at DESC LIMIT 5;` | Hàng `EMAIL_VERIFICATION_REQUESTED` → `COMPLETED` (sau ≤1 chu kỳ scheduler) |
| 4 | Log notification-service | `Email provider accepted` + subject verify (OTP có thể không in full body — OK) |
| 5 | *(Optional)* `POST /api/v1/auth/forgot-password` | Topic `auth.password.reset_requested`; event `COMPLETED`; log subject reset |

**USER_CREATED:** Sau register, có thể thấy thêm event `USER_CREATED` → `COMPLETED` **không** có log email provider.

**Troubleshooting ngắn**

| Triệu chứng | Kiểm tra |
|-------------|----------|
| Không có topic/message | Auth: `AUTH_OUTBOX_PUBLISH_ENABLED`, Kafka up |
| `notification_events` không có row | `NOTIFICATION_KAFKA_CONSUMER_ENABLED`, bootstrap `localhost:9092`, consumer group **riêng** (tránh instance khác ACK), log `Invalid domain event` / `Domain event ingested from Kafka` |
| Resend OTP không tạo row mới (cùng user) | `event_key` trùng — auth dùng `outboxEventId` làm key cho `EMAIL_VERIFICATION_REQUESTED` |
| `PENDING` mãi | `NOTIFICATION_PROCESS_EVENTS_ENABLED=true` |
| `FAILED` + thiếu `verification_code` | Payload OTP — [kafka_section_email_otp.md](kafka_section_email_otp.md) |
| `SKIPPED` email | `NOTIFICATION_EMAIL_ENABLED=true` |
| `PENDING` mãi + log `notification_event_status = character varying` | JDBC claim cần `CAST(:status AS notification_event_status)` — xem `NotificationEventRepositoryAdapter` |
| `.env` tắt consumer/process | So `.env` với `.env.example`: `NOTIFICATION_KAFKA_CONSUMER_ENABLED` + `NOTIFICATION_PROCESS_EVENTS_ENABLED` = `true` |

---

## Hạng mục 2B: SMTP + MailHog

### Mục tiêu

- Dev nhận email **thật** trong [MailHog](http://localhost:8025) (SMTP `localhost:1025`).
- `NOTIFICATION_EMAIL_PROVIDER=smtp` → `SmtpEmailNotificationProvider` (`spring-boot-starter-mail`).
- `NOTIFICATION_EMAIL_PROVIDER=logging` (mặc định) → `LoggingEmailNotificationProvider` (2A).
- Verify email: body OTP 6 chữ số (`verification_code`). Reset password: `reset_link` trong body.

### Docker

```bash
cd Infrastructure
docker compose up -d mailhog
# hoặc cùng stack: docker compose up -d kafka kafka-ui postgres-auth postgres-notification redis mailhog
```

| Port | Dịch vụ |
|------|---------|
| 1025 | SMTP (MailHog) |
| 8025 | Web UI — http://localhost:8025 |

### Env notification (2B)

| Biến | Gợi ý dev |
|------|-----------|
| `NOTIFICATION_EMAIL_PROVIDER` | `smtp` |
| `NOTIFICATION_EMAIL_ENABLED` | `true` |
| `SPRING_MAIL_HOST` | `localhost` |
| `SPRING_MAIL_PORT` | `1025` |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | `false` |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | `false` |
| `NOTIFICATION_EMAIL_FROM` | `noreply@2hands.local` |
| `NOTIFICATION_EMAIL_FROM_NAME` | `2Hands` |
| `NOTIFICATION_EMAIL_PASSWORD_RESET_LINK_BASE_URL` | `http://localhost:5173/auth/reset-password?token={{token}}` |

`NOTIFICATION_EMAIL_VERIFICATION_LINK_BASE_URL` — **không dùng** cho OTP verify.

Giữ env **2A** (Kafka consumer + `NOTIFICATION_PROCESS_EVENTS_ENABLED=true`) và env **auth publish** từ [kafka_section_1.md](kafka_section_1.md).

### Provider switch

| `notification.integrations.email.provider` | Class active |
|------------------------------------------|--------------|
| `logging` (default) | `LoggingEmailNotificationProvider` |
| `smtp` | `SmtpEmailNotificationProvider` |

Chỉ một bean `EmailNotificationProvider` active (`@ConditionalOnProperty`). Log thành công: `SMTP email sent` (không log OTP/reset token trong body).

### Verify 2B (manual)

1. `docker compose up -d mailhog` (+ stack 2A nếu chưa chạy).
2. Copy `notification-service/.env.example` → `.env` (có `NOTIFICATION_EMAIL_PROVIDER=smtp`).
3. Restart `notification-service`.
4. `POST /api/v1/auth/register` (email mới).
5. Mở http://localhost:8025 → inbox có mail **Verify your 2Hands email**.
6. Mở message → body hiển thị dòng OTP kiểu **「Mã xác thực của bạn: 123456」** (6 chữ số; giá trị thực từ auth).
7. *(Optional)* `POST /api/v1/auth/forgot-password` → mail reset có **link** (không phải OTP).

**Screenshot mô tả (MailHog UI):**

- Cột trái: 1 message tới `user@…`, subject `Verify your 2Hands email`.
- Panel phải: plain text body với `Mã xác thực của bạn:` + **6 digit code**, không có URL verify-email.

Log service: `SMTP email sent messageId=… to=u***@example.com subject=Verify your 2Hands email`.

**Checklist E2E đầy đủ (OTP + resend + forgot + USER_CREATED):** [kafka_section_email_otp.md § Checklist manual test E2E](kafka_section_email_otp.md#checklist-manual-test-e2e-2a--2b--otp).

---

## Việc chưa làm (sau 2B)

| Hạng mục | Nội dung |
|----------|----------|
| Prod email | SendGrid / SES |
| 2+ | Social/commerce consumer E2E, DLQ, monitoring |

---

## Liên kết

- [kafka_section_0.md](kafka_section_0.md) — Docker broker
- [kafka_section_1.md](kafka_section_1.md) — Outbox publisher
- [kafka_section_email_otp.md](kafka_section_email_otp.md) — OTP payload & template
- [ConsumeDomainEvent-internal-and-behavior.md](../api_fe_behavior/notification_api_fe_behavior/ConsumeDomainEvent-internal-and-behavior.md)
