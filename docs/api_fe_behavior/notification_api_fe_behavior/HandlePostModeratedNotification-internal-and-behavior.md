# Handle Post Moderated Notification - Internal and Behavior

## 1. Business Goal

Notify **post author** when Admin publishes `POST_MODERATED`: in-app + push (no email).

## 2. Trigger

- Kafka: `admin.post.moderated`
- Internal ingest: `eventType` = `POST_MODERATED`

## 3. Flow

1. **Ingest:** `AdminPostModerationPayloadNormalizer` maps reason to `moderation_reason`, strips `moderated_by`.
2. **Worker:** `PostModeratedNotificationEventHandler` (@Order 44).
3. **Reference:** `POST/{post_id}`.

## 4. Admin Payload

Required: `post_id`, `author_user_id`, `action`, `reason`.

## 5. FE / Client

- Deep link: `reference_type=POST`, `reference_id={post_id}`.
- Titles: **Post hidden** or **Post removed** by action.