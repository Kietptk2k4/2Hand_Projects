# UC - Cross Service Integration

## 1. Overview

Use case nay mo ta cach Admin Service dieu phoi enforcement/moderation voi Auth, Social, Commerce va Notification ma khong truy cap truc tiep DB cua service khac.

## 2. Actors

- **Admin Service:** Own decision/audit/outbox.
- **Auth Service:** Own user/session/role state.
- **Social Service:** Own post/comment/social restrictions.
- **Commerce Service:** Own product/shop/review/order state.
- **Notification Service:** Fan-out notifications/announcements.

## 3. Related Data

- `outbox_events`
- `admin_action_logs`
- `content_moderation_logs`
- `user_enforcements`

## 4. Business Rules

- Owner service owns final domain state.
- Admin Service owns admin decision and audit.
- Cross-service changes use internal API or outbox event.
- Consumers must be idempotent.

## 5. Sub-Use Cases

### 5.1. Integrate User Enforcement

**Main Flow:** Admin publishes user enforcement event; Auth/Social/Commerce apply status/restrictions.

### 5.2. Integrate Product/Review/Shop Moderation

**Main Flow:** Admin publishes moderation event; Commerce applies domain status.

### 5.3. Integrate Social Content Moderation

**Main Flow:** Admin publishes post/comment moderation event; Social applies visibility/status.

### 5.4. Integrate Announcements

**Main Flow:** Admin publishes announcement event; Notification Service fans out.

## 6. Acceptance Criteria

- No cross-service DB access.
- Every cross-service state change is audited and has outbox event.
- Owner service applies final state.

