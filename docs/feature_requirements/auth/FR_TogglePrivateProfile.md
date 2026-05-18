# Functional Requirement (FR) - Bat/Tat private profile

## 1. Feature Overview
Cho phep user bat/tat trang thai private profile qua truong `is_private` trong `USER_PROFILES`.

## 2. Actors
- **User:** Chu tai khoan da dang nhap.

## 3. Scope
- **In Scope:**
  - Toggle `is_private`.
  - Ghi outbox event `USER_UPDATED` de service khac dong bo privacy.
- **Out of Scope:**
  - Policy hien thi chi tiet tren service khac (Social se consume event va tu xu ly).

## 4. API Contract
**Endpoint:** `PATCH /api/v1/users/me/privacy`
**Auth:** Required (JWT)

**Request Body:**
```json
{
  "is_private": true
}
```

## 5. Business Rules
- Ownership check bat buoc.
- Thay doi privacy co hieu luc ngay sau khi update DB.
- Ghi `USER_UPDATED` event de cac service khac cap nhat cache/projection.

## 6. Database Impact
- `USER_PROFILES`: update `is_private`, `updated_at`.
- `OUTBOX_EVENTS`: insert `USER_UPDATED`.

## 7. Acceptance Criteria
- User co the bat/tat private profile thanh cong.
- Co outbox event de dong bo he thong.

