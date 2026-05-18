# Functional Requirement (FR) - Xem thong tin tai khoan

## 1. Feature Overview
Cho phep user da dang nhap xem thong tin tai khoan cua chinh minh, tong hop tu `USERS`, `USER_PROFILES`, `USER_SETTINGS`.

## 2. Actors
- **User:** Chu tai khoan da dang nhap.

## 3. Scope
- **In Scope:**
  - Lay thong tin account self.
  - Tra ve data tong hop users/profile/settings.
  - Bao gom `avatar_url` (co the la URL object tren MinIO).
- **Out of Scope:**
  - Xem profile cua user khac (public profile API rieng).

## 4. API Contract
**Endpoint:** `GET /api/v1/users/me`
**Auth:** Required (JWT)

**Response - 200:**
```json
{
  "code": 200,
  "success": true,
  "message": "Lay thong tin tai khoan thanh cong.",
  "data": {
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "status": "ACTIVE",
      "email_verified": true,
      "phone": null,
      "last_login_at": "2026-05-16T10:00:00Z"
    },
    "profile": {
      "display_name": "user",
      "avatar_url": "https://minio.example.com/2hands-avatar/avatars/u1.png",
      "bio": "",
      "website": "",
      "social_links": {},
      "is_private": false
    },
    "settings": {
      "appearance_mode": "SYSTEM"
    }
  },
  "errors": null,
  "timestamp": "2026-05-16T10:00:00Z"
}
```

## 5. Business Rules
- Chi cho phep lay thong tin cua current user trong JWT context.
- Neu user da `DELETED` -> tu choi truy cap (401/404 theo policy he thong).
- `avatar_url` la URL den object storage (MinIO) neu user da cap nhat avatar.

## 6. Database Impact
- Read-only:
  - `USERS`
  - `USER_PROFILES`
  - `USER_SETTINGS`

## 7. Security
- Bat buoc JWT auth.
- Khong expose password_hash va field nhay cam.
- Khong log token.

## 8. Acceptance Criteria
- User da dang nhap nhan duoc du lieu self day du.
- Khong user nao lay duoc thong tin private cua user khac qua endpoint nay.

