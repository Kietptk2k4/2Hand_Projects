# Functional Requirement - View Product Moderation History

## 1. Feature Overview

Cho phep admin xem lich su moderation cua mot product, bao gom remove/restore actions, reason, admin id va timestamp.

## 2. Actors

- **Admin/Moderator/Support:** Xem moderation history.
- **Admin Service:** Query moderation logs.

## 3. Scope

**In Scope:**

- List content moderation logs for product.
- Sort newest first.

**Out of Scope:**

- Commerce product edit history.
- Order history for product.

## 4. API Contract

**Endpoint:** `GET /admin/api/v1/products/{productId}/moderation-history`

**Auth:** Required, permission `PRODUCT_MODERATION_READ`.

## 5. Business Rules

- Filter `content_moderation_logs.target_type = PRODUCT`.
- Include action, reason, note, admin id, created_at.

## 6. Database Impact

- Read `content_moderation_logs`.

## 7. Transaction

- Read-only.

## 8. Security

- Permission required.

## 9. Failure Cases

- Missing permission -> 403.
- Product has no history -> empty list.

## 10. Acceptance Criteria

- Authorized admin can view product moderation history.
- Logs are sorted newest first.
- Unauthorized admin is denied.

