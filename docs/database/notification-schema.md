# Notification Service Database Schema (MVP)

Notification Service su dung **PostgreSQL** va so huu cac bang lien quan den event ingestion, in-app notification, push device token va notification settings. Schema nay duoc thiet ke cho at-least-once event delivery, idempotent processing va retry co kiem soat.

Service khong duoc tao physical foreign key sang database cua Auth/Social/Commerce/Admin. Cac truong nhu `user_id`, `actor_id`, `reference_id` la logical FK theo event contract/JWT/internal API.

## 1. Enumerations

### 1.1. notification_event_status

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

Meaning:

- `PENDING`: event da ingest, chua xu ly.
- `PROCESSING`: worker dang xu ly.
- `COMPLETED`: xu ly xong, da tao/gui notification theo policy.
- `FAILED`: xu ly loi, co the retry neu con retry budget.

### 1.2. notification_delivery_status

```text
PENDING
SENT
FAILED
```

Meaning:

- `PENDING`: notification da tao nhung delivery policy chua hoan tat.
- `SENT`: notification san sang hien thi/gui thanh cong theo channel bat buoc.
- `FAILED`: delivery loi va can retry/inspect.

### 1.3. notification_source_service

```text
AUTH
SOCIAL
COMMERCE
ADMIN
SYSTEM
```

### 1.4. device_type

```text
IOS
ANDROID
WEB
```

### 1.5. notification_channel

Channel enum nay chu yeu dung trong code/config/payload routing. MVP chua bat buoc co table delivery rieng.

```text
IN_APP
PUSH
EMAIL
```

## 2. Table: notification_events

Internal durable queue cho cac domain events Notification Service consume tu broker/outbox cua service khac.

### 2.1. Columns

| Column | Type | Nullable | Default | Description |
|---|---:|---:|---:|---|
| `id` | UUID | No | generated | Primary key cua Notification Service. |
| `source_event_id` | UUID | Yes | null | ID cua outbox/domain event ben producer. Dung cho idempotency. |
| `event_key` | VARCHAR(255) | Yes | null | Deterministic key tu producer neu co, vi du `commerce.payment.{paymentId}.success`. |
| `event_type` | VARCHAR(100) | No | - | Event type `UPPER_SNAKE_CASE`, vi du `POST_LIKED`, `PAYMENT_SUCCESS`. |
| `source_service` | notification_source_service | No | - | Service publish event: `AUTH`, `SOCIAL`, `COMMERCE`, `ADMIN`, `SYSTEM`. |
| `aggregate_type` | VARCHAR(80) | Yes | null | Root entity cua producer, vi du `POST`, `ORDER`, `PAYMENT`, `USER`. |
| `aggregate_id` | VARCHAR(100) | Yes | null | ID cua aggregate ben producer. String de ho tro UUID/number/external id. |
| `actor_id` | UUID | Yes | null | User gay ra action neu co. Logical FK to Auth users. |
| `recipient_user_id` | UUID | Yes | null | Recipient duy nhat neu event chi target mot user. Broadcast/multi-recipient co the de null va dung payload. |
| `payload` | JSONB | No | `'{}'::jsonb` | Sanitized event payload/envelope. |
| `status` | notification_event_status | No | `PENDING` | Processing status. |
| `retry_count` | INTEGER | No | `0` | So lan retry da thuc hien. |
| `max_retry_count` | INTEGER | No | `5` | Gioi han retry cho event nay. |
| `last_error` | TEXT | Yes | null | Loi gan nhat, da sanitize/truncate. |
| `locked_at` | TIMESTAMPTZ | Yes | null | Thoi diem worker lock event de process. |
| `locked_by` | VARCHAR(100) | Yes | null | Worker id/process id dang xu ly. |
| `created_at` | TIMESTAMPTZ | No | `now()` | Thoi diem ingest vao Notification Service. |
| `processed_at` | TIMESTAMPTZ | Yes | null | Thoi diem xu ly completed. |

### 2.2. Constraints

```sql
ALTER TABLE notification_events
  ADD CONSTRAINT pk_notification_events
  PRIMARY KEY (id);

ALTER TABLE notification_events
  ADD CONSTRAINT chk_notification_events_retry_count
  CHECK (retry_count >= 0 AND max_retry_count >= 0 AND retry_count <= max_retry_count);

ALTER TABLE notification_events
  ADD CONSTRAINT chk_notification_events_processed_at
  CHECK (
    (status = 'COMPLETED' AND processed_at IS NOT NULL)
    OR (status <> 'COMPLETED')
  );
```

Recommended unique constraints/indexes:

```sql
CREATE UNIQUE INDEX uq_notification_events_source_event
ON notification_events(source_service, source_event_id)
WHERE source_event_id IS NOT NULL;

CREATE UNIQUE INDEX uq_notification_events_event_key
ON notification_events(source_service, event_key)
WHERE event_key IS NOT NULL;
```

Reason:

- Broker/outbox delivery is at-least-once.
- Same producer event must not create duplicate notification processing.

### 2.3. Indexes

```sql
CREATE INDEX idx_notification_events_status
ON notification_events(status, created_at);

CREATE INDEX idx_notification_events_retry
ON notification_events(status, retry_count, created_at)
WHERE status IN ('PENDING', 'FAILED');

CREATE INDEX idx_notification_events_locked
ON notification_events(status, locked_at)
WHERE status = 'PROCESSING';

CREATE INDEX idx_notification_events_source_type
ON notification_events(source_service, event_type, created_at);

CREATE INDEX idx_notification_events_recipient
ON notification_events(recipient_user_id, created_at)
WHERE recipient_user_id IS NOT NULL;
```

### 2.4. State Transition Rules

```text
PENDING -> PROCESSING
PROCESSING -> COMPLETED
PROCESSING -> FAILED
FAILED -> PROCESSING
PROCESSING -> FAILED when stale lock is recovered
```

Rules:

- `processed_at` is set only when status becomes `COMPLETED`.
- Retryable `FAILED` rows can be processed again if `retry_count < max_retry_count`.
- Worker should use row locking/batching to avoid duplicate processing.
- `last_error` must not contain token, OTP, email secret, provider credential or raw authorization header.

## 3. Table: user_notifications

Stores user-visible in-app notifications. One `notification_events` row can generate many `user_notifications`, for example order created notification to buyer and multiple sellers, or system announcement broadcast.

### 3.1. Columns

| Column | Type | Nullable | Default | Description |
|---|---:|---:|---:|---|
| `id` | UUID | No | generated | Primary key. |
| `notification_event_id` | UUID | Yes | null | FK to `notification_events.id`. Nullable for manual/local notification future use. |
| `user_id` | UUID | No | - | Recipient user. Logical FK to Auth users. |
| `actor_id` | UUID | Yes | null | Actor user if action caused by another user. Logical FK to Auth users. |
| `type` | VARCHAR(100) | No | - | UI/business notification type, usually same family as event type. |
| `title` | VARCHAR(255) | No | - | Short notification title. |
| `content` | TEXT | No | - | Body shown to user. |
| `reference_type` | VARCHAR(80) | Yes | null | Linked resource type: `POST`, `COMMENT`, `ORDER`, `PAYMENT`, `SHIPMENT`, `PRODUCT`, `SHOP`, `SYSTEM_ANNOUNCEMENT`. |
| `reference_id` | VARCHAR(100) | Yes | null | Linked resource id. String to support IDs from other services. |
| `is_read` | BOOLEAN | No | `false` | Read status. |
| `is_deleted` | BOOLEAN | No | `false` | Soft delete/hide status. |
| `metadata` | JSONB | No | `'{}'::jsonb` | Sanitized deep link, thumbnail, template params, pinned/dismissible flags. |
| `delivery_status` | notification_delivery_status | No | `PENDING` | General delivery status for MVP. |
| `created_at` | TIMESTAMPTZ | No | `now()` | Creation time. |
| `read_at` | TIMESTAMPTZ | Yes | null | Read time. |

### 3.2. Constraints

```sql
ALTER TABLE user_notifications
  ADD CONSTRAINT pk_user_notifications
  PRIMARY KEY (id);

ALTER TABLE user_notifications
  ADD CONSTRAINT fk_user_notifications_notification_event
  FOREIGN KEY (notification_event_id)
  REFERENCES notification_events(id)
  ON DELETE SET NULL;

ALTER TABLE user_notifications
  ADD CONSTRAINT chk_user_notifications_read_at
  CHECK (
    (is_read = false AND read_at IS NULL)
    OR (is_read = true)
  );
```

Recommended duplicate-prevention index:

```sql
CREATE UNIQUE INDEX uq_user_notifications_event_recipient_reference
ON user_notifications(
  notification_event_id,
  user_id,
  type,
  COALESCE(reference_type, ''),
  COALESCE(reference_id, '')
)
WHERE notification_event_id IS NOT NULL;
```

Reason:

- Retrying same event must not create duplicate notification for same user/reference.
- One event can still create multiple notifications for different users.

### 3.3. Indexes

```sql
CREATE INDEX idx_user_notifications_user_created
ON user_notifications(user_id, created_at DESC)
WHERE is_deleted = false;

CREATE INDEX idx_user_notifications_user_unread
ON user_notifications(user_id, is_read, created_at DESC)
WHERE is_deleted = false;

CREATE INDEX idx_user_notifications_reference
ON user_notifications(reference_type, reference_id)
WHERE reference_type IS NOT NULL AND reference_id IS NOT NULL;

CREATE INDEX idx_user_notifications_event
ON user_notifications(notification_event_id)
WHERE notification_event_id IS NOT NULL;

CREATE INDEX idx_user_notifications_delivery_status
ON user_notifications(delivery_status, created_at)
WHERE delivery_status IN ('PENDING', 'FAILED');
```

### 3.4. Business Rules

- `user_id` is recipient and must come from event contract/routing logic, not client request body for create.
- User can only view/update/delete own notifications.
- Delete is soft delete: `is_deleted = true`.
- Default list filters `is_deleted = false`.
- Unread count filters `is_deleted = false AND is_read = false`.
- `read_at` is set when marking read.
- `metadata` must be sanitized before storing/returning.
- Deep link authorization still belongs to owner service; notification visibility does not grant resource permission.

## 4. Table: user_device_tokens

Stores active/inactive FCM/APNS/Web push device tokens for push notification.

### 4.1. Columns

| Column | Type | Nullable | Default | Description |
|---|---:|---:|---:|---|
| `id` | UUID | No | generated | Primary key. |
| `user_id` | UUID | No | - | Token owner. Logical FK to Auth users. |
| `device_type` | device_type | No | - | `IOS`, `ANDROID`, `WEB`. |
| `device_token` | VARCHAR(512) | No | - | Provider device token. Sensitive value. |
| `is_active` | BOOLEAN | No | `true` | Whether token can receive push. |
| `updated_at` | TIMESTAMPTZ | No | `now()` | Last update time. |
| `last_used_at` | TIMESTAMPTZ | Yes | null | Last successful use or registration time. |
| `created_at` | TIMESTAMPTZ | No | `now()` | Creation time. |

### 4.2. Constraints

```sql
ALTER TABLE user_device_tokens
  ADD CONSTRAINT pk_user_device_tokens
  PRIMARY KEY (id);

ALTER TABLE user_device_tokens
  ADD CONSTRAINT uq_user_device_tokens_device_token
  UNIQUE (device_token);
```

Optional, if one user should not store same token twice after token rotation:

```sql
CREATE UNIQUE INDEX uq_user_device_tokens_user_token
ON user_device_tokens(user_id, device_token);
```

### 4.3. Indexes

```sql
CREATE INDEX idx_user_device_tokens_user_active
ON user_device_tokens(user_id, is_active, updated_at DESC);

CREATE INDEX idx_user_device_tokens_active_type
ON user_device_tokens(device_type, is_active);

CREATE INDEX idx_user_device_tokens_last_used
ON user_device_tokens(last_used_at)
WHERE is_active = true;
```

### 4.4. Business Rules

- Register token is upsert by `device_token`.
- Logout/revoke sets `is_active = false`, not hard delete.
- FCM invalid/unregistered token sets `is_active = false`.
- Do not log full `device_token`.
- User can manage only own tokens through user APIs.
- Service-to-service/admin cleanup must be internal-only.

