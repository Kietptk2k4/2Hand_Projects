# UC - System Config Management

## 1. Overview

Use case nay mo ta quan ly runtime configs cua he thong. Moi config update la critical action, bat buoc ghi config history va admin audit log.

## 2. Actors

- **Super Admin:** Tao/cap nhat/bat tat config.
- **Consumer Services:** Consume config update events.

## 3. Related Data

- `system_configs`
- `system_config_history`
- `admin_action_logs`
- `outbox_events`

## 4. Business Rules

- `config_key` unique and immutable.
- `config_value` must match `value_type`.
- Every create/update/toggle writes history.
- Every change writes admin action log.
- Publish `SYSTEM_CONFIG_UPDATED`.

## 5. Sub-Use Cases

### 5.1. Create Config

**Main Flow:** Validate key/value/type, insert config, write history/audit/outbox.

### 5.2. Update Config

**Main Flow:** Validate permission and type, update value, write old/new history, audit log and outbox.

### 5.3. Toggle Config

**Main Flow:** Update `is_active`, write history/audit/outbox.

### 5.4. View Config History

**Main Flow:** Query `system_config_history` by config key.

## 6. Acceptance Criteria

- Config update validates value type.
- History is written for every change.
- Admin action log is written.
- Config update event is published.

