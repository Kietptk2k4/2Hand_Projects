# Functional Requirement (FR) - Change Password

## 1. Feature Overview
Chuc nang cho phep user da dang nhap doi mat khau chu dong bang cach nhap mat khau cu va mat khau moi.
Sau khi doi mat khau thanh cong, he thong phai vo hieu hoa tat ca session dang hoat dong de bao ve tai khoan.

## 2. Actors
- **User:** Chu tai khoan da dang nhap.
- **System:** Auth Service thuc thi doi mat khau va revoke session.

## 3. Scope
- **In Scope:**
  - Validate input doi mat khau.
  - Xac minh mat khau cu.
  - Hash va cap nhat mat khau moi.
  - Cap nhat `password_changed_at`.
  - Revoke tat ca `REFRESH_TOKEN_SESSION` dang `ACTIVE`.
  - Ghi outbox event `PASSWORD_CHANGED`.
- **Out of Scope:**
  - Forgot Password / Reset Password flow.
  - Login/logout endpoint behavior.
  - 2FA.

## 4. Preconditions
- User da dang nhap hop le (co authentication context).
- User ton tai va khong o trang thai `DELETED`.
- User cung cap dung mat khau cu.

## 5. Validation Rule
| Field | Type | Required | Rules | Error Message |
| :--- | :--- | :--- | :--- | :--- |
| `current_password` | string | Yes | Khong rong | "Vui long nhap mat khau hien tai." |
| `new_password` | string | Yes | 8-32 ky tu, it nhat 1 chu hoa, 1 chu thuong, 1 chu so | "Mat khau moi khong dat yeu cau do manh." |
| `confirm_new_password` | string | Yes | Phai trung `new_password` | "Xac nhan mat khau moi khong khop." |

## 6. Business Rules
- Mat khau cu phai dung so voi `password_hash` hien tai.
- Mat khau moi khong duoc trung mat khau cu.
- Mat khau moi bat buoc hash (BCrypt/Argon2), khong luu plaintext.
- Sau khi doi mat khau thanh cong:
  - `USERS.password_changed_at = now()`
  - Tat ca refresh sessions cua user dang `ACTIVE` -> `REVOKED`
- Ghi outbox event `PASSWORD_CHANGED` de Notification Service gui canh bao bao mat.

## 7. API Contract
**Endpoint:** `POST /api/v1/auth/change-password`

**Auth:** Required (JWT)

**Request Body:**
```json
{
  "current_password": "OldPassword123!",
  "new_password": "NewPassword456!",
  "confirm_new_password": "NewPassword456!"
}
```

**Response - 200 OK:**
```json
{
  "code": 200,
  "success": true,
  "message": "Doi mat khau thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

**Response - 400 Bad Request:**
```json
{
  "code": 400,
  "success": false,
  "message": "Mat khau hien tai khong chinh xac.",
  "data": null,
  "errors": [
    {
      "field": "current_password",
      "reason": "INVALID_CREDENTIAL"
    }
  ],
  "timestamp": "2026-05-15T10:00:00Z"
}
```

**Response - 401 Unauthorized:**
- Khong co JWT hoac JWT khong hop le.

## 8. Workflow
1. User gui request doi mat khau.
2. Auth Service xac thuc user tu JWT context.
3. Validate payload (sai -> `400`).
4. Lay user va kiem tra `current_password` voi `password_hash`.
5. Neu mat khau cu sai -> `400`.
6. Kiem tra `new_password` khong trung mat khau cu.
7. Mo transaction:
   - update `USERS.password_hash`
   - update `USERS.password_changed_at = now()`
   - revoke all `REFRESH_TOKEN_SESSION` status `ACTIVE` -> `REVOKED`
   - insert `OUTBOX_EVENTS` voi `event_type = PASSWORD_CHANGED`, `status = PENDING`
8. Commit transaction.
9. Tra `200`.

## 9. Database Impact
- **USERS**
  - cap nhat `password_hash`
  - cap nhat `password_changed_at`
  - cap nhat `updated_at`
- **REFRESH_TOKEN_SESSION**
  - cap nhat all rows theo `user_id` va `status = ACTIVE` -> `REVOKED`
- **OUTBOX_EVENTS**
  - insert event `PASSWORD_CHANGED`

## 10. Transaction & Consistency
- Cac buoc cap nhat USERS + revoke sessions + outbox event phai cung 1 transaction ACID.
- Neu co loi bat ky buoc nao -> rollback toan bo.

## 11. Security
- Khong log `current_password`, `new_password`, token.
- Bat buoc hash mat khau moi.
- Bat buoc HTTPS/TLS.
- Khuyen nghi rate limit endpoint doi mat khau.
- Sau khi doi mat khau, cac thiet bi khac bi dang xuat (do refresh session da revoke).

## 12. Error Handling
- `400`: payload sai, current_password sai, confirm password khong khop, new password khong hop le.
- `401`: chua dang nhap/het han JWT.
- `500`: loi he thong.

## 13. FE Behavior
- Form gom:
  - `current_password`
  - `new_password`
  - `confirm_new_password`
- Inline validation cho do manh mat khau moi va confirm password.
- Khi submit:
  - disable nut + hien loading.
  - neu `200`: thong bao thanh cong va dieu huong ve login (vi cac session bi revoke).
  - neu `400`: hien loi field/chung theo response.
  - neu `401`: dieu huong login.

## 14. Acceptance Criteria
- **AC1:** Nhap dung mat khau cu + mat khau moi hop le -> doi mat khau thanh cong, tra `200`.
- **AC2:** `password_changed_at` duoc cap nhat.
- **AC3:** Tat ca refresh session `ACTIVE` cua user bi chuyen `REVOKED`.
- **AC4:** Tao outbox event `PASSWORD_CHANGED` cung transaction.
- **AC5:** Sai mat khau cu -> `400`, khong cap nhat du lieu.

## 15. Mapping to Existing Project Docs
- `docs/business-spec/auth-service-spec.md` (Authentication - Doi mat khau)
- `docs/use-cases/uc-password-recovery.md` (muc 3.3 va 3.4)
- `docs/business-flow/password-recovery-flow.md`
- `docs/business-flow/session-management-flow.md`
- `docs/database/auth_schema.md` (`USERS`, `REFRESH_TOKEN_SESSION`, `OUTBOX_EVENTS`)
- `docs/engineering-rules/api-standard.md`
