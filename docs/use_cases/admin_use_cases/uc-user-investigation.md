# UC - User Investigation

## 1. Overview

Use case nay mo ta cac man hinh/operation admin dung de dieu tra user truoc khi enforcement: xem profile, login history, active sessions, OAuth accounts, roles va enforcement history.

## 2. Actors

- **Support/Admin/Moderator:** Xem thong tin dieu tra.
- **Auth Service:** Own profile/session/login/OAuth/role data.
- **Admin Service:** Own enforcement history and audit.

## 3. Related Data

- Auth Service user/profile/session/login history APIs.
- `user_enforcements`
- `user_enforcement_logs`
- `admin_action_logs`

## 4. Business Rules

- Investigation requires explicit support/moderation permission.
- Do not expose password/token/OTP/secret.
- Sensitive support read can be audit logged.
- Admin Service does not mutate Auth user data in investigation flow.

## 5. Sub-Use Cases

### 5.1. View User Profile For Investigation

**Main Flow:** Admin requests user profile; Admin Service checks permission and fetches profile from Auth Service.

### 5.2. View Login History

**Main Flow:** Admin requests login history; Auth Service returns attempts with timestamp, IP, device and status.

### 5.3. View Active Sessions

**Main Flow:** Admin requests active sessions; Auth Service returns active devices/sessions without secret tokens.

### 5.4. View OAuth Accounts

**Main Flow:** Admin requests linked OAuth providers for the user.

### 5.5. View User Roles

**Main Flow:** Admin requests roles/permissions from Auth Service.

### 5.6. View Enforcement Context

**Main Flow:** Admin Service returns current enforcement and history from Admin DB.

## 6. Acceptance Criteria

- Authorized support/admin can view investigation dossier.
- Unauthorized users receive 403.
- No secrets/tokens are returned.
- Enforcement context is included.

