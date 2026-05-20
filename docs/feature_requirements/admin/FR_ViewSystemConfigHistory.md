# Functional Requirement - View System Config History

## 1. Feature Overview

Cho phep admin co quyen xem lich su thay doi config de audit va rollback thu cong khi can.

## 2. Actors

- **Super Admin/Auditor:** View config history.
- **Admin Service:** Query history.

## 3. Scope

**In Scope:**

- List history by config.
- Show old/new values, reason, actor and timestamp.

**Out of Scope:**

- Automatic rollback.
- Secret value disclosure.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/system-configs/{configId}/history`

**Auth:** Required, permission `SYSTEM_CONFIG_VIEW`.

## 5. Business Rules

- History is append-only.
- Sensitive values must be masked if config is classified as secret-like.
- Sort newest first by default.

## 6. Database Impact

- Read `system_config_history`.
- Join admin actor profile from Auth/internal view if needed.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.
- Mask values according to sensitivity.

## 9. Failure Cases

- Config not found -> 404.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Admin can inspect config changes.
- Response includes actor, reason and before/after values.
- History cannot be modified through this feature.

