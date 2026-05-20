# Notification Service Business Specification (MVP)

Notification Service la service so huu toan bo nghiep vu notification trong 2Hands: consume domain events tu Auth/Social/Commerce/Admin, tao in-app notification, gui push notification, gui email notification co ban, quan ly device token, quan ly notification settings va retry cac notification bi loi. Tai lieu nay la source-of-truth de AI/engineer co the doc va hieu boundary, event contract, channel routing, idempotency va business rules truoc khi code.

## 1. Service Ownership

Notification Service own cac aggregate va data sau:

- Notification ingestion queue: `notification_events`.
- User-facing in-app notifications: `user_notifications`.
- User push device tokens: `user_device_tokens`.
- Per-user/per-event notification preferences: `user_notification_settings`.
- Delivery orchestration cho in-app, push va email trong MVP.
- Retry logic cho failed event processing va failed delivery.

Notification Service khong own:

- User credential, user status, email verification token, password reset token: Auth Service own.
- Post/comment/follow/social graph source-of-truth: Social Service own.
- Order/payment/shipment/product/shop/review source-of-truth: Commerce Service own.
- Admin enforcement, moderation, system config, announcement source-of-truth: Admin Service own.

Notification Service khong duoc truy cap truc tiep database cua service khac. Neu can thong tin bo sung, service phai lay tu event payload, JWT claim, local projection sau nay, hoac internal API theo contract ro rang. MVP nen uu tien event payload du du lieu de render notification ma khong can synchronous lookup.

## 2. Actors

### User

Nguoi dung nhan notification. User co the:

- Xem danh sach notification.
- Xem notification chua doc.
- Xem unread count.
- Mark notification da doc.
- Mark all notification da doc.
- Soft delete/hide notification.
- Quan ly notification settings.
- Dang ky/revoke device token khi login/logout.

### Auth Service

Auth Service publish event lien quan identity/security:

- `USER_CREATED`
- `EMAIL_VERIFICATION_REQUESTED`
- `PASSWORD_RESET_REQUESTED`
- `PASSWORD_CHANGED`

Notification Service consume cac event nay de tao welcome/security notification va gui email critical.

### Social Service

Social Service publish event lien quan tuong tac social:

- `POST_LIKED`
- `USER_FOLLOWED`
- `COMMENT_CREATED`
- `COMMENT_REPLIED`
- `COMMENT_LIKED`

Notification Service consume de notify chu post/comment/user duoc follow.

### Commerce Service

Commerce Service publish event lien quan order/payment/shipment:

- `ORDER_CREATED`
- `PAYMENT_SUCCESS`
- `PAYMENT_FAILED`
- `SHIPMENT_CREATED`
- `SHIPMENT_SHIPPED`
- `SHIPMENT_DELIVERED`
- `ORDER_COMPLETED`

Neu Commerce spec su dung prefix `COMMERCE_*`, Notification Service can mapping ve canonical event type hoac chap nhan alias co contract ro rang.

### Admin Service

Admin Service publish event lien quan enforcement/moderation/announcement:

- `USER_SUSPENDED`
- `USER_RESTRICTED`
- `PRODUCT_REMOVED`
- `REVIEW_HIDDEN`
- `SHOP_SUSPENDED`
- `SYSTEM_ANNOUNCEMENT_SENT` hoac `SYSTEM_ANNOUNCEMENT_PUBLISHED`

Notification Service consume de gui thong bao enforcement, moderation va fan-out announcement.

### System

Background workers/jobs trong Notification Service:

- Consume broker event vao `notification_events`.
- Process pending notification events.
- Retry failed notification events.
- Retry failed push/email delivery.
- Cleanup invalid device tokens.
- Optional cleanup old notifications.
- Optional review reminder notification.

### External Providers

- FCM: push notification provider chinh trong MVP.
- Email provider: SMTP/SendGrid/Mailgun hoac provider tuong duong, chi dung cho email critical/system trong MVP.

