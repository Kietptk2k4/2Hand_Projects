# UC - Product Moderation

## 1. Overview

Use case nay mo ta admin moderation cho product cua Commerce Service: remove, restore va xem moderation history. Admin Service luu logs va publish events; Commerce Service own product state.

## 2. Actors

- **Admin/Moderator:** Remove/restore product.
- **Commerce Service:** Apply product state changes.
- **Outbox Worker:** Publish moderation events.

## 3. Related Data

- `content_moderation_logs`
- `admin_action_logs`
- `outbox_events`

## 4. Business Rules

- Product remove requires `PRODUCT_REMOVE`.
- Reason required.
- Admin Service must not update Commerce DB directly.
- Commerce sets `products.status = REMOVED` and invalidates cart items.

## 5. Sub-Use Cases

### 5.1. Remove Product

**Main Flow:**

1. Admin requests product removal.
2. System checks permission.
3. System writes `content_moderation_logs`.
4. System writes `admin_action_logs`.
5. System inserts outbox event `PRODUCT_REMOVED`.

### 5.2. Restore Product

**Main Flow:**

1. Admin requests restore.
2. System logs restore action.
3. System publishes `PRODUCT_RESTORED`.
4. Commerce validates final status according product readiness.

### 5.3. View Product Moderation History

**Main Flow:** Query moderation logs by `target_type = PRODUCT` and `target_id`.

## 6. Acceptance Criteria

- Product moderation writes logs and outbox event.
- Permission is enforced.
- Commerce remains owner of final product status.