## 5. Table: user_notification_settings

Stores notification preferences per user and event type.

### 5.1. Columns

| Column | Type | Nullable | Default | Description |
|---|---:|---:|---:|---|
| `user_id` | UUID | No | - | User owner. Logical FK to Auth users. |
| `event_type` | VARCHAR(100) | No | - | Event type setting applies to. |
| `allow_push` | BOOLEAN | No | `true` | Whether push is allowed for this event type. |
| `allow_email` | BOOLEAN | No | `false` | Whether email is allowed for this event type. |
| `allow_in_app` | BOOLEAN | No | `true` | Whether in-app notification is allowed. |
| `created_at` | TIMESTAMPTZ | No | `now()` | Creation time. |
| `updated_at` | TIMESTAMPTZ | No | `now()` | Last update time. |

### 5.2. Constraints

```sql
ALTER TABLE user_notification_settings
  ADD CONSTRAINT pk_user_notification_settings
  PRIMARY KEY (user_id, event_type);
```

### 5.3. Indexes

```sql
CREATE INDEX idx_user_notification_settings_event_type
ON user_notification_settings(event_type);

CREATE INDEX idx_user_notification_settings_user_updated
ON user_notification_settings(user_id, updated_at DESC);
```

### 5.4. Business Rules

- Missing setting means use default channel policy for `event_type`.
- User can update only own settings.
- Critical security/system events may override disabled email/in-app setting only if explicitly defined in business spec/FR.
- `event_type` should use canonical `UPPER_SNAKE_CASE`.
- Updating settings should update `updated_at`.

## 6. Relationships

```text
notification_events 1 -- N user_notifications
users 1 -- N user_notifications       (logical FK to Auth Service)
users 1 -- N user_device_tokens       (logical FK to Auth Service)
users 1 -- N user_notification_settings (logical FK to Auth Service)
```

Important:

- Only `notification_events -> user_notifications` can be a physical FK because both tables are in Notification DB.
- All `users` references are logical. Do not create cross-database FK.

## 7. Idempotency Rules

Notification Service must be idempotent because producer outbox/broker delivery is at-least-once.

Required:

- `notification_events(source_service, source_event_id)` unique when `source_event_id` exists.
- `notification_events(source_service, event_key)` unique when `event_key` exists.
- `user_notifications(notification_event_id, user_id, type, reference_type, reference_id)` unique when `notification_event_id` exists.

Handler rules:

- Duplicate broker message should ack successfully and not create new row.
- Retried event should reuse existing `notification_events`.
- Retried event should not create duplicate user notification.
- Payment webhook duplicate from Commerce must not create duplicate `PAYMENT_SUCCESS` notification.
- System announcement fan-out must not create duplicate notification for same user.

## 8. Retry And Worker Processing

### 8.1. Event Processing Query Pattern

Recommended worker selection:

```sql
SELECT *
FROM notification_events
WHERE status IN ('PENDING', 'FAILED')
  AND retry_count < max_retry_count
ORDER BY created_at ASC
LIMIT :batchSize
FOR UPDATE SKIP LOCKED;
```

Then update selected rows:

```text
status = PROCESSING
locked_at = now()
locked_by = worker_id
```

### 8.2. Success Handling

On success:

```text
status = COMPLETED
processed_at = now()
locked_at = null
locked_by = null
last_error = null
```

### 8.3. Failure Handling

On retryable failure:

```text
status = FAILED
retry_count = retry_count + 1
last_error = sanitized_error
locked_at = null
locked_by = null
```

On permanent failure:

```text
status = FAILED
retry_count = max_retry_count
last_error = permanent_failure_reason
```

### 8.4. Stale Processing Recovery

If worker crashes while status is `PROCESSING`, recovery job can mark stale rows as `FAILED` when:

```text
status = PROCESSING
AND locked_at < now() - processing_timeout
```

## 9. Default Channel Policy Reference

Channel defaults are implemented in application/config layer, not as a table in MVP. Suggested defaults:

| Event Type | In-app | Push | Email |
|---|---:|---:|---:|
| `USER_CREATED` | true | false | true |
| `EMAIL_VERIFICATION_REQUESTED` | false | false | true |
| `PASSWORD_RESET_REQUESTED` | false | false | true |
| `PASSWORD_CHANGED` | true | true | true |
| `POST_LIKED` | true | true | false |
| `USER_FOLLOWED` | true | true | false |
| `COMMENT_CREATED` | true | true | false |
| `COMMENT_REPLIED` | true | true | false |
| `COMMENT_LIKED` | true | true | false |
| `ORDER_CREATED` | true | true | true |
| `PAYMENT_SUCCESS` | true | true | true |
| `PAYMENT_FAILED` | true | true | false |
| `SHIPMENT_CREATED` | true | false | false |
| `SHIPMENT_SHIPPED` | true | true | false |
| `SHIPMENT_DELIVERED` | true | true | false |
| `ORDER_COMPLETED` | true | true | false |
| `USER_SUSPENDED` | true | true | true |
| `USER_RESTRICTED` | true | true | true |
| `PRODUCT_REMOVED` | true | true | false |
| `REVIEW_HIDDEN` | true | false | false |
| `SHOP_SUSPENDED` | true | true | true |
| `SYSTEM_ANNOUNCEMENT_SENT` | true | optional | false |

If producer uses prefixed event types such as `COMMERCE_ORDER_CREATED`, application layer should map them to canonical handlers or store exact event type and route via alias table/config.

## 10. Security And Privacy Notes

- `device_token` is sensitive; never log full value.
- `payload`, `metadata`, `last_error` must not store password, token, OTP secret, provider credential or raw authorization header.
- Client response should return only metadata safe for that user.
- `reference_id` does not grant access to target resource; owner service still enforces authorization.
- Internal ingestion/retry endpoints, if any, require service authentication.

## 11. Suggested DDL Skeleton

This DDL is a reference skeleton. Actual migration may need project-specific UUID/default syntax.

```sql
CREATE TABLE notification_events (
  id UUID PRIMARY KEY,
  source_event_id UUID NULL,
  event_key VARCHAR(255) NULL,
  event_type VARCHAR(100) NOT NULL,
  source_service notification_source_service NOT NULL,
  aggregate_type VARCHAR(80) NULL,
  aggregate_id VARCHAR(100) NULL,
  actor_id UUID NULL,
  recipient_user_id UUID NULL,
  payload JSONB NOT NULL DEFAULT '{}'::jsonb,
  status notification_event_status NOT NULL DEFAULT 'PENDING',
  retry_count INTEGER NOT NULL DEFAULT 0,
  max_retry_count INTEGER NOT NULL DEFAULT 5,
  last_error TEXT NULL,
  locked_at TIMESTAMPTZ NULL,
  locked_by VARCHAR(100) NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  processed_at TIMESTAMPTZ NULL,
  CHECK (retry_count >= 0 AND max_retry_count >= 0 AND retry_count <= max_retry_count),
  CHECK ((status = 'COMPLETED' AND processed_at IS NOT NULL) OR (status <> 'COMPLETED'))
);

CREATE TABLE user_notifications (
  id UUID PRIMARY KEY,
  notification_event_id UUID NULL REFERENCES notification_events(id) ON DELETE SET NULL,
  user_id UUID NOT NULL,
  actor_id UUID NULL,
  type VARCHAR(100) NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  reference_type VARCHAR(80) NULL,
  reference_id VARCHAR(100) NULL,
  is_read BOOLEAN NOT NULL DEFAULT false,
  is_deleted BOOLEAN NOT NULL DEFAULT false,
  metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
  delivery_status notification_delivery_status NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  read_at TIMESTAMPTZ NULL,
  CHECK ((is_read = false AND read_at IS NULL) OR (is_read = true))
);

CREATE TABLE user_device_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  device_type device_type NOT NULL,
  device_token VARCHAR(512) NOT NULL UNIQUE,
  is_active BOOLEAN NOT NULL DEFAULT true,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_used_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_notification_settings (
  user_id UUID NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  allow_push BOOLEAN NOT NULL DEFAULT true,
  allow_email BOOLEAN NOT NULL DEFAULT false,
  allow_in_app BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, event_type)
);
```