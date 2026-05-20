# UC - Social Notifications

## 1. Overview

Use case nay mo ta cac notification duoc tao tu Social Service events: post liked, user followed, comment created, comment replied va comment liked.

## 2. Actors

- **Social Service:** Publish social domain events.
- **Notification Service:** Consume va deliver notification.
- **Actor User:** Nguoi like/follow/comment/reply.
- **Recipient User:** Chu post/comment hoac user duoc follow.

## 3. Related Data

- `notification_events`
- `user_notifications`
- `user_notification_settings`
- `user_device_tokens`

## 4. Preconditions

- Social event duoc publish voi recipient fields day du.
- Event payload co `actor_id` va target owner id.
- Notification handler support event type.

## 5. Business Rules

- Notification Service khong query Social DB trong MVP.
- Social payload phai co recipient.
- Self notification bi skip.
- Default channels: in-app + push, khong email.
- User settings co the disable in-app/push.

## 6. Sub-Use Cases

### UC-SOCIAL-01: Notify Post Liked

Main flow:

1. Social publishes `POST_LIKED`.
2. Notification resolves recipient = `post_author_id`.
3. If `actor_id == post_author_id`, skip.
4. Create notification reference `POST/post_id`.
5. Send push if allowed.

### UC-SOCIAL-02: Notify User Followed

Main flow:

1. Social publishes `USER_FOLLOWED`.
2. Notification resolves recipient = `followed_user_id`.
3. Skip if actor is recipient.
4. Create notification reference `USER/actor_id`.
5. Send push if allowed.

### UC-SOCIAL-03: Notify Comment Created

Main flow:

1. Social publishes `COMMENT_CREATED`.
2. Notification resolves recipient = `post_author_id`.
3. Skip if commenter is post author.
4. Create notification reference `POST/post_id` or `COMMENT/comment_id`.

### UC-SOCIAL-04: Notify Comment Replied

Main flow:

1. Social publishes `COMMENT_REPLIED`.
2. Notification resolves recipient = `parent_comment_author_id`.
3. Skip self reply.
4. Create notification reference `COMMENT/parent_comment_id`.

### UC-SOCIAL-05: Notify Comment Liked

Main flow:

1. Social publishes `COMMENT_LIKED`.
2. Notification resolves recipient = `comment_author_id`.
3. Skip self-like.
4. Create notification reference `COMMENT/comment_id`.

## 7. Failure Cases

- Missing recipient owner id.
- Missing actor id for self-skip event.
- Unsupported event type.
- Duplicate social event.
- Push provider failure.

## 8. Security

- Do not include private post/comment body unless payload is safe.
- Actor display summary must be sanitized.
- Deep link access remains authorized by Social Service.

## 9. Acceptance Criteria

- Social events notify correct owners.
- Self actions do not notify.
- Email is not sent for social MVP notifications.
- Duplicate events do not duplicate notifications.
- Social DB is not accessed directly.