## 3. Core Domain Concepts

### Notification Event

Notification Event la internal processing record trong Notification Service, duoc tao tu domain event cua service khac. No khong phai source-of-truth cua nghiep vu goc; no la queue noi bo de dam bao retry va traceability.

Processing status:

```text
PENDING -> PROCESSING -> COMPLETED
PENDING -> PROCESSING -> FAILED
FAILED -> PROCESSING -> COMPLETED
FAILED -> PROCESSING -> FAILED
PROCESSING -> FAILED when timeout/recovery marks stale processing
```

Meaning:

- `PENDING`: event da duoc ingest nhung chua xu ly.
- `PROCESSING`: worker dang xu ly event.
- `COMPLETED`: da route xong theo channel can thiet, da tao records delivery can thiet.
- `FAILED`: xu ly loi va co the retry neu chua vuot max retry.

`notification_events` phai co idempotency key du de khong process duplicate event trong at-least-once delivery. Recommended unique key:

```text
(source_service, source_event_id)
```

Neu producer chua gui `source_event_id`, tam thoi dung `event_key` tu producer hoac hash deterministic cua `(source_service, event_type, aggregate_id, occurred_at)`, nhung cach hash chi la fallback va can tranh neu co the.

### User Notification

User Notification la notification hien thi cho user trong app/web. Record nay phai gan voi:

- `user_id`: recipient.
- `actor_id`: user gay ra action, nullable cho system/admin/provider event.
- `notification_event_id`: event da tao notification, nullable cho manual/local notification neu sau nay co.
- `type`: UI/business type.
- `reference_type` va `reference_id`: deep link den resource lien quan.
- `metadata`: payload phu tro da sanitize.

In-app lifecycle:

```text
CREATED -> READ
CREATED/READ -> DELETED
```

Schema dung boolean:

- `is_read = false`, `read_at = null`: unread.
- `is_read = true`, `read_at != null`: read.
- `is_deleted = true`: user da hide/soft delete, khong hien trong list mac dinh.

### Notification Channel

MVP co 3 channel:

- `IN_APP`: persisted trong `user_notifications`, channel chinh cua MVP.
- `PUSH`: gui qua FCM toi active device token.
- `EMAIL`: gui email cho critical/system event.

Mot event co the route toi nhieu channel. Vi du:

- `POST_LIKED`: in-app + push, khong email.
- `PAYMENT_SUCCESS`: in-app + push + email.
- `PASSWORD_RESET_REQUESTED`: email, co the khong tao in-app.
- `SYSTEM_ANNOUNCEMENT_SENT`: in-app fan-out, optional push neu critical.

### Delivery Status

MVP luu `delivery_status` tren `user_notifications` de phan anh trang thai delivery tong quat cua notification:

```text
PENDING -> SENT
PENDING -> FAILED
FAILED -> SENT after retry succeeds
```

Meaning:

- `PENDING`: record da tao, channel delivery chua hoan tat.
- `SENT`: in-app da san sang hien thi va cac delivery bat buoc da thanh cong theo policy.
- `FAILED`: delivery bat buoc bi loi va can retry/inspect.

Neu sau MVP can theo doi tung channel chi tiet, co the them table `notification_deliveries`, nhung MVP giu schema don gian.

### Device Token

Device token la token push cua user theo device:

- `IOS`
- `ANDROID`
- `WEB`

`device_token` unique toan he thong. Khi user login tren device moi, app register token. Khi logout, token duoc deactivate. Khi FCM bao invalid token, service set `is_active = false`.

### Notification Settings

Notification settings la preference cua user theo `event_type`:

- `allow_push`
- `allow_email`
- `allow_in_app`

Neu user chua co setting cho event type, system dung default policy cua event type. Default policy nen duoc define trong code/config va document trong spec/FR, khong suy luan random tai runtime.

## 4. Canonical Event Contract

