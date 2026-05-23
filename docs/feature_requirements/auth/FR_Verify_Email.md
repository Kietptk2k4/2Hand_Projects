# Functional Requirement (FR) - Verify Email

## 1. Feature Overview
Chuc nang cho phep Guest/User kich hoat tai khoan bang token/OTP da duoc gui qua email sau khi dang ky.
Khi xac thuc thanh cong, tai khoan chuyen tu `PENDING_VERIFICATION` sang `ACTIVE`, dong thoi danh dau email da duoc xac thuc.

## 2. Actors
- **Guest/User:** Nguoi dang so huu token xac thuc email.

## 3. Scope
- **In Scope:**
  - Tiep nhan token/OTP verify email.
  - Kiem tra token hop le/chua het han/chua su dung.
  - Cap nhat trang thai user va danh dau token da dung.
  - Ghi outbox event de cac service khac dong bo user da kich hoat.
- **Out of Scope:**
  - Resend OTP — xem `FR_ResendEmailVerification.md`.
  - Dang ky (API rieng).
  - Login (API rieng).

## 4. Preconditions
- User ton tai trong `USERS`.
- User dang o trang thai `PENDING_VERIFICATION`.
- Co ban ghi token trong `VERIFICATION_TOKENS` voi:
  - `type = EMAIL_VERIFY`
  - `used_at IS NULL`
  - `expires_at > now()`

## 5. API Contract
**Endpoint:** `POST /api/v1/auth/verify-email`

**Request Body:**
```json
{
  "token": "verify_token_or_otp"
}
```

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Xac thuc email thanh cong.",
  "data": {
    "user_id": "uuid-1234-5678",
    "email_verified": true,
    "status": "ACTIVE"
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

**Response - 400 Bad Request (token sai/het han/da dung):**
```json
{
  "code": 400,
  "success": false,
  "message": "Token xac thuc khong hop le hoac da het han.",
  "data": null,
  "errors": [
    {
      "field": "token",
      "reason": "INVALID_OR_EXPIRED"
    }
  ],
  "timestamp": "2026-05-15T10:00:00Z"
}
```

## 6. Business Rules
- Token verify email chi dung 1 lan:
  - Thanh cong => set `used_at = now()`.
- Tai khoan chi duoc kich hoat khi:
  - Token hop le
  - User dang `PENDING_VERIFICATION`
- Sau khi verify thanh cong:
  - `USERS.email_verified = true`
  - `USERS.status = ACTIVE`
- Truong hop verify lai user da ACTIVE:
  - Co the tra 200 idempotent message
  - De dong nhat UX, khuyen nghi tra 200 voi thong bao "Tai khoan da duoc xac thuc truoc do."

## 7. Database Impact
Thuc thi transaction ghi vao cac bang:
1. **VERIFICATION_TOKENS**
   - Cap nhat `used_at = now()` cho token hop le.
2. **USERS**
   - Cap nhat `email_verified = true`
   - Cap nhat `status = ACTIVE`
   - Cap nhat `updated_at = now()`
3. **OUTBOX_EVENTS**
   - Insert event `USER_CREATED` (hoac `USER_UPDATED` theo convention event cua team)
   - `status = PENDING`

## 8. Transaction & Consistency
- Buoc cap nhat `VERIFICATION_TOKENS`, `USERS`, `OUTBOX_EVENTS` bat buoc nam trong **mot transaction ACID**.
- Neu bat ky buoc nao fail => rollback toan bo.
- Xu ly tranh race condition verify dong thoi:
  - lock row token (hoac update co dieu kien `used_at IS NULL`)
  - neu 0 row updated => token da duoc dung/khong hop le.

## 9. Event Flow
1. API Gateway nhan request verify email.
2. Auth Service validate token.
3. Auth Service update token + user trong transaction.
4. Auth Service ghi outbox event.
5. Auth Service tra 200.
6. Outbox worker publish event sang broker cho cac service subscribe.

## 10. Security
- Khong log gia tri token thuan van.
- Khuyen nghi luu `token_hash` trong DB, compare bang hash.
- Gioi han so lan verify that bai/IP de giam brute-force OTP.
- Dung parameterized queries/ORM de ngan SQL injection.

## 11. Error Handling
- `400`: token khong hop le, het han, da su dung, hoac user khong o trang thai cho verify.
- `404` (tuy chon): user khong ton tai (thuong an thong tin nay, uu tien 400 chung).
- `500`: loi he thong.

## 12. FE Behavior
- Man hinh Verify Email gom:
  - O nhap token/OTP
  - Nut "Xac thuc"
  - Nut/link "Gui lai ma" (goi API resend - ngoai pham vi FR nay)
- UX:
  - Disable submit khi token rong/khong dung format.
  - Hien loading state khi goi API.
  - Neu 200: hien thong bao thanh cong, dieu huong sang Login hoac Home.
  - Neu 400: hien thong bao "Token khong hop le hoac da het han."

## 13. Acceptance Criteria
- **AC1:** Token hop le, chua het han, chua dung => user duoc chuyen `PENDING_VERIFICATION -> ACTIVE`.
- **AC2:** `email_verified` duoc set `true` sau khi verify thanh cong.
- **AC3:** Token khong hop le/het han/da dung => API tra `400`, khong thay doi `USERS`.
- **AC4:** Co ban ghi outbox event duoc tao cung transaction verify thanh cong.
- **AC5:** Khong lo token/password trong logs.

## 14. Mapping to Existing Project Docs
- Phu hop voi:
  - `docs/use-cases/uc-user-authentication.md` (muc 3.2 Verify Email)
  - `docs/business-flow/authentication-lifecycle-flow.md`
  - `docs/business-spec/auth-service-spec.md` (Authentication - Verify Email)
  - `docs/database/auth_schema.md` (`USERS`, `VERIFICATION_TOKENS`, `OUTBOX_EVENTS`)
  - `docs/engineering-rules/api-standard.md` (response format)
