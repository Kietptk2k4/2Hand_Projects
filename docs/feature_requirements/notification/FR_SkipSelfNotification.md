# Functional Requirement - Skip Self Notification

## 1. Feature Overview

Prevent notifications where actor and recipient are the same user for interaction events such as likes, comments, replies and follows.

## 2. Actors

- **Notification Worker:** Applies self-skip rule.
- **Actor User:** Performs action.
- **Recipient User:** Would otherwise receive notification.

## 3. Scope

**In Scope:**

- Compare `actor_id` and recipient id.
- Skip notification for self social interactions.
- Mark event completed if no recipients remain.

**Out of Scope:**

- Preventing self action in Social Service.
- Applying self-skip to commerce/account/system events.

## 4. Event Types

Applies to:

- `POST_LIKED`
- `COMMENT_LIKED`
- `COMMENT_CREATED`
- `COMMENT_REPLIED`
- `USER_FOLLOWED`

Does not apply to:

- `ORDER_CREATED`
- `PAYMENT_SUCCESS`
- `USER_SUSPENDED`
- System/security events.

## 5. Business Rules

- If `actor_id == recipient_user_id`, do not create in-app/push/email.
- Skipped notification should not be treated as failure.
- If all recipients are skipped, event can be `COMPLETED`.
- Missing `actor_id` for self-skip event should fail or use conservative policy defined per handler.

## 6. Database Impact

- No `user_notifications` insert for skipped recipient.
- Update `notification_events` status when processing completes.

## 7. Acceptance Criteria

- Users do not receive notification for own like/comment/reply/follow.
- Non-self interactions still notify.
- Skipped events do not retry forever.

