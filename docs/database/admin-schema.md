# Admin Service Database Schema

Admin Service su dung PostgreSQL lam source-of-truth cho system configs, announcements, admin audit logs, user enforcements, moderation logs va admin outbox events. Tai lieu nay mo ta schema logic cho MVP, gom bang, enum, constraint, relationship va index quan trong.

## 1. Naming And Type Conventions

- Primary key uu tien UUID.
- Ten bang dung `snake_case` so nhieu.
- Ten cot dung `snake_case`.
- FK den user/admin tu Auth Service chi la logical reference UUID; khong cross-DB FK vat ly neu Auth o database/service rieng.
- JSON fields dung JSONB.
- Timestamps dung timezone-aware timestamp neu DB convention cho phep.
- Critical payload logs phai redact secret/token/password truoc khi luu.

## 2. Core Enums

### `system_config_value_type`

- `INTEGER`
- `DECIMAL`
- `STRING`
- `BOOLEAN`
- `JSON`

### `announcement_severity`

- `INFO`
- `WARNING`
- `CRITICAL`

### `announcement_status`

- `DRAFT`
- `SENT`
- `CANCELLED`

### `admin_action_type`

MVP minimum:

- `USER_SUSPEND`
- `PRODUCT_REMOVE`
- `REVIEW_HIDE`
- `REFUND_EXECUTE`

Recommended extension:

- `USER_BAN`
- `USER_RESTRICT`
- `USER_ENFORCEMENT_REVOKE`
- `SHOP_SUSPEND`
- `SHOP_CLOSE`
- `POST_MODERATE`
- `COMMENT_MODERATE`
- `SYSTEM_CONFIG_CREATE`
- `SYSTEM_CONFIG_UPDATE`
- `SYSTEM_CONFIG_TOGGLE`
- `SYSTEM_ANNOUNCEMENT_PUBLISH`
- `ORDER_SUPPORT_VIEW`
- `PAYMENT_SUPPORT_VIEW`
- `SHIPMENT_SUPPORT_VIEW`

### `user_enforcement_action_type`

- `BAN`
- `SUSPEND`
- `RESTRICT`

### `user_enforcement_status`

- `ACTIVE`
- `REVOKED`
- `EXPIRED`

### `content_moderation_target_type`

- `POST`
- `COMMENT`
- `PRODUCT`
- `REVIEW`

### `content_moderation_action`

- `HIDE`
- `REMOVE`
- `RESTORE`

### `outbox_status`

- `PENDING`
- `PROCESSING`
- `PUBLISHED`
- `FAILED`

## 3. Tables

### 3.1 `system_configs`

Runtime configs cho platform/services.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `config_key` | varchar | NOT NULL, UNIQUE |
| `config_value` | text/jsonb | NOT NULL; value serialized according to `value_type` |
| `value_type` | system_config_value_type | NOT NULL |
| `description` | text | NULLABLE |
| `is_active` | boolean | NOT NULL, default true |
| `created_by` | UUID | NOT NULL, logical admin user id |
| `created_at` | timestamp | NOT NULL |
| `updated_by` | UUID | NULLABLE, logical admin user id |
| `updated_at` | timestamp | NOT NULL |

Business rules:

- `config_key` is immutable after create.
- `config_value` must validate against `value_type`.
- Every create/update/toggle writes `system_config_history`.
- Every critical config change writes `admin_action_logs`.

### 3.2 `system_config_history`

Audit trail rieng cho config changes.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `config_key` | varchar | NOT NULL |
| `old_value` | text/jsonb | NULLABLE for create |
| `new_value` | text/jsonb | NOT NULL |
| `changed_by` | UUID | NOT NULL, logical admin user id |
| `reason` | text | NOT NULL |
| `created_at` | timestamp | NOT NULL |

Relationship:

- `system_configs.config_key` 1 - N `system_config_history.config_key`.

### 3.3 `system_announcements`