Tat ca event tu producer nen co envelope toi thieu:

```json
{
  "event_id": "uuid-from-producer-outbox",
  "event_type": "PAYMENT_SUCCESS",
  "source_service": "COMMERCE",
  "event_key": "commerce.payment.payment-id.success",
  "aggregate_type": "PAYMENT",
  "aggregate_id": "payment-id",
  "actor_id": "optional-user-id",
  "recipient_user_ids": ["user-id"],
  "occurred_at": "2026-05-20T16:00:00Z",
  "payload": {}
}
```

Rules:

- `event_id` cua producer la idempotency key chinh.
- `event_type` la `UPPER_SNAKE_CASE`.
- `source_service` phai nam trong `AUTH`, `SOCIAL`, `COMMERCE`, `ADMIN`, `SYSTEM`.
- `recipient_user_ids` nen co san trong payload neu producer biet recipient.
- Payload chi chua data can render notification, khong chua password, token, OTP secret, payment secret, raw provider credential.
- Notification Service co the reject/mark failed event neu thieu recipient cho event bat buoc co recipient.

## 5. Notification Event Ingestion

Flow ingestion:

1. Producer service ghi outbox event trong transaction nghiep vu.
2. Producer outbox worker publish event len broker.
3. Notification consumer nhan broker message.
4. Notification Service validate envelope.
5. Notification Service insert `notification_events` voi `status = PENDING`.
6. Neu duplicate `(source_service, source_event_id)`, service ack broker message va khong insert duplicate.
7. Processing worker lay event pending de route theo event type.

Business rules:

- Ingestion phai idempotent.
- Broker at-least-once delivery khong duoc tao duplicate notification.
- Raw broker message co the luu trong `payload`, nhung phai sanitize truoc khi log.
- Event khong support co the mark `FAILED` voi `last_error = UNSUPPORTED_EVENT_TYPE` hoac ignore theo allowlist policy.
- Khong block producer service bang synchronous notification delivery.

## 6. Event Processing And Routing

Processing worker:

1. Lock batch `notification_events` co status `PENDING` hoac retryable `FAILED`.
2. Set status `PROCESSING`.
3. Resolve handler theo `event_type`.
4. Resolve recipients tu event payload.
5. Apply skip rules va settings.
6. Create `user_notifications` neu `allow_in_app = true`.
7. Send push neu `allow_push = true` va user co active device token.
8. Send email neu `allow_email = true` va event policy cho phep email.
9. Update delivery status.
10. Mark event `COMPLETED` neu xu ly xong.
11. Neu loi, increment `retry_count`, set `last_error`, mark `FAILED`.

Channel routing policy can duoc define theo event type. Vi du:

| Event Type | In-app | Push | Email |
|---|---:|---:|---:|
| `POST_LIKED` | yes | yes | no |
| `USER_FOLLOWED` | yes | yes | no |
| `COMMENT_CREATED` | yes | yes | no |
| `ORDER_CREATED` | yes | yes | yes |
| `PAYMENT_SUCCESS` | yes | yes | yes |
| `PAYMENT_FAILED` | yes | yes | no |
| `SHIPMENT_SHIPPED` | yes | yes | no |
| `SHIPMENT_DELIVERED` | yes | yes | no |
| `EMAIL_VERIFICATION_REQUESTED` | no | no | yes |
| `PASSWORD_RESET_REQUESTED` | no | no | yes |
| `USER_SUSPENDED` | yes | yes | yes |
| `SYSTEM_ANNOUNCEMENT_SENT` | yes | optional | no |

## 7. In-App Notification

In-app notification la channel chinh cua MVP.

### Create Notification

Service tao `user_notifications` khi:

- Event type co policy `allow_in_app_by_default = true`.
- User setting cho event type khong tat `allow_in_app`.
- Recipient hop le.
- Event khong bi skip do self notification.

