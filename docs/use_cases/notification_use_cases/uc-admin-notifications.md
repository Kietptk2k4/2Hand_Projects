# UC - Admin Notifications

## 1. Overview

Use case nay mo ta notification duoc tao tu Admin Service events lien quan user enforcement, moderation va shop status.

## 2. Actors

- **Admin Service:** Publish enforcement/moderation events.
- **Notification Service:** Consume va deliver.
- **Target User/Seller:** Recipient.
- **Email/Push Provider:** External delivery.

## 3. Related Data

- `notification_events`
- `user_notifications`
- `user_notification_settings`
- `user_device_tokens`

## 4. Preconditions

- Admin event da duoc publish qua outbox.
- Event payload co target user/seller id.
- Reason/note trong payload da duoc sanitize hoac co field user-safe.

## 5. Business Rules

- Admin Service owns enforcement/moderation decision.
- Notification Service owns delivery only.
- Critical account/shop events can use email by default.
- Internal admin notes must not be exposed to user.
- Suspended user can still receive account-critical notification.

## 6. Sub-Use Cases

### UC-ADMIN-01: Notify User Suspended

Main flow:

1. Admin publishes `USER_SUSPENDED`.
2. Notification resolves `target_user_id`.
3. Notification creates account enforcement notification.
4. Notification sends push/email according to critical policy.

### UC-ADMIN-02: Notify User Restricted

Main flow:

1. Admin publishes `USER_RESTRICTED`.
2. Notification resolves target user.
3. Notification creates in-app notification explaining restricted capabilities at high level.
4. Notification sends email if policy requires.

### UC-ADMIN-03: Notify Product Removed

Main flow:

1. Admin publishes `PRODUCT_REMOVED`.
2. Notification resolves seller/product owner.
3. Notification creates notification reference `PRODUCT/product_id`.
4. Notification sends push if allowed.

### UC-ADMIN-04: Notify Review Hidden

Main flow:

1. Admin publishes `REVIEW_HIDDEN`.
2. Notification resolves review author and optional seller.
3. Notification creates in-app notification if policy enables this MVP optional path.

### UC-ADMIN-05: Notify Shop Suspended

Main flow:

1. Admin publishes `SHOP_SUSPENDED`.
2. Notification resolves shop owner.
3. Notification creates shop enforcement notification.
4. Notification sends push/email according to critical policy.

## 7. Failure Cases

- Missing target user/seller id.
- Payload includes unsafe internal note.
- Unknown moderation event type.
- Email provider failure.
- Duplicate admin event.

## 8. Security

- Do not expose admin internal notes.
- Reason must be user-safe.
- Notification Service must not mutate Admin/Auth/Social/Commerce state.

## 9. Acceptance Criteria

- Enforcement events notify target user.
- Product/shop moderation events notify owner.
- Critical events can still reach suspended user.
- Internal moderation details remain hidden.
- Duplicate events are idempotent.