Thong bao he thong toan platform.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `title` | varchar | NOT NULL |
| `content` | text | NOT NULL |
| `severity` | announcement_severity | NOT NULL |
| `is_pinned` | boolean | NOT NULL, default false |
| `dismissible` | boolean | NOT NULL, default true |
| `status` | announcement_status | NOT NULL, default `DRAFT` |
| `created_by` | UUID | NOT NULL, logical admin user id |
| `created_at` | timestamp | NOT NULL |
| `sent_at` | timestamp | NULLABLE |

Business rules:

- `sent_at` is set when status becomes `SENT`.
- `CANCELLED` announcement should not be fan-out as active announcement.
- Critical announcements should be audit logged.

### 3.4 `admin_action_logs`

Audit trail cho admin actions.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `admin_id` | UUID | NOT NULL, logical Auth user id |
| `action_type` | admin_action_type | NOT NULL |
| `target_type` | varchar | NOT NULL, example `USER`, `PRODUCT`, `REVIEW`, `SHOP`, `CONFIG` |
| `target_id` | varchar | NOT NULL |
| `request_payload` | jsonb | NULLABLE, only for critical action |
| `response_payload` | jsonb | NULLABLE, only for critical action |
| `ip_address` | varchar | NULLABLE |
| `user_agent` | text | NULLABLE |
| `created_at` | timestamp | NOT NULL |

Business rules:

- Critical actions should store redacted request/response payload.
- Non-critical actions can store payload as null to reduce sensitive data.
- Never log token/password/secret.

### 3.5 `user_enforcements`

Luu enforcement decision len user.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | NOT NULL, logical Auth user id |
| `action_type` | user_enforcement_action_type | NOT NULL |
| `reason_code` | varchar | NOT NULL |
| `description` | text | NOT NULL |
| `expires_at` | timestamp | NULLABLE, null means permanent |
| `enforced_by` | UUID | NOT NULL, logical admin user id |
| `created_at` | timestamp | NOT NULL |
| `status` | user_enforcement_status | NOT NULL, default `ACTIVE` |
| `updated_at` | timestamp | NOT NULL |

Business rules:

- `ACTIVE` enforcement applies until revoked or expired.
- `RESTRICT` allows login but blocks selected write capabilities in Social/Commerce.
- `SUSPEND/BAN` should integrate with Auth to suspend login/session.
- Every status transition writes `user_enforcement_logs`.

### 3.6 `user_enforcement_logs`

History cua enforcement status changes.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `enforcement_id` | UUID | NOT NULL, FK -> `user_enforcements.id` |
| `old_status` | user_enforcement_status | NULLABLE for initial state |
| `new_status` | user_enforcement_status | NOT NULL |
| `admin_id` | UUID | NULLABLE, logical admin id or null/system for job |
| `note` | text | NULLABLE |
| `created_at` | timestamp | NOT NULL |

### 3.7 `content_moderation_logs`

Moderation audit cho content/product/review targets.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `target_type` | content_moderation_target_type | NOT NULL |
| `target_id` | varchar | NULLABLE according input; should be NOT NULL for normal moderation |
| `action` | content_moderation_action | NOT NULL |
| `reason` | text | NOT NULL |
| `admin_id` | UUID | NOT NULL, logical admin user id |
| `created_at` | timestamp | NOT NULL |
| `note` | text | NULLABLE |

Business rules:

- Admin Service logs moderation decision.
- Owner service applies target state.
- `target_id` should be populated for all real target moderation actions; nullable exists for edge/import cases.

### 3.8 `outbox_events`

Admin outbox events for cross-service integration.

| Column | Type | Constraint / Meaning |
|---|---|---|
| `id` | UUID | PK |
| `event_type` | varchar | NOT NULL |
| `aggregate_id` | UUID | NOT NULL |
| `payload` | jsonb | NOT NULL |
| `status` | outbox_status | NOT NULL, default `PENDING` |
| `retry_count` | integer | NOT NULL, default 0 |
| `created_at` | timestamp | NOT NULL |
| `published_at` | timestamp | NULLABLE |