Title/content phai duoc tao tu template server-side theo event type. Payload producer chi cung cap data, khong duoc tin tuong raw title/content neu producer khong phai trusted contract.

### View Notification List

User xem danh sach notification cua minh:

- Filter `user_id = current_user_id`.
- Filter `is_deleted = false`.
- Sort newest first.
- Pagination bat buoc.

User khong duoc xem notification cua user khac.

### View Unread Notifications

Unread notification:

```text
is_read = false
AND is_deleted = false
```

### Unread Count

Unread count dung cho badge. Query phai toi uu bang index `(user_id, is_read, is_deleted, created_at)`.

### Mark Notification As Read

Rules:

- Chi owner duoc mark read.
- Neu da read, operation idempotent.
- Khi mark read:

```text
is_read = true
read_at = now()
```

### Mark All Notifications As Read

Rules:

- Chi update notifications cua current user.
- Chi update `is_read = false`.
- Nen update theo batch neu user co qua nhieu records.

### Soft Delete Notification

Rules:

- DELETE mac dinh la soft delete.
- Set `is_deleted = true`.
- Khong hard delete trong MVP.
- Deleted notification khong hien trong default list va khong tinh unread count.

## 8. Push Notification

Push notification dung FCM trong MVP.

### Register Device Token

Khi app login hoac refresh token:

1. Client gui `device_type`, `device_token`.
2. Service validate JWT.
3. Upsert theo `device_token`.
4. Set `user_id = current_user_id`, `is_active = true`, update `last_used_at`, `updated_at`.

Rules:

- `device_token` unique.
- Neu token cu dang gan user khac, latest authenticated user wins only if security policy accepts; otherwise reject and require token rotation.
- Khong log full device token.

### Revoke Device Token

Khi logout:

- Set `is_active = false`.
- Update `updated_at`.

Rules:

- User chi revoke token cua minh.
- Logout all devices co the deactivate tat ca active tokens cua user.

### Send Push Notification

Push duoc gui neu:

- Event policy cho phep push.
- User setting `allow_push = true` hoac default cho phep.
- User co active device token.
- Recipient khong bi skip.

Neu user khong co active device token, event van co the completed neu in-app/email da xu ly theo policy. Khong co device token khong phai system error.

### Invalid Token Cleanup

Neu FCM tra invalid/unregistered token:

- Set token `is_active = false`.
- Update `last_used_at`/`updated_at` neu can.
- Khong retry token invalid.

## 9. Email Notification

Email trong MVP chi dung cho critical/system events:

- Verify email.
- Password reset.
- Password changed/security alert.
- Order confirmation.
- Payment success.
- Account suspended/restricted.

Rules:

- Khong lam marketing/campaign email trong MVP.
- Email template co the hard-code/server-side trong MVP, chua can template builder.
- Khong log OTP, reset token, email provider secret.
- Email verification/password reset payload phai chua token/link da duoc Auth Service tao; Notification Service chi deliver, khong generate credential token.
- Neu email fail, event co the retry theo policy.

## 10. Notification Settings

User co the bat/tat notification theo event type:

```text
(user_id, event_type) unique
```

Use cases:

- View notification settings.
- Update notification settings.
- Initialize default settings khi user created hoac khi user lan dau mo settings page.

Rules:

- User chi cap nhat settings cua chinh minh.
- Missing setting dung default policy.
- Critical security email co the override `allow_email = false` neu policy bat buoc de bao ve tai khoan. Neu override, spec/FR phai noi ro.
- `allow_in_app = false` nghia la khong tao in-app notification cho event type do, tru critical system announcement bat buoc neu policy quy dinh.

Example:

| Event Type | Push | Email | In-app |
|---|---:|---:|---:|
| `POST_LIKED` | true | false | true |
| `PAYMENT_SUCCESS` | true | true | true |
| `PASSWORD_RESET_REQUESTED` | false | true | false |
| `SYSTEM_ANNOUNCEMENT_SENT` | true | false | true |

