# UC - Admin Auth Authorization

## 1. Overview

Use case nay mo ta cach Admin Service bao ve admin APIs bang authentication va authorization tu Auth Service. Admin Service khong own password/session/role source-of-truth; no chi verify JWT, doc claims hoac goi Auth Service de check role/permission.

## 2. Actors

- **Admin/Moderator/Support/Super Admin:** Goi admin APIs.
- **Auth Service:** Own login, refresh token, logout, session revoke, role/permission.
- **Admin Service:** Authorize request truoc khi execute use case.

## 3. Related Data

- JWT/Auth claims from Auth Service.
- `admin_action_logs` optional for critical/failed admin actions.

## 4. Business Rules

- Admin id lay tu JWT, khong lay tu request body.
- Admin APIs require JWT.
- Permission check bat buoc cho destructive/support-sensitive action.
- Admin Service khong luu password/token/refresh session.
- Do not log tokens, passwords, OTPs, secrets.

## 5. Sub-Use Cases

### 5.1. Admin Login

**Main Flow:**

1. Admin submit credentials to Auth Service.
2. Auth Service validates admin identity.
3. Auth Service returns access token and refresh token.
4. Admin frontend uses access token for Admin Service APIs.

**Exception Flow:** Invalid credentials -> 401; non-admin user -> 403.

### 5.2. Authorize Admin API

**Main Flow:**

1. Admin request protected endpoint.
2. Admin Service validates JWT.
3. Admin Service checks required permission from token claims or Auth Service.
4. If authorized, execute use case.
5. If denied, return 403.

### 5.3. Admin Logout / Revoke Session

**Main Flow:**

1. Admin requests logout or super admin requests revoke session.
2. Admin Service delegates to Auth Service.
3. Auth Service revokes refresh/session.

## 6. Acceptance Criteria

- Unauthenticated admin requests return 401.
- Missing permission returns 403.
- Authorized admin can execute allowed action.
- Admin Service does not store credentials or sessions.

