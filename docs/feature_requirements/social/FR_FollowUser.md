# Functional Requirement (FR) - Follow User

## 1. Feature Overview
Cho phep user theo doi user khac de xay dung social graph va following feed.

## 2. Actors
- **User:** Nguoi follow.

## 3. Scope
- **In Scope:**
  - Tao relation trong `FOLLOWS`.
  - Ho tro status `PENDING`/`ACCEPTED`.
  - Publish `USER_FOLLOWED`.
- **Out of Scope:**
  - Notification template chi tiet.

## 4. API Contract
**Endpoint:** `POST /api/v1/social/users/{user_id}/follow`  
**Auth:** Required (JWT)

## 5. Business Rules
- `follower_id != followee_id`.
- Tai khoan public -> `ACCEPTED`; private -> `PENDING` (neu private flow bat).
- Unique `(follower_id, followee_id)`.

## 6. Database Impact
- `FOLLOWS`: insert relation.
- `OUTBOX_EVENTS`: insert `USER_FOLLOWED`.

## 7. Transaction
- Tao relation + outbox event trong cung transaction local.

## 8. Security
- JWT bat buoc.
- Chan tu-follow.

## 9. Acceptance Criteria
- Follow hop le -> 200/201 va relation duoc tao.
- Tu-follow -> 400.
