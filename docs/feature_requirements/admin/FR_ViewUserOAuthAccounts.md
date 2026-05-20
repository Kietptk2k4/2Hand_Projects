# Functional Requirement - View User OAuth Accounts

## 1. Feature Overview

Cho phep admin/support xem danh sach OAuth accounts lien ket voi user phuc vu dieu tra account linking va suspicious login.

## 2. Actors

- **Support/Admin:** Xem OAuth account links.
- **Auth Service:** Own OAuth account data.
- **Admin Service:** Authorize request.

## 3. Scope

**In Scope:**

- List OAuth providers linked to user.
- Include provider name and linked timestamp.

**Out of Scope:**

- Link/unlink OAuth account.
- Expose OAuth access tokens.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/users/{userId}/oauth-accounts`

**Auth:** Required, permission `USER_INVESTIGATION_READ`.

## 5. Business Rules

- Auth owns OAuth account records.
- OAuth tokens/secrets must never be returned.
- Support read can be audit logged.

## 6. Database Impact

- Admin Service: optional audit log.
- Auth Service: read OAuth accounts.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- No provider token/secret exposure.

## 9. Failure Cases

- User not found -> 404.
- Auth unavailable -> 503.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized admin can view linked OAuth provider metadata.
- OAuth secrets are never exposed.
- Unauthorized admin is denied.

