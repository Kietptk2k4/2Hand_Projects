# Functional Requirement - Handle Post Moderated Notification

## 1. Feature Overview

Notify post author when Admin publishes `POST_MODERATED`.

## 2. Scope

**In Scope:**

- Create in-app notification for post author.
- Send push notification by default policy.
- Include user-safe moderation reason and action (HIDE/REMOVE).

**Out of Scope:**

- Moderating post in Social.
- Email notification.

## 3. Event Contract

Required payload:

- `post_id`
- `author_user_id` (or `post_author_id`)
- `action` (HIDE or REMOVE)
- `reason` user-safe

## 4. Business Rules

- Admin Service owns moderation decision.
- Internal admin notes must not be exposed.
- Reference: `POST/post_id`.

## 5. Acceptance Criteria

- Post author receives moderation notice with action-specific copy.
- Notification Service does not mutate Social/Admin state.