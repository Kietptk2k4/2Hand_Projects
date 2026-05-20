# Functional Requirement - Handle Product Removed Notification

## 1. Feature Overview

Notify seller/product owner when Admin publishes `PRODUCT_REMOVED`.

## 2. Actors

- **Admin Service:** Publishes moderation event.
- **Notification Service:** Notifies seller.
- **Seller:** Recipient.

## 3. Scope

**In Scope:**

- Create product removed notification.
- Send push by default.
- Include safe moderation reason if available.

**Out of Scope:**

- Removing/restoring product.
- Commerce product state mutation.

## 4. Event Contract

Required payload:

- `product_id`
- `seller_user_id`
- `reason` optional user-safe

## 5. Business Rules

- Commerce owns product state; Admin owns moderation decision.
- Notification Service only delivers seller notice.
- Internal admin notes must not be included.
- Reference: `PRODUCT/product_id`.
- Default channels: in-app + push.

## 6. Database Impact

- Insert `user_notifications` if allowed.
- Update `notification_events`.

## 7. Failure Cases

- Missing seller user id -> failed.
- Missing product id -> failed.
- Unsafe reason -> sanitize or omit.

## 8. Acceptance Criteria

- Seller receives product removed notification.
- Product state is not mutated by Notification Service.
- Duplicate event does not duplicate notice.

