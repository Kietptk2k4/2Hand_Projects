# Functional Requirement - Restrict User

## 1. Feature Overview

Cho phep admin restrict user. Restricted user van co the login va mua hang, nhung bi chan cac write actions nhu post, comment, review, create product theo policy cua Social/Commerce.

## 2. Actors

- **Admin/Moderator:** Restrict user.
- **Social Service:** Blocks social write actions.
- **Commerce Service:** Blocks review/create product or configured writes.
- **Admin Service:** Stores enforcement and publishes event.

## 3. Scope

**In Scope:**

- Create `RESTRICT` enforcement.
- Write enforcement/audit logs.
- Publish `USER_RESTRICTED`.

**Out of Scope:**

- Login blocking.
- Session revoke by default.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/users/{userId}/restrict`

**Auth:** Required, permission `USER_RESTRICT`.

**Request body:**

- `reason_code`
- `description`
- `expires_at` optional

## 5. Business Rules

- Restricted user can login.
- Restricted user can buy unless policy says otherwise.
- Restricted user cannot perform configured writes:
  - create post.
  - comment.
  - review.
  - create product.
- Consumer services apply restrictions from event/cache.

## 6. Database Impact

- Insert `user_enforcements`.
- Insert `user_enforcement_logs`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required for Admin DB writes.

## 8. Security

- Permission required.
- Admin id from JWT.

## 9. Failure Cases

- Missing permission -> 403.
- User not found -> 404.
- Invalid expiration -> 400.

## 10. Acceptance Criteria

- Authorized admin can restrict user.
- Restriction event is published.
- Auth login is not blocked solely by restrict.
- Social/Commerce can enforce restricted writes.