## 11. Social Notification Flows

### Post Liked

Social publishes `POST_LIKED`.

Recipient:

- Post author.

Rules:

- Neu `actor_id = post_author_id`, skip self notification.
- Create notification: "A da thich bai viet cua ban".
- Reference: `POST`, `post_id`.
- Default channels: in-app + push.

### User Followed

Social publishes `USER_FOLLOWED`.

Recipient:

- Followed user.

Rules:

- Skip self follow if event accidentally emitted.
- Reference: `USER`, `actor_id`.
- Default channels: in-app + push.

### Comment Created

Social publishes `COMMENT_CREATED`.

Recipient:

- Post author, neu commenter khong phai post author.

Rules:

- Reference: `POST` or `COMMENT`.
- Default channels: in-app + push.

### Comment Replied

Social publishes `COMMENT_REPLIED`.

Recipient:

- Parent comment author.

Rules:

- Skip self reply.
- Reference: `COMMENT`.
- Default channels: in-app + push.

### Comment Liked

Social publishes `COMMENT_LIKED`.

Recipient:

- Comment author.

Rules:

- Skip self-like.
- Reference: `COMMENT`.
- Default channels: in-app + push.

## 12. Commerce Notification Flows

### Order Created

Commerce publishes `ORDER_CREATED`.

Recipients:

- Buyer: "Don hang cua ban da duoc tao".
- Seller(s): "Ban co don hang moi".

Rules:

- One source event can create multiple `user_notifications`.
- Reference: `ORDER`, `order_id`.
- Default channels: buyer in-app + push + email; seller in-app + push.

### Payment Success

Commerce publishes `PAYMENT_SUCCESS`.

Recipient:

- Buyer.

Rules:

- Reference: `PAYMENT` or `ORDER`.
- Default channels: in-app + push + email.
- Duplicate payment webhook must not send duplicate notification.

### Payment Failed

Commerce publishes `PAYMENT_FAILED`.

Recipient:

- Buyer.

Rules:

- Reference: `PAYMENT` or `ORDER`.
- Default channels: in-app + push.

### Shipment Created

Commerce publishes `SHIPMENT_CREATED`.

Recipients:

- Buyer and optionally seller depending use case.

Rules:

- Reference: `SHIPMENT`.
- Default channels: in-app.

### Shipment Shipped

Commerce publishes `SHIPMENT_SHIPPED`.

Recipient:

- Buyer.

Rules:

- Message: order is being delivered.
- Reference: `SHIPMENT`.
- Default channels: in-app + push.

### Shipment Delivered

Commerce publishes `SHIPMENT_DELIVERED`.

Recipient:

- Buyer.

Rules:

- Message: order delivered, ask buyer to confirm/review if relevant.
- Default channels: in-app + push.

### Order Completed

Commerce publishes `ORDER_COMPLETED`.

Recipient:

- Buyer.

Rules:

- Message: order completed successfully.
- Optional review prompt can be separate event/job.

### Review Reminder Optional

MVP optional:

- Background job identifies delivered/completed order without review after X days.
- Sends reminder notification.
- Should avoid spam by storing reminder marker or using deterministic idempotency key.

## 13. Admin Notification Flows

### User Suspended

Admin publishes `USER_SUSPENDED`.

Recipient:

- Target user.

Rules:

- Message explains account suspended and support path if available.
- Default channels: in-app + push + email.
- Suspended user can still receive critical system/account notification.

### User Restricted

Admin publishes `USER_RESTRICTED`.

Recipient:

- Target user.

Rules:

- Message explains restricted capabilities at high level.
- Default channels: in-app + push + email.

### Product Removed

Admin publishes `PRODUCT_REMOVED`.

Recipient:

- Seller/product owner.

Rules:

- Reference: `PRODUCT`.
- Message should include reason if safe and policy allows.
- Default channels: in-app + push.

### Review Hidden

