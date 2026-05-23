# Functional Requirement (FR) - Resend Email Verification

## 1. Feature Overview

Cho phep Guest/User yeu cau gui lai ma xac thuc email khi dang ky nhung chua verify hoac token/OTP cu da het han. He thong tao token moi, vo hieu hoa token cu chua dung (neu co) va ghi outbox event de Notification Service gui email.

Muc tieu:

- Ho tro luong kich hoat tai khoan sau `FR_Register_Email`.
- Giam ty le user bi ket o trang thai `PENDING_VERIFICATION`.
- Chong spam resend bang rate limit.

## 2. Actors

- **Guest/User:** Nguoi dang cho xac thuc email.
- **System:** Auth Service + Outbox Worker + Notification Service.

## 3. Scope

- **In Scope:**
  - Nhan email (hoac user da login nhung chua verify — policy MVP: chi email).
  - Tao token/OTP moi trong `VERIFICATION_TOKENS` voi `type = EMAIL_VERIFY`.
  - Invalidate token `EMAIL_VERIFY` cu chua `used_at` cua cung user (neu policy cho phep).
  - Ghi outbox `EMAIL_VERIFICATION_REQUESTED`.
  - Tra response anti-enumeration (khong lo email ton tai hay khong).
- **Out of Scope:**
  - Verify token (`FR_Verify_Email`).
  - Doi email account.
  - Resend SMS/phone verification.

## 4. Preconditions

- He thong co outbox worker de publish event.
- Notification Service consume `EMAIL_VERIFICATION_REQUESTED`.

## 5. Business Rules

- Chi user o trang thai `PENDING_VERIFICATION` moi duoc tao token moi (neu email ton tai).
- User `ACTIVE` / `SUSPENDED` / `DELETED`: khong tao token; van tra `200` thong diep chung (anti-enumeration).
- Token moi co TTL ngan (vi du 15 phut, theo config).
- Token chi dung 1 lan khi verify (`FR_Verify_Email`).
- Ghi `VERIFICATION_TOKENS` + `OUTBOX_EVENTS` trong cung transaction.
- Rate limit theo IP + email (vi du toi da 3 lan / 15 phut / email).
- Khong log raw OTP/token.

## 6. API Contract

**Endpoint:** `POST /api/v1/auth/resend-email-verification`

**Auth:** Public (khong bat buoc JWT). MVP co the yeu cau JWT neu user da login nhung chua verify — tuy product.

**Request body:**

```json
{
  "email": "user@example.com"
}
```

**Response - 200 OK (luon dung khi format hop le):**

```json
{
  "code": 200,
  "success": true,
  "message": "Neu email hop le va chua xac thuc, chung toi da gui lai ma xac thuc.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-21T10:00:00Z"
}
```

**Response - 400 Bad Request:**

```json
{
  "code": 400,
  "success": false,
  "message": "Du lieu khong hop le.",
  "data": null,
  "errors": [
    { "field": "email", "reason": "INVALID_FORMAT" }
  ],
  "timestamp": "2026-05-21T10:00:00Z"
}
```

**Response - 429 Too Many Requests:** rate limit.

## 7. Validation Rule

| Field | Type | Required | Rules |
| :--- | :--- | :--- | :--- |
| `email` | string | Yes | Dinh dang email hop le, max 255 ky tu |

## 8. Workflow

1. Client gui email.
2. Validate format → `400` neu sai.
3. Kiem tra rate limit → `429` neu vuot nguong.
4. Tra cuu `USERS` theo email normalized.
5. Neu user ton tai va `status = PENDING_VERIFICATION`:
   - Vo hieu hoa token `EMAIL_VERIFY` cu chua dung (set `used_at` hoac xoa theo policy).
   - Insert token moi + outbox `EMAIL_VERIFICATION_REQUESTED` (`PENDING`).
6. Neu khong thoa dieu kien → khong ghi DB.
7. Tra `200` thong diep chung.

## 9. Database Impact

- **VERIFICATION_TOKENS:** insert token moi; co the update token cu.
- **OUTBOX_EVENTS:** insert `EMAIL_VERIFICATION_REQUESTED` khi tao token thanh cong.

## 10. Transaction & Consistency

- Token + outbox trong 1 transaction.
- Publish email qua worker bat dong bo.

## 11. Security

- Anti-enumeration: khong phan biet email ton tai / da verify.
- Rate limit IP + email.
- Khong log OTP/token.

## 12. FE Behavior

- Man hinh "Cho xac thuc email" co nut "Gui lai ma".
- Countdown 60–120s truoc khi cho phep resend.
- Hien thong bao chung sau khi goi API thanh cong.
- Link quay ve login.

## 13. Acceptance Criteria

- **AC1:** Email thuoc user `PENDING_VERIFICATION` → tao token moi + outbox event.
- **AC2:** Email khong ton tai hoac user da `ACTIVE` → van `200`, khong lo thong tin.
- **AC3:** Rate limit hoat dong khi spam resend.
- **AC4:** Token cu chua dung duoc thay the theo policy.
- **AC5:** Khong log secret.

## 14. Related

- `FR_Register_Email.md`, `FR_Verify_Email.md`
- `docs/use_cases/auth_use_cases/uc-user-authentication.md` (3.1, 3.2)
- `docs/business-spec/auth-service-spec.md` (Verify Email, Register)
- `docs/database/auth_schema.md` (`VERIFICATION_TOKENS`, `OUTBOX_EVENTS`)