Recommended future fields:

- `event_key` varchar unique nullable for idempotency.
- `last_error` text nullable for retry diagnostics.

## 4. Relationships

- `system_configs` 1 - N `system_config_history` via `config_key`.
- Users/Admins 1 - N `admin_action_logs` via `admin_id`.
- Users 1 - N `user_enforcements` via `user_id`.
- `user_enforcements` 1 - N `user_enforcement_logs`.
- Admin users 1 - N `content_moderation_logs` via `admin_id`.
- Admin users 1 - N `system_announcements` via `created_by`.
- Admin users 1 - N `system_configs` via `created_by`/`updated_by`.
- Domain changes 1 - N `outbox_events` via `aggregate_id` logical relation.

Note:

- `USERS` are owned by Auth Service. Admin DB should not enforce physical FK to Auth DB if service databases are isolated.

## 5. Indexes

Required indexes from MVP schema:

```sql
CREATE INDEX idx_admin_logs_admin_created
ON admin_action_logs(admin_id, created_at DESC);

CREATE INDEX idx_enforcement_user
ON user_enforcements(user_id);

CREATE INDEX idx_moderation_target
ON content_moderation_logs(target_type, target_id);

CREATE INDEX idx_outbox_pending
ON outbox_events(status, created_at)
WHERE status = 'PENDING';
```

Additional recommended indexes:

```sql
CREATE UNIQUE INDEX idx_system_configs_key
ON system_configs(config_key);

CREATE INDEX idx_config_history_key_created
ON system_config_history(config_key, created_at DESC);

CREATE INDEX idx_announcements_status_created
ON system_announcements(status, created_at DESC);

CREATE INDEX idx_enforcement_active_expiring
ON user_enforcements(status, expires_at)
WHERE status = 'ACTIVE' AND expires_at IS NOT NULL;

CREATE INDEX idx_enforcement_user_status
ON user_enforcements(user_id, status);

CREATE INDEX idx_enforcement_logs_enforcement
ON user_enforcement_logs(enforcement_id, created_at DESC);

CREATE INDEX idx_admin_logs_target
ON admin_action_logs(target_type, target_id, created_at DESC);
```

## 6. Event Catalog

Admin Service outbox can publish:

- `USER_SUSPENDED`
- `USER_BANNED`
- `USER_RESTRICTED`
- `USER_ENFORCEMENT_REVOKED`
- `USER_ENFORCEMENT_EXPIRED`
- `PRODUCT_REMOVED`
- `PRODUCT_RESTORED`
- `REVIEW_HIDDEN`
- `REVIEW_RESTORED`
- `SHOP_SUSPENDED`
- `SHOP_RESTORED`
- `SHOP_CLOSED`
- `POST_MODERATED`
- `COMMENT_MODERATED`
- `SYSTEM_CONFIG_UPDATED`
- `SYSTEM_ANNOUNCEMENT_PUBLISHED`

Topic naming should follow `{service}.{domain}.{action}`, for example:

- `admin.user.suspended`
- `admin.user.restricted`
- `admin.product.removed`
- `admin.review.hidden`
- `admin.shop.suspended`
- `admin.config.updated`

## 7. Data Integrity Notes

- Admin Service stores admin decision and audit trail.
- Target service owns final domain state.
- Every config update must write `system_config_history`.
- Every enforcement state transition must write `user_enforcement_logs`.
- Every critical admin action must write `admin_action_logs`.
- Every cross-service event must be written to `outbox_events` in same transaction as local admin decision.
- Enforcement expiration job should update expired rows idempotently.

## 8. MVP Out Of Scope Tables / Fields

Not included in MVP schema:

- Manual refunds table.
- Dispute/case management tables.
- Admin-specific credential/session tables.
- Notification dismissal tracking for announcements.
- Full dead-letter outbox table.