Admin publishes `REVIEW_HIDDEN`.

Recipient:

- Review author, optional seller depending policy.

Rules:

- MVP optional.
- Default channels: in-app.

### Shop Suspended

Admin publishes `SHOP_SUSPENDED`.

Recipient:

- Shop owner.

Rules:

- Reference: `SHOP`.
- Default channels: in-app + push + email.

## 14. System Announcement Fan-Out

Admin Service owns announcement creation/publish state. Notification Service only receives publish event and fan-out notifications.

Flow:

1. Admin publishes announcement in Admin Service.
2. Admin outbox publishes `SYSTEM_ANNOUNCEMENT_SENT` or `SYSTEM_ANNOUNCEMENT_PUBLISHED`.
3. Notification Service ingests event.
4. Notification Service resolves recipients.
5. Notification Service creates `user_notifications`.
6. If `is_pinned = true`, metadata marks pinned.
7. If `dismissible = true`, users can soft delete/dismiss notification.

MVP recipient strategy:

- For all-users broadcast, fan-out in batches.
- Avoid loading all users into memory.
- If Auth Service owns user list, Notification needs internal API/page stream or a future projection.
- For MVP documentation, require event payload to specify target audience or recipient ids if broadcast infra is not implemented yet.

Rules:

- Announcement fan-out must be idempotent.
- One announcement should not create duplicate notification for same user.
- Critical announcement may bypass user settings if business policy requires.
- Dismiss action maps to `is_deleted = true` for that user's notification.

## 15. Delivery Rules And Invariants

### Respect Notification Settings

Before creating/sending notification:

1. Load user setting by `(user_id, event_type)`.
2. If missing, use default policy.
3. Apply channel flags.
4. Apply critical override if event type requires.

### Skip Self Notification

Skip notification if event is caused by same user who would receive it:

```text
actor_id == recipient_user_id
```

Applies to:

- `POST_LIKED`
- `COMMENT_LIKED`
- `COMMENT_CREATED`
- `COMMENT_REPLIED`
- `USER_FOLLOWED`

Does not apply to:

- `PAYMENT_SUCCESS`
- `ORDER_CREATED`
- `USER_SUSPENDED`
- System/security events.

### Deleted User

If Auth says user is deleted:

- Do not send normal notification.
- Do not create new in-app notification unless required for compliance.

MVP should avoid synchronous Auth lookup per event. Producer should avoid sending recipient deleted users, or Notification can maintain future user status projection.

### Suspended User

Suspended user can still receive:

- Account enforcement notification.
- Security notification.
- System announcement if policy allows.

Suspended user may not receive social/commerce marketing-like notifications depending policy.

### Notification Content Safety

Notification title/content must:

- Avoid exposing private content beyond recipient authorization.
- Avoid raw untrusted HTML.
- Avoid secrets/tokens/OTP except email body when specifically required and safe.
- Be generated from trusted templates and sanitized payload.

## 16. Reliability And Idempotency

Notification Service consumes at-least-once events. Therefore every handler must be idempotent.

Required invariants:

- Same `(source_service, source_event_id)` creates at most one `notification_events`.
- Same `(notification_event_id, user_id, type, reference_type, reference_id)` should not create duplicate `user_notifications`.
- Retrying `PAYMENT_SUCCESS` must not send duplicate user-visible notification.
- Retrying push/email may call provider more than once only if provider idempotency is unavailable; user-visible in-app record must stay unique.
- Worker crash during `PROCESSING` must be recoverable by timeout policy.

Recommended processing safeguards:

- Row-level lock batch with `FOR UPDATE SKIP LOCKED` if database supports.
- `locked_at` and `locked_by` for worker recovery if implemented.
- Max retry count with exponential backoff.
- `last_error` truncated/sanitized.
- `processed_at` set only when completed.

## 17. Background Jobs

### Retry Failed Notification Events

Retry records:

```text
notification_events.status = FAILED
AND retry_count < max_retry_count
```

