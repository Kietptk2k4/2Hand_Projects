# Admin Service Business Specification (MVP)

Admin Service la service dieu phoi quan tri, enforcement, moderation, system configuration, announcement, audit va support-read trong he thong 2Hands. Tai lieu nay la source-of-truth de AI/engineer co the doc va hieu boundary, permission model, business rules, event contract va cac luong cross-service truoc khi code.

## 1. Service Ownership

Admin Service own cac aggregate va data sau:

- System configs va config history.
- System announcements.
- Admin action logs.
- User enforcements va enforcement logs.
- Content moderation logs.
- Admin outbox events.

Admin Service khong own:

- User credential/session/role source-of-truth: Auth Service own.
- Product/shop/review/order/payment/shipment source-of-truth: Commerce Service own.
- Post/comment/follow/social action source-of-truth: Social Service own.
- Notification delivery source-of-truth: Notification Service own.

Admin Service thuc hien moderation/enforcement bang cach:

- Goi internal API cua service owner khi can synchronous result.
- Publish domain events qua Outbox Pattern de service owner consume va apply state eventually.
- Ghi audit log cho admin action trong Admin DB.

## 2. Actors

### Admin / Moderator

Admin hoac moderator co role/permission tu Auth Service. Co the:

- Suspend/ban/restrict user.
- Investigate user profile, login history, active sessions, OAuth accounts va roles.
- Moderate product, review, shop, post, comment.
- Quan ly system announcement.
- Quan ly system config.
- Xem support data ve order/payment/shipment/webhook.
- Xem audit logs neu co permission.

### Support

Support admin co quyen doc thong tin de dieu tra:

- User profile/session/login history.
- Order/payment/shipment detail.
- Webhook logs va status history.

Support khong mac dinh duoc thuc hien destructive action nhu suspend user/remove product/update config.

### Super Admin

Super Admin co quyen cao nhat:

- Cap nhat system config.
- Thuc hien critical moderation.
- Xem audit logs.
- Revoke admin sessions neu Auth Service support.

### System

Background worker/job:

- Expire user enforcements.
- Publish/retry outbox events.
- Optional cleanup/retention audit logs sau MVP.

## 3. Admin Authentication And Authorization

Admin authentication dung Auth Service, khong duplicate credential trong Admin Service.

Admin login/session capabilities:

- Admin login.
- Refresh admin token.
- Admin logout.
- Revoke admin session.

Admin authorization:

- Check admin role.
- Check admin permission.
- Authorize admin API before executing use case.

Example roles:

- `MODERATOR`
- `SUPPORT`
- `SUPER_ADMIN`

Example permissions:

- `PRODUCT_REMOVE`
- `USER_SUSPEND`
- `USER_RESTRICT`
- `REVIEW_HIDE`
- `SHOP_SUSPEND`
- `POST_MODERATE`
- `COMMENT_MODERATE`
- `SYSTEM_CONFIG_UPDATE`
- `SYSTEM_ANNOUNCEMENT_PUBLISH`
- `ADMIN_AUDIT_READ`
- `ORDER_SUPPORT_READ`

Security rules:

- Protected admin APIs require JWT.
- Permission check happens in delivery/application boundary before domain action.
- Admin id comes from JWT, never from request body.
- Do not log password, token, OTP, secret, provider credential.

## 4. User Enforcement

User Enforcement la moderation action len user account/platform capability. Admin Service stores enforcement decision; Auth/Social/Commerce apply effects through API/event integration.

### Enforcement Types

`SUSPEND`:

- Intended effect in Auth Service: `auth.users.status = SUSPENDED`.
- Auth should revoke refresh token sessions.
- User cannot login.

`BAN`:

- MVP can model as `SUSPEND` effect plus `USER_ENFORCEMENTS.action_type = BAN`.
- Auth Service may not need separate `BAN` status in MVP.

`RESTRICT`:

- User can login.
- User can buy products.
- User cannot create post, comment, review, create product, or other restricted actions depending policy.
- Social/Commerce must check enforcement event/cache before protected write actions.

### Enforcement Status

```text
ACTIVE -> REVOKED
ACTIVE -> EXPIRED
```

Meaning:

