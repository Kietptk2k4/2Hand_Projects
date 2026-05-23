# Handle Post Moderated Event – API & Behavior

## 1. Business Goal
Áp dụng quyết định moderation từ Admin Service lên post trong MongoDB khi nhận event `POST_MODERATED` (topic `admin.post.moderated`). Admin không mutate Social DB; Social own trạng thái post và visibility trên feed/search/profile.

## 2. Event Contract

- **Transport:** Kafka
- **Topic:** `admin.post.moderated`
- **Consumer group:** `social-post-moderated`
- **Event type:** `POST_MODERATED`

### Envelope (Admin outbox)

```json
{
  "event_id": "uuid",
  "event_type": "POST_MODERATED",
  "occurred_at": "2026-05-23T10:00:00Z",
  "payload": {
    "post_id": "507f1f77bcf86cd799439011",
    "moderation_log_id": "uuid",
    "action": "HIDE",
    "reason": "Noi dung vi pham chinh sach",
    "moderated_by": "uuid-admin",
    "moderated_at": "2026-05-23T10:00:00Z"
  }
}
```

`note` nội bộ admin **không** có trong payload broker.

## 3. Action Mapping

| Admin `action` | Social effect | Discovery (feed/search/profile người khác) |
|----------------|---------------|--------------------------------------------|
| `HIDE` | `status=ACTIVE`, `moderation_status=HIDDEN` | Không hiển thị |
| `REMOVE` | `status=DELETED`, `moderation_status=REMOVED`, `deleted_at` set | 404 / không hiển thị |

Author vẫn có thể xem post `HIDDEN` qua `FR_ViewPostDetail` (policy trả về như post tồn tại với viewer là author).

## 4. Idempotency

- `processed_domain_events` theo `event_id` (envelope).
- Trùng `moderation_log_id` + `action` đã áp dụng → no-op.
- Post đã `DELETED` + event `REMOVE` → skip.
- Post không tồn tại → log warning, vẫn mark processed (không retry vô hạn).

## 5. Failure Handling

| Tình huống | Hành vi |
|------------|---------|
| Payload invalid / `post_id` sai format | Log error, **ack** (không retry) |
| `action` không hợp lệ | Log error, **ack** |
| Mongo lỗi tạm thời | Retry (Kafka error handler), không ack |
| Post missing | Warn, mark processed, ack |

## 6. Configuration

```yaml
social:
  kafka:
    consumer:
      enabled: true
      post-moderated-group-id: social-post-moderated
      post-moderated-topics:
        - admin.post.moderated
```

## 7. Data Dependencies

| Storage | Usage |
|---------|--------|
| MongoDB `posts` | Update `status`, `moderation_status`, `moderation_reason`, `last_moderation_log_id`, `deleted_at`, `updated_at` |
| PostgreSQL `processed_domain_events` | Idempotency |

## 8. Related

- `docs/feature_requirements/social/FR_HandlePostModeratedEvent.md`
- `docs/api_fe_behavior/admin_api_fe_behavior/ModeratePost-api-and-behavior.md`
- `FR_ViewPostDetail`, `FR_ViewGlobalFeed`, `FR_ViewUserPosts`
