# Functional Requirement (FR) - Forgot Password

## 1. Feature Overview
Chuc nang ho tro nguoi dung yeu cau dat lai mat khau khi quen mat khau. He thong tao token/OTP reset tam thoi, luu vao CSDL va ghi outbox event de Notification Service gui email reset.

Muc tieu:
- Khoi phuc quyen truy cap an toan.
- Tranh ro ri thong tin ton tai tai khoan (email enumeration).

## 2. Actors
- **Guest/User:** Nguoi dung quen mat khau.
- **System:** Auth Service + Outbox Worker + Notification Service.

## 3. Scope
- **In Scope:**
  - Nhan email yeu cau quen mat khau.
  - Tao reset token/OTP va luu `VERIFICATION_TOKENS` voi `type = PASSWORD_RESET`.
  - Ghi outbox event `PASSWORD_RESET_REQUESTED`.
  - Tra ve response thanh cong theo huong anti-enumeration.
- **Out of Scope:**
  - API reset password (nhap token + mat khau moi) la FR rieng.
  - Login/logout/refresh.

## 4. Preconditions
- Client gui email hop le.
- He thong co san outbox worker de publish event.

## 5. Business Rules
- He thong **khong** tiet lo email co ton tai hay khong:
  - Email ton tai -> tao token + event.
  - Email khong ton tai -> van tra 200 voi thong diep chung.
- Token reset co thoi han ngan (vi du 15 phut, theo config).
- Token reset chi dung 1 lan (xu ly o FR ResetPassword).
- Ghi DB theo outbox pattern de dam bao eventually consistent.

## 6. API Contract
**Endpoint:** `POST /api/v1/auth/forgot-password`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response - 200 OK (luon dung cho email ton tai/khong ton tai):**
```json
{
  "code": 200,
  "success": true,
  "message": "Neu email hop le, chung toi da gui huong dan dat lai mat khau.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

**Response - 400 Bad Request (payload sai):**
```json
{
  "code": 400,
  "success": false,
  "message": "Du lieu khong hop le.",
  "data": null,
  "errors": [
    {
      "field": "email",
      "reason": "INVALID_FORMAT"
    }
  ],
  "timestamp": "2026-05-15T10:00:00Z"
}
```

## 7. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `email` | string | Yes | Dung dinh dang email, max 255 ky tu | "Vui long nhap dung dinh dang email." |

## 8. Workflow
1. Client gui request forgot-password voi email.
2. Auth Service validate format email (sai -> `400`).
3. Normalize email va tra cuu `USERS`.
4. Neu user ton tai:
   - Tao token reset (khuyen nghi luu hash vao DB).
   - Insert `VERIFICATION_TOKENS`:
     - `type = PASSWORD_RESET`
     - `expires_at`
     - `used_at = null`
   - Insert `OUTBOX_EVENTS` voi `event_type = PASSWORD_RESET_REQUESTED`, `status = PENDING`.
5. Neu user khong ton tai:
   - Khong tao token/event.
6. Tra `200` thong diep chung.

## 9. Database Impact
- **VERIFICATION_TOKENS** (neu user ton tai):
  - insert ban ghi token reset.
- **OUTBOX_EVENTS** (neu user ton tai):
  - insert ban ghi `PASSWORD_RESET_REQUESTED`.

## 10. Transaction & Consistency
- Cac buoc ghi `VERIFICATION_TOKENS` va `OUTBOX_EVENTS` phai cung 1 transaction.
- Neu 1 buoc fail -> rollback.
- Outbox worker publish bat dong bo den Notification Service.

## 11. Event Flow
1. Auth Service ghi `OUTBOX_EVENTS` (`PASSWORD_RESET_REQUESTED`, `PENDING`).
2. Outbox worker poll va publish len broker.
3. Notification Service consume event va gui email reset.

## 12. Security
- Khong log raw token/otp.
- Khong tra thong bao phan biet email ton tai hay khong.
- Rate limit endpoint forgot-password theo IP/email de chong abuse.
- Bat buoc HTTPS/TLS.

## 13. Error Handling
- `400`: email sai format/thiếu.
- `200`: xu ly thanh cong theo anti-enumeration policy.
- `500`: loi he thong.

## 14. FE Behavior
- Form chi gom 1 truong `email`.
- Inline validate email format.
- Khi submit:
  - disable button + hien loading.
  - neu 200: hien thong bao chung "Kiem tra email neu tai khoan ton tai."
- Khong hien thong diep "email khong ton tai".
- Co link quay ve trang login.

## 15. Acceptance Criteria
- **AC1:** Email hop le va ton tai -> tao `VERIFICATION_TOKENS` type `PASSWORD_RESET` + tao outbox event `PASSWORD_RESET_REQUESTED`.
- **AC2:** Email hop le nhung khong ton tai -> van tra `200`, khong lo thong tin ton tai tai khoan.
- **AC3:** Email sai format -> tra `400`.
- **AC4:** Khong log plaintext token/password.
- **AC5:** Outbox event duoc tao nhat quan cung transaction token.

## 16. Mapping to Existing Project Docs
- `docs/use-cases/uc-password-recovery.md` (muc 3.1 Forgot Password)
- `docs/business-flow/password-recovery-flow.md`
- `docs/business-spec/auth-service-spec.md` (Authentication - Quen mat khau)
- `docs/database/auth_schema.md` (`VERIFICATION_TOKENS`, `OUTBOX_EVENTS`, `USERS`)
- `docs/engineering-rules/api-standard.md` (response format)
