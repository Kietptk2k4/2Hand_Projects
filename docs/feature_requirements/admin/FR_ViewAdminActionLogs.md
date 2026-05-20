# Functional Requirement - View Admin Action Logs

## 1. Feature Overview

Cho phep auditor/super admin tra cuu admin action logs theo actor, action, target, status va thoi gian.

## 2. Actors

- **Auditor/Super Admin:** Search and view logs.
- **Admin Service:** Query logs.

## 3. Scope

**In Scope:**

- Filter/paginate admin action logs.
- View log detail.
- Mask sensitive payload fields.

**Out of Scope:**

- Log modification/deletion.
- SIEM export.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/admin-action-logs`

**Auth:** Required, permission `ADMIN_AUDIT_VIEW`.

**Query params:**

- `admin_id`
- `action`
- `target_type`
- `target_id`
- `status`
- `from`
- `to`
- `page`
- `size`

## 5. Business Rules

- Logs are append-only and read-only.
- Default sort newest first.
- Payload display must be sanitized.

## 6. Database Impact

- Read `admin_action_logs`.

## 7. Transaction

- Read-only.

## 8. Security

- Audit permission required.
- Response must not expose secrets.

## 9. Failure Cases

- Invalid date range -> 400.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Authorized admin can search logs.
- Logs include actor/action/target/status/timestamp.
- No mutation endpoint exists for audit logs.

