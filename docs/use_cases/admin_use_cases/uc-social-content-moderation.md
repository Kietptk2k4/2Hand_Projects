# UC - Social Content Moderation

## 1. Overview

Use case nay mo ta admin moderation cho Social Service post/comment. Admin Service logs decision and publishes moderation events; Social Service owns content state and visibility.

## 2. Actors

- **Admin/Moderator:** Hide/remove/restore post/comment.
- **Social Service:** Apply content state changes.

## 3. Related Data

- `content_moderation_logs`
- `admin_action_logs`
- `outbox_events`

## 4. Business Rules

- Post/comment moderation requires social moderation permission.
- Reason required.
- Admin Service does not access Social DB.
- Social validates final restore/remove/hide effect.

## 5. Sub-Use Cases

### 5.1. Moderate Post

**Main Flow:**

1. Admin requests hide/remove/restore post.
2. System checks permission.
3. System logs moderation action.
4. System publishes `POST_MODERATED`.

### 5.2. Moderate Comment

**Main Flow:**

1. Admin requests hide/remove/restore comment.
2. System checks permission.
3. System logs moderation action.
4. System publishes `COMMENT_MODERATED`.

### 5.3. View Moderation History

**Main Flow:** Query logs by target type and id.

## 6. Acceptance Criteria

- Social content moderation is permission protected.
- Logs and outbox event are written.
- Social Service owns final content state.

