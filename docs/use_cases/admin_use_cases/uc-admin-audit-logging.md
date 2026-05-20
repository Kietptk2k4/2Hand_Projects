# UC - Admin Audit Logging

## 1. Overview

Use case nay mo ta audit logging cho admin actions. Audit log giup truy vet ai lam gi, tac dong vao target nao, luc nao, tu IP/user-agent nao.

## 2. Actors

- **Admin Service Use Cases:** Emit audit log commands.
- **Super Admin/Auditor:** View audit logs.

## 3. Related Data

- `admin_action_logs`

## 4. Business Rules

- Critical actions must be logged.
- Request/response payload only stored for critical action.
- Payload must be redacted.
- Audit read requires `ADMIN_AUDIT_READ`.

## 5. Sub-Use Cases

### 5.1. Log Enforcement Action

**Main Flow:** Use case writes audit with admin id, action type, target user, payload reason.

### 5.2. Log Moderation Action

**Main Flow:** Use case writes audit with target type/id and action.

### 5.3. Log Config Change

**Main Flow:** Use case writes audit with old/new values redacted if needed.

### 5.4. View Admin Action Logs

**Main Flow:** Authorized admin filters logs by admin, target, action type and date.

## 6. Acceptance Criteria

- Critical actions are auditable.
- Sensitive payload data is redacted.
- Audit read is permission-protected.

