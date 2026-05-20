# Functional Requirement - Update System Config

## 1. Feature Overview

Cho phep super admin cap nhat gia tri config runtime, co history va audit trail day du.

## 2. Actors

- **Super Admin:** Update config.
- **System:** Persist current value and history.

## 3. Scope

**In Scope:**

- Update config value/description.
- Validate value type.
- Record before/after values.
- Publish config changed event.

**Out of Scope:**

- Secret rotation.
- Service-specific rollout strategy.

## 4. API Contract

**Endpoint:** `PATCH /admin/api/v1/system-configs/{configId}`

**Auth:** Required, permission `SYSTEM_CONFIG_UPDATE`.

**Request body:**

- `config_value`
- `description` optional
- `reason`

## 5. Business Rules

- Reason required.
- New value must match existing `value_type`.
- History stores old and new values.
- Critical config changes should require stronger permission or approval in later phases.

## 6. Database Impact

- Update `system_configs`.
- Insert `system_config_history`.
- Insert `admin_action_logs`.
- Insert `outbox_events` with `SYSTEM_CONFIG_CHANGED`.

## 7. Transaction

- Required.

## 8. Security

- Super admin permission required.
- Do not log secrets.

## 9. Failure Cases

- Config not found -> 404.
- Invalid value -> 400.
- Missing reason -> 400.

## 10. Acceptance Criteria

- Config current value changes.
- History contains before/after values.
- Audit log references actor and reason.