Rules:

- Apply backoff.
- Do not retry non-retryable validation errors unless payload/handler changed.
- Keep event id stable.

### Retry Failed Push Notifications

Retry push delivery for notifications with retryable provider error.

Rules:

- Invalid/unregistered token is not retryable.
- Provider timeout/rate-limit is retryable.
- Deactivate invalid token.

### Retry Failed Email Notifications

Retry email for transient provider failure.

Rules:

- Invalid recipient email can be permanent failure.
- Provider timeout/rate-limit is retryable.

### Cleanup Invalid Device Tokens

Deactivate tokens known invalid or stale.

Rules:

- Do not hard delete tokens in MVP unless retention policy says so.
- Keep updated_at for audit/debug.

### Cleanup Old Notifications Optional

MVP optional:

- Soft/hard delete notifications older than configured retention, e.g. 180 days.
- Must not delete audit-critical system notifications unless policy allows.

## 18. API Surface MVP

Base URL:

```text
https://api.2hands.vn/notification/api/v1
```

User APIs:

- `GET /notifications`
- `GET /notifications/unread`
- `GET /notifications/unread-count`
- `PATCH /notifications/{notificationId}/read`
- `PATCH /notifications/read-all`
- `DELETE /notifications/{notificationId}`
- `GET /notification-settings`
- `PUT /notification-settings/{eventType}`
- `POST /device-tokens`
- `DELETE /device-tokens/{deviceToken}`

Internal/system APIs may be avoided if broker consumption is used. If implemented for testing/admin:

- `POST /internal/events`
- `POST /internal/events/{eventId}/retry`

All user APIs require JWT. Internal APIs require service-to-service authentication, not user JWT only.

## 19. Database Invariants

- `notification_events.source_service + source_event_id` unique when `source_event_id` is present.
- `user_notifications.notification_event_id` references `notification_events.id`.
- `user_notifications.user_id` is logical FK to Auth users.
- `user_device_tokens.device_token` unique.
- `user_notification_settings(user_id, event_type)` unique.
- `read_at` must be null when `is_read = false`.
- `read_at` should be non-null when `is_read = true`.
- `is_deleted = true` hides notification from default list.
- `delivery_status = SENT` should mean in-app record is ready and required delivery policy completed.

## 20. Security And Privacy

Security rules:

- Protected APIs require JWT.
- User can access only own notifications, device tokens and settings.
- Device token is sensitive; never log full token.
- Email verification/reset token must not be logged.
- Raw provider credentials and authorization headers must not be persisted in logs.
- `metadata` and `payload` must be sanitized before returning to client.
- Internal event ingestion endpoint, if any, must require service credential/signature.

Privacy rules:

- Notification content should not expose private post/comment/order details to unauthorized user.
- Use `reference_id` for deep link; client still must authorize resource access in owner service.
- Deleted notification is hidden, not removed from database in MVP.

## 21. Observability

Minimum logs/metrics:

- Event consumed count by `source_service`, `event_type`.
- Event processing success/failure count.
- Retry count and permanent failure count.
- Push send success/failure count.
- Email send success/failure count.
- Invalid device token count.
- Processing latency from `created_at` to `processed_at`.

Logging rules:

- INFO for normal processing summary.
- WARN for retryable provider failures.
- ERROR for permanent failures and unexpected exceptions.
- DEBUG only for sanitized payload summaries in development.

## 22. MVP Out Of Scope

Khong lam trong MVP:

- Realtime websocket cluster.
- Kafka-specific advanced topology neu project chua chon Kafka.
- Marketing/campaign notification system.
- Notification analytics product.
- Scheduled notification campaign.
- Email template builder.
- Multi-language template engine.
- Batch/grouped notification UI.
- Notification priority engine.
- Complex distributed queue/dead-letter infrastructure.
- Per-channel delivery table unless MVP retry/debug khong du voi `delivery_status`.

