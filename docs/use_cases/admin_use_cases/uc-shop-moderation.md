# UC - Shop Moderation

## 1. Overview

Use case nay mo ta admin moderation cho Commerce seller shops: suspend, reopen va close shop. Admin Service logs decision and publishes events; Commerce Service owns shop status.

## 2. Actors

- **Admin/Moderator:** Moderate shop.
- **Commerce Service:** Apply shop status and marketplace effects.

## 3. Related Data

- `content_moderation_logs`
- `admin_action_logs`
- `outbox_events`

## 4. Business Rules

- Shop moderation requires permission.
- Reason required.
- Suspended/closed shop blocks new commerce activity in Commerce.
- Existing orders remain supportable.
- Reopen does not automatically republish removed/archived products.

## 5. Sub-Use Cases

### 5.1. Suspend Shop

**Main Flow:** Admin logs action and publishes `SHOP_SUSPENDED`.

### 5.2. Reopen Shop

**Main Flow:** Admin logs action and publishes `SHOP_RESTORED`.

### 5.3. Close Shop

**Main Flow:** Admin logs action and publishes `SHOP_CLOSED`.

### 5.4. View Shop Moderation History

**Main Flow:** Query `content_moderation_logs` for shop target.

## 6. Acceptance Criteria

- Shop moderation requires permission.
- Logs and outbox event are written.
- Commerce owns final shop state.

