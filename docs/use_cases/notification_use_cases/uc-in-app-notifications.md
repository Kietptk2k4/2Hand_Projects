# UC - In-App Notifications

## 1. Overview

Use case nay mo ta viec tao in-app notification tu `notification_events` da ingest. In-app la channel chinh cua Notification Service MVP va duoc luu trong `user_notifications`.

## 2. Actors

- **Notification Worker:** Xu ly event va tao notification.
- **User:** Recipient cua notification.
- **Producer Service:** Cung cap event payload/recipient.

## 3. Related Data

- `notification_events`
- `user_notifications`
- `user_notification_settings`

## 4. Preconditions

- Event da ton tai voi `status = PENDING` hoac retryable `FAILED`.
- Event type co handler/template.
- Recipient co the resolve tu payload.

## 5. Business Rules

- `user_id` cua notification phai la recipient resolved server-side.
- Title/content duoc tao tu server-side template.
- `metadata` phai sanitized.
- Self notification bi skip voi social events.
- `allow_in_app = false` thi khong tao in-app notification, tru critical mandatory event.
- Retry khong duoc tao duplicate notification.

## 6. Sub-Use Cases

### UC-INAPP-01: Create In-App Notification

Main flow:

1. Worker lock `notification_events` row.
2. Worker resolve event handler theo `event_type`.
3. Worker resolve recipient list.
4. Worker load notification setting cua tung recipient.
5. Worker apply skip/self/settings rules.
6. Worker generate title/content/reference/metadata.
7. Worker insert `user_notifications`.
8. Worker mark delivery status `SENT` neu in-app la channel required duy nhat.

Postconditions:

- Recipient co notification moi trong notification center.
- Event co the tiep tuc xu ly push/email neu policy can.

### UC-INAPP-02: Skip In-App By Setting

Main flow:

1. Worker load setting cho `(user_id, event_type)`.
2. `allow_in_app = false`.
3. Worker khong insert `user_notifications`.
4. Worker tiep tuc channel khac neu duoc allow.

Postconditions:

- Khong co in-app notification moi cho recipient.

### UC-INAPP-03: Handle Duplicate Insert On Retry

Main flow:

1. Worker retry event da insert notification truoc do.
2. Worker insert theo idempotency key.
3. DB unique conflict xay ra.
4. Worker treat conflict as success.

Postconditions:

- Chi co mot notification cho same event/user/reference.

## 7. Failure Cases

- Missing recipient.
- Missing template.
- Invalid metadata.
- DB insert failure.
- Unsupported event type.

## 8. Security

- Notification content khong duoc expose private data khong can thiet.
- Deep link reference khong cap quyen truy cap resource.
- Client chi duoc doc notification cua chinh minh.

## 9. Acceptance Criteria

- In-app notification duoc tao dung recipient.
- Settings va self-skip duoc ap dung.
- Retry khong tao duplicate.
- Metadata/title/content an toan de return cho client.

