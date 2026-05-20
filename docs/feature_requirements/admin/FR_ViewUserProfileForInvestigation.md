# Functional Requirement - View User Profile For Investigation

## 1. Feature Overview

Cho phep admin/support xem profile user phuc vu dieu tra abuse/spam/suspicious behavior. Profile source-of-truth thuoc Auth Service.

## 2. Actors

- **Support/Admin:** Xem profile.
- **Auth Service:** Own profile source-of-truth.
- **Admin Service:** Authorize and compose investigation response.

## 3. Scope

**In Scope:**

- View public/support-safe profile fields.
- Include account status and creation metadata.
- Optional include current enforcement summary.

**Out of Scope:**

- Edit user profile.
- Password/email verification mutation.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/users/{userId}/profile`

**Auth:** Required, permission `USER_INVESTIGATION_READ`.

## 5. Business Rules

- Auth Service owns profile fields.
- Admin Service may enrich with current enforcement from Admin DB.
- Sensitive data minimized.
- No password/token/secret returned.

## 6. Database Impact

- Admin Service: read `user_enforcements` optional.
- Auth Service: read user profile.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- PII exposure must be limited to support need.

## 9. Failure Cases

- User not found -> 404.
- Auth unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized admin sees investigation profile.
- Sensitive fields are not exposed.
- Current enforcement context can be included.

