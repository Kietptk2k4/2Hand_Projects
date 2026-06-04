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

- `docs/feature_requirements/auth/FR_Register_Email.md`
- `docs/feature_requirements/notification/FR_SendEmailVerificationEmail.md`
- `docs/api_fe_behavior/notification_api_fe_behavior/SendEmailVerificationEmail-internal-and-behavior.md`

---

## Checklist manual test (E2E OTP verify)

**Chuẩn bị:** Auth + Notification chạy local; `AUTH_OUTBOX_PUBLISH_ENABLED=true`, `NOTIFICATION_EMAIL_ENABLED=true` (hoặc mock SMTP/logging provider); FE trỏ `VITE_AUTH_SERVICE_BASE_URL`.

1. **Đăng ký** — `POST /api/v1/auth/register` (hoặc UI Register). Kỳ vọng: `PENDING_VERIFICATION`; outbox/Kafka payload có `verification_code` 6 chữ số (không có `verification_link`).
2. **Nhận email** — Xử lý event `EMAIL_VERIFICATION_REQUESTED` (worker ingest hoặc poll outbox). Kỳ vọng: subject/body chỉ hiển thị mã OTP (vd `123456`), không có URL verify.
3. **Verify OTP** — `POST /api/v1/auth/verify-email` body `{ "token": "<6-digit>" }` (hoặc UI Verify). Kỳ vọng: `200`, user `ACTIVE`; OTP sai/5 ký tự → `400`.
4. **Resend** — `POST /api/v1/auth/resend-email-verification` `{ "email": "..." }` (hoặc nút **Gửi lại mã** trên Verify Email). Kỳ vọng: `200`, email mới với OTP mới; nút countdown ~90s trước khi gửi lại được.
5. **Rate limit verify** — Gửi >10 request verify sai liên tiếp cùng IP (trong window cấu hình). Kỳ vọng: `429` `VERIFY_EMAIL_RATE_LIMITED` trước khi brute-force OTP.