- `ACTIVE`: enforcement currently applies.
- `REVOKED`: admin manually revoked enforcement.
- `EXPIRED`: system expired enforcement when `expires_at < now`.

### Create Enforcement Flow

1. Admin selects user and action type.
2. System checks permission.
3. System validates target user exists through Auth Service/read model.
4. System creates `user_enforcements`.
5. System creates `user_enforcement_logs`.
6. System writes `admin_action_logs`.
7. System writes outbox event.
8. Auth/Social/Commerce consume event or Admin calls owner service API.

Business rules:

- Active enforcement for same user/action should not conflict unless policy allows stacking.
- Permanent enforcement has `expires_at = null`.
- Temporary enforcement has future `expires_at`.
- Every enforcement state transition must write `USER_ENFORCEMENT_LOGS`.

## 5. User Investigation

User investigation is read-oriented support/admin capability.

Admin can view:

- User profile.
- Login history.
- Active sessions.
- OAuth accounts.
- User roles.
- Current enforcement status.
- Enforcement history.

Use cases:

- Detect spam account.
- Detect suspicious login.
- Investigate abuse report.
- Decide whether to suspend/restrict/ban.

Boundary:

- Auth Service owns profile, sessions, login history, OAuth accounts and roles.
- Admin Service can call Auth internal APIs or consume projections if later introduced.
- Investigation read must be audited when sensitive enough.

## 6. Product Moderation

Product moderation targets Commerce Service product.

Actions:

- Remove product.
- Restore product.
- View moderation history.

Remove product effect:

- Admin writes `CONTENT_MODERATION_LOGS`.
- Admin writes `ADMIN_ACTION_LOGS`.
- Admin publishes `PRODUCT_REMOVED`.
- Commerce Service sets `products.status = REMOVED`.
- Commerce cart behavior: related active `cart_items.status = INVALID_PRODUCT`.
- Product no longer visible, searchable, add-cartable, or checkoutable.

Restore product effect:

- Admin publishes `PRODUCT_RESTORED` or calls Commerce restore API.
- Commerce validates whether product can return to previous valid status.
- Restore must not bypass Commerce product readiness rules.

## 7. Review Moderation

Review moderation targets Commerce review.

Actions:

- Hide review.
- Restore review.
- Remove review as soft delete/status action.

Hide review effect:

- Admin writes moderation log.
- Admin publishes `REVIEW_HIDDEN`.
- Commerce sets `reviews.status = HIDDEN`.
- Review not public-visible.
- Review remains in DB for audit/support.

Restore review effect:

- Publish `REVIEW_RESTORED`.
- Commerce sets review visible if allowed.
- Rating summary may be recalculated by Commerce.

## 8. Social Content Moderation

Social content moderation targets Social Service post/comment.

Post actions:

- Hide post.
- Remove post.
- Restore post.

Comment actions:

- Hide comment.
- Remove comment.
- Restore comment.

Boundary:

- Social Service owns post/comment state.
- Admin Service stores moderation logs and publishes/calls moderation command.
- Social Service applies status changes and owns visibility/feed behavior.

## 9. Shop Moderation

Shop moderation targets Commerce seller shop.

Actions:

- Suspend shop.
- Reopen shop.
- Close shop.

Suspend shop effect:

- Admin publishes `SHOP_SUSPENDED`.
- Commerce sets `seller_shops.status = SUSPENDED`.
- Shop cannot publish new products.
- Products can be hidden from marketplace according Commerce policy.
- Existing orders should still be processed/supportable in MVP unless explicit support action exists.

Close shop effect:

- Commerce sets `seller_shops.status = CLOSED`.
- New checkout is blocked.
- Existing order snapshots remain unchanged.

Reopen shop effect:

- Commerce sets shop `ACTIVE` if policy allows.
- Removed/archived products are not automatically republished.

## 10. System Announcement

System announcements are platform-wide messages for users/admins.

Use cases:

- Maintenance.
- Shipping delay.
- Payment outage.
- Policy update.
- Critical incident.

Announcement status:

```text
DRAFT -> SENT
DRAFT -> CANCELLED
SENT -> CANCELLED
```

Fields:

- `severity`: `INFO`, `WARNING`, `CRITICAL`.
- `is_pinned`: show prominently.
- `dismissible`: user can dismiss if frontend/notification service supports.

