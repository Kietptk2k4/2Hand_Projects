# Functional Requirement (FR) - Soft delete account

## 1. Feature Overview
Cho phep user yeu cau xoa tai khoan theo co che soft delete de van giu du lieu doi soat.

## 2. Actors
- **User:** Chu tai khoan da dang nhap.

## 3. Scope
- **In Scope:**
  - Xac nhan bang mat khau.
  - Chuyen trang thai `USERS.status` sang `DELETED`.
  - Set `deleted_at`.
  - Revoke toan bo refresh sessions.
  - Ghi outbox event `USER_DELETED`.
- **Out of Scope:**
  - Hard delete vat ly du lieu.
  - Xoa object avatar tren MinIO ngay lap tuc (co the xu ly async bang event consumer/job rieng).

## 4. API Contract
**Endpoint:** `POST /api/v1/users/me/soft-delete`
**Auth:** Required (JWT)

**Request Body:**
```json
{
  "password": "CurrentPassword123!"
}
```

## 5. Business Rules
- Mat khau xac nhan phai dung.
- User dang `DELETED` khong duoc thuc hien lai flow update profile/settings.
- Sau khi soft delete:
  - user khong duoc login nua.
  - tat ca refresh sessions ACTIVE -> REVOKED.
- Ghi event `USER_DELETED` de cac service khac an/vo hieu hoa du lieu lien quan.
- Avatar object tren MinIO khong bat buoc xoa dong bo trong transaction nay; xu ly boi background worker neu can.

## 6. Database Impact
- `USERS`: update `status = DELETED`, `deleted_at`, `updated_at`.
- `REFRESH_TOKEN_SESSION`: revoke all ACTIVE sessions cua user.
- `OUTBOX_EVENTS`: insert `USER_DELETED`.

## 7. Transaction
- Users update + session revoke + outbox insert phai cung 1 transaction.

## 8. Error Handling
- `400`: sai mat khau.
- `401`: unauthorized.
- `409`: user khong du dieu kien xoa (neu co rule lien ket voi service khac).

## 9. Acceptance Criteria
- Soft delete thanh cong thi user bi khoa login va session bi revoke.
- Co outbox event `USER_DELETED` de dong bo he thong.

