# Functional Requirement - Create System Config

## 1. Feature Overview

Cho phep super admin tao runtime config moi cho platform/services.

## 2. Actors

- **Super Admin:** Create config.
- **Admin Service:** Validate and persist config.

## 3. Scope

**In Scope:**

- Create config key/value/type.
- Write config history.
- Write admin action log.
- Publish config event if needed.

**Out of Scope:**

- Secret storage.
- Feature flag targeting.

## 4. API Contract

**Endpoint:** `POST /admin/api/v1/system-configs`

**Auth:** Required, permission `SYSTEM_CONFIG_UPDATE`.

**Request body:**

- `config_key`
- `config_value`
- `value_type`
- `description`
- `is_active`
- `reason`

## 5. Business Rules

- `config_key` unique.
- Value must match `value_type`.
- Reason required.
- Create writes `system_config_history`.
- Critical action writes `admin_action_logs`.

## 6. Database Impact

- Insert `system_configs`.
- Insert `system_config_history`.
- Insert `admin_action_logs`.
- Optional insert `outbox_events`.

## 7. Transaction

- Required.

## 8. Security

- Permission required.
- Do not store secrets as normal config.

## 9. Failure Cases

- Duplicate key -> 409.
- Invalid value type -> 400.
- Missing permission -> 403.

## 10. Acceptance Criteria

- Config is created with unique key.
- History and audit log are written.
- Value type is validated.

