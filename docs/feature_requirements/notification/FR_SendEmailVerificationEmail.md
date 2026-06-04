# Functional Requirement - Send Email Verification Email

## 1. Feature Overview

Gửi email xác minh khi Auth Service publish `EMAIL_VERIFICATION_REQUESTED`. Nội dung email là **mã OTP 6 chữ số** do Auth tạo — **không** dùng verification link / magic link.

## 2. Actors

- **Auth Service:** Publish event, sinh OTP, hash lưu `VERIFICATION_TOKENS`.
- **Notification Service:** Render template và gửi email.
- **User:** Nhận OTP qua email, nhập trên màn Verify Email (Auth API).

## 3. Scope

**In Scope:**

- Consume event `EMAIL_VERIFICATION_REQUESTED`.
- Render template với biến `verification_code` (6 digit).
- Deliver email.

**Out of Scope:**

- Sinh hoặc validate OTP (thuộc Auth).
- Cập nhật trạng thái user verify.
- Build verification link từ base URL.

## 4. Trigger

- Event type `EMAIL_VERIFICATION_REQUESTED` từ Auth (Kafka / outbox).

## 5. Business Rules

- Payload bắt buộc có email người nhận và **`verification_code`** (6 chữ số) từ Auth outbox.
- Auth có thể gửi thêm `verification_token` (cùng giá trị OTP) — Notification map về `verification_code`; **không** tạo `verification_link`.
- Notification Service **không** tự sinh OTP.
- Email security-critical: gửi bất kể user tắt marketing email.
- Không log OTP plaintext sau ingest.

## 6. Database Impact

- Đọc/cập nhật `notification_events`.
- Payload persisted sau ingest **không** chứa raw OTP/token.

## 7. Security

- Không lưu OTP thuần văn trong DB notification hoặc logs.
- Template không expose trạng thái nội bộ user.

## 8. Failure Cases

- Thiếu email → failed.
- Thiếu `verification_code` → failed (permanent).
- Provider lỗi tạm thời → retry.
- Email invalid → permanent failure.

## 9. Acceptance Criteria

- Email gửi thành công khi event hợp lệ, body hiển thị OTP 6 chữ số.
- Auth là owner của OTP; Notification chỉ deliver.
- OTP không xuất hiện trong logs/`last_error`.