Business rules:

- Only authorized admins can publish/cancel.
- Publishing sets `sent_at`.
- Critical announcements should be logged in `ADMIN_ACTION_LOGS`.
- Notification Service can consume announcement events for fan-out.

## 11. System Configuration Management

System configs are runtime config values used by platform/services.

Examples:

- `MAX_CART_ITEMS`
- `PAYMENT_EXPIRE_MINUTES`
- `AUTO_COMPLETE_ORDER_DAYS`
- `ALLOW_NEW_SELLER`
- `MAX_IMAGES_PER_PRODUCT`

Config value types:

- `INTEGER`
- `DECIMAL`
- `STRING`
- `BOOLEAN`
- `JSON`

Business rules:

- `config_key` unique.
- Config update must validate value according to `value_type`.
- Every create/update/toggle must write `SYSTEM_CONFIG_HISTORY`.
- Every create/update/toggle must write `ADMIN_ACTION_LOGS`.
- Config updates should publish `SYSTEM_CONFIG_UPDATED`.
- Disabled configs keep history; consumers decide fallback behavior.

## 12. Admin Audit Logging

Admin action logging is mandatory for moderation/enforcement/config/support critical actions.

Log fields:

- `admin_id`
- `action_type`
- `target_type`
- `target_id`
- `request_payload`
- `response_payload`
- `ip_address`
- `user_agent`
- `created_at`

Critical payload logging:

- Store request/response payload only for critical actions:
  - refund/manual operation if introduced later.
  - config update.
  - destructive moderation.
  - enforcement action.

Rules:

- Redact secrets/tokens/passwords.
- Payload should be minimal and audit-safe.
- Read-only support views can be logged without full response payload unless policy requires.

## 13. Order / Payment / Shipment Support MVP-lite

Admin Service does not execute real refund/dispute/payout reversal in MVP.

Support can view:

- Order detail.
- Payment status.
- Shipment status.
- Webhook logs.
- Order status history.
- Payment status history.
- Shipment status history.

Boundary:

- Commerce Service owns order/payment/shipment data.
- Admin Service calls Commerce support APIs or reads projected support views if later added.
- Support read should be permission-controlled and optionally audit-logged.

## 14. Event And Cross-Service Integration

Admin Service publishes:

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

Integration rules:

- Auth applies account/session status for suspend/ban.
- Social applies restricted behavior for post/comment/follow and content moderation.
- Commerce applies product/shop/review moderation and restricted commerce writes.
- Notification can fan out announcement/enforcement notices.

All publish operations must go through `outbox_events`.

## 15. Background Jobs

### Enforcement Expiration Job

Job finds active enforcements where:

```text
status = ACTIVE
expires_at IS NOT NULL
expires_at < now
```

Then:

1. Set enforcement `EXPIRED`.
2. Insert `USER_ENFORCEMENT_LOGS`.
3. Insert `ADMIN_ACTION_LOGS` or system action log if policy requires.
4. Insert outbox event `USER_ENFORCEMENT_EXPIRED`.

### Outbox Publisher / Retry Job

1. Poll `outbox_events` with status `PENDING` or retryable `FAILED`.
2. Mark `PROCESSING`.
3. Publish to broker.
4. Mark `PUBLISHED` or `FAILED`.
5. Increment retry count and keep error detail if schema later adds `last_error`.

## 16. Security And Compliance

- Admin APIs require JWT and permission.
- All destructive actions require reason.
- All critical actions require audit log.
- Sensitive payloads must be redacted.
- Admin Service must not directly mutate another service database.
- Admin Service must not expose broad support data without explicit support permission.

## 17. Important Invariants

- Admin Service stores decision/audit; owner services apply domain state.
- Every enforcement state transition writes enforcement log.
- Every config change writes config history.
- Every critical admin action writes admin action log.
- Every cross-service event uses outbox.
- Restrict is not same as suspend: restricted user can login but loses selected capabilities.
- MVP refund/dispute/payout reversal is out of scope.

## 18. MVP Out Of Scope

- Real refund execution.
- Dispute workflow.
- Payout reversal.
- Full admin case management.
- Advanced risk scoring.
- ML moderation.
- Dedicated admin user database separate from Auth Service.

