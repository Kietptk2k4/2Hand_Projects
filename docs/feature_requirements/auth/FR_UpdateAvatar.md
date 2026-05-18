# Functional Requirement (FR) - Cap nhat avatar

## 1. Feature Overview
Cho phep user cap nhat anh dai dien (`avatar_url`) cho profile. He thong su dung MinIO lam object storage.

## 2. Actors
- **User:** Chu tai khoan da dang nhap.

## 3. MinIO Context
- Docker compose co service MinIO:
  - API: `http://localhost:9000`
  - Console: `http://localhost:9001`
- Auth-service luu `avatar_url` trong `USER_PROFILES`; file avatar duoc upload len MinIO bucket (vi du `2hands-avatar`).

## 4. Scope
- **In Scope:**
  - Nhan `avatar_url` hop le sau khi upload file len MinIO.
  - Cap nhat `USER_PROFILES.avatar_url`.
  - Ghi outbox event `USER_UPDATED`.
- **Out of Scope:**
  - Xu ly binary upload stream trong endpoint nay.
  - Quan ly bucket/chinh sach MinIO chi tiet.

## 5. API Contract
**Endpoint:** `PATCH /api/v1/users/me/avatar`
**Auth:** Required (JWT)

**Request Body:**
```json
{
  "avatar_url": "https://minio.example.com/2hands-avatar/avatars/user-123.png"
}
```

## 6. Business Rules
- Ownership check: chi cap nhat avatar cua current user.
- `avatar_url` bat buoc la URL hop le, khuyen nghi thuoc domain object storage cua he thong (MinIO public endpoint/CDN).
- Khong overwrite avatar tu OAuth login sau khi user da tu chinh avatar.
- Cap nhat thanh cong thi phat `USER_UPDATED`.

## 7. Database Impact
- `USER_PROFILES`: update `avatar_url`, `updated_at`.
- `OUTBOX_EVENTS`: insert `USER_UPDATED` (`status = PENDING`).

## 8. Transaction
- Update avatar + outbox event trong 1 transaction.

## 9. Security
- JWT required.
- URL validation de tranh script/url doc hai.
- Khong log token dang upload hoac signed URL.

## 10. FE Behavior
- FE upload file len MinIO (qua presigned URL hoac upload gateway), sau do goi API nay de luu `avatar_url`.

## 11. Acceptance Criteria
- Avatar duoc cap nhat thanh cong va hien thi lai tren profile.
- Co outbox event de Social Service dong bo avatar moi.

