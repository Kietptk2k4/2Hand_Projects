# Functional Requirement (FR) - Cap nhat user settings

## 1. Feature Overview
Cho phep user cap nhat cau hinh giao dien ca nhan trong `USER_SETTINGS`, hien tai bao gom `appearance_mode`.

## 2. Actors
- **User:** Chu tai khoan da dang nhap.

## 3. Scope
- **In Scope:**
  - Cap nhat `appearance_mode`.
- **Out of Scope:**
  - Notification settings chi tiet (chua thuoc MVP auth-service hien tai).

## 4. API Contract
**Endpoint:** `PATCH /api/v1/users/me/settings`
**Auth:** Required (JWT)

**Request Body:**
```json
{
  "appearance_mode": "DARK"
}
```

## 5. Business Rules
- Chi nhan enum hop le: `LIGHT`, `DARK`, `SYSTEM`.
- Ownership check bat buoc.
- Cap nhat thanh cong thi tra settings moi cho FE.

## 6. Database Impact
- `USER_SETTINGS`: update `appearance_mode`, `updated_at`.

## 7. Error Handling
- `400`: enum khong hop le.
- `401`: chua dang nhap/invalid JWT.

## 8. Acceptance Criteria
- User cap nhat settings thanh cong.
- Gia tri enum sai bi tu choi voi 400.

