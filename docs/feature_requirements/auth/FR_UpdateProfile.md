# Functional Requirement (FR) - Cap nhat ho so ca nhan

## 1. Feature Overview
Cho phep user cap nhat thong tin profile ca nhan (khong bao gom doi avatar rieng): `display_name`, `bio`, `website`, `social_links`.

## 2. Actors
- **User:** Chu tai khoan da dang nhap.

## 3. Scope
- **In Scope:**
  - Cap nhat thong tin profile trong `USER_PROFILES`.
  - Validate dinh dang URL/JSON.
  - Ghi outbox event `USER_UPDATED`.
- **Out of Scope:**
  - Upload file avatar len MinIO (thuoc FR_UpdateAvatar).

## 4. API Contract
**Endpoint:** `PUT /api/v1/users/me/profile`
**Auth:** Required (JWT)

**Request Body:**
```json
{
  "display_name": "Kiet Tran",
  "bio": "Backend developer",
  "website": "https://example.com",
  "social_links": {
    "facebook": "https://facebook.com/user",
    "github": "https://github.com/user"
  }
}
```

## 5. Business Rules
- Cap nhat chi cho current user.
- `display_name` gioi han do dai, khong chua noi dung vi pham policy.
- `website` va `social_links` phai la URL hop le.
- Thanh cong thi ghi `USER_UPDATED` vao `OUTBOX_EVENTS` de dong bo xuong service khac.

## 6. Database Impact
- `USER_PROFILES`: update `display_name`, `bio`, `website`, `social_links`, `updated_at`.
- `OUTBOX_EVENTS`: insert `USER_UPDATED` (`status = PENDING`).

## 7. Transaction
- Update profile + outbox event trong 1 transaction.

## 8. Security
- JWT required.
- Ownership check bat buoc.

## 9. Acceptance Criteria
- Cap nhat profile thanh cong thi du lieu duoc luu va co outbox event.
- Payload sai format tra 400.

