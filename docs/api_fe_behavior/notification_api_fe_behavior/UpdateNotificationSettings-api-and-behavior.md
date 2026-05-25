# Update Notification Settings – API & Behavior

## 1. Business Goal

Cho phép user đã đăng nhập cập nhật notification preferences cho một `event_type` cụ thể. Thay đổi chỉ ảnh hưởng delivery tương lai, không backfill notification cũ.

## 2. API Contract

- **Method:** `PUT`
- **URL:** `/api/v1/notification/notification-settings/{eventType}`
- **Auth:** Bearer JWT (user access token)
- **Path params:** `eventType` — canonical hoặc alias được support (vd. `POST_LIKED`, `COMMERCE_ORDER_CREATED`)
- **Request body:**

```json
{
  "allowPush": false,
  "allowEmail": false,
  "allowInApp": true
}
```

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "Notification setting updated successfully",
  "data": {
    "eventType": "POST_LIKED",
    "allowPush": false,
    "allowEmail": false,
    "allowInApp": true,
    "explicitSetting": true
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 400 | 400 | Event type không support, body thiếu/sai boolean |
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 500 | 500 | Lỗi hệ thống / DB unavailable |

Validation error mẫu (unknown event type):

```json
{
  "code": 400,
  "success": false,
  "message": "Unknown event type for notification settings",
  "data": null,
  "errors": [
    {
      "field": "eventType",
      "reason": "Event type is not configured for delivery."
    }
  ],
  "timestamp": "2026-05-24T12:00:01Z"
}
```

## 5. Business Rules

- `user_id` lấy từ JWT subject; chỉ upsert settings của chính user đó.
- `eventType` được normalize (trim + uppercase) và resolve alias trước khi validate.
- `eventType` phải nằm trong `NotificationDefaultChannelPolicy`.
- Upsert theo key `(user_id, event_type)`; insert nếu chưa có, update nếu đã có.
- `created_at` giữ nguyên khi update; `updated_at` luôn set `now()`.
- `explicitSetting` trong response luôn `true` sau khi update thành công.
- Critical override policies (security/system events) vẫn có thể áp dụng ở delivery layer.

## 6. Edge Cases

- Lần đầu update event type chưa có row → insert mới.
- Update lại cùng event type → single row, không duplicate.
- Body thiếu `allowPush` / `allowEmail` / `allowInApp` → 400 validation.
- Event type alias commerce (`COMMERCE_ORDER_CREATED`) → lưu canonical `ORDER_CREATED`.

## 7. Data Dependencies

- Table: `user_notification_settings`

## 8. FE Integration Notes

- Sau update thành công, có thể refresh `GET /notification-settings` hoặc merge local state từ response `data`.
- Không cần gọi lại mark-read hay reload notification list — settings chỉ ảnh hưởng event tương lai.
- Toggle UI nên gửi đủ 3 boolean flags trong body.
