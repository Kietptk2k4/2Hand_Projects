# Functional Requirement - Handle Shop Suspended Notification

## 1. Feature Overview

Notify shop owner when Admin publishes `SHOP_SUSPENDED`.

## 2. Actors

- **Admin Service:** Publishes shop moderation event.
- **Notification Service:** Sends notification.
- **Shop Owner:** Recipient.

## 3. Scope

**In Scope:**

- Create shop suspended in-app notification.
- Send push/email by critical policy.
- Include user-safe reason/duration.

**Out of Scope:**

- Suspending/reopening shop.
- Commerce shop mutation.

## 4. Event Contract

Required payload:

- `shop_id`
- `shop_owner_id`
- `reason` optional user-safe
- `expires_at` optional

## 5. Business Rules

- Admin/Commerce own moderation/effect; Notification owns delivery.
- Shop suspension is business-critical for seller and can send email.
- Internal admin notes must not be exposed.
- Reference: `SHOP/shop_id`.
- Duplicate event is idempotent.

## 6. Database Impact

- Insert `user_notifications` if allowed/required.
- Update `notification_events`.

## 7. Failure Cases

- Missing shop owner -> failed.
- Missing shop id -> failed.
- Email provider failure -> retry.

## 8. Acceptance Criteria

- Shop owner receives suspension notice.
- Email is sent when policy allows/requires.
- Commerce shop state is not mutated.

