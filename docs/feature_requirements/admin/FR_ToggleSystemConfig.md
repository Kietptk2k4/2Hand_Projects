# Functional Requirement - Toggle System Config

## 1. Feature Overview

Cho phep super admin bat/tat config thong qua `is_active`, phuc vu tam dung config hoac feature flag don gian.

## 2. Actors

- **Super Admin:** Toggle config.
- **System:** Persist activation state.

## 3. Scope

**In Scope:**

- Update `is_active`.
- Record history and audit log.
- Publish config toggled event.

**Out of Scope:**

- Gradual rollout.
- User segmentation.

## 4. API Contract

**Endpoint:** `PATCH /admin/api/v1/system-configs/{configId}/toggle`

**Auth:** Required, permission `SYSTEM_CONFIG_UPDATE`.

**Request body:**

- `is_active`
- `reason`

## 5. Business Rules

- Reason required.
- Toggling to current state can be idempotent.
- Deactivated config should not be consumed by runtime config resolver unless resolver explicitly allows inactive values.

## 6. Database Impact

- Update `system_configs.is_active`.
- Insert `system_config_history`.
- Insert `admin_action_logs`.
- Insert `outbox_events`.

## 7. Transaction

- Required.

## 8. Security

- Permission required.

## 9. Failure Cases

- Config not found -> 404.
- Missing reason -> 400.

## 10. Acceptance Criteria

- Config active state changes.
- Toggle is traceable in history and audit logs.
- Event is available for consumers.

