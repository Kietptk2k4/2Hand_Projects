# View Notification Settings – API & Behavior

## 1. Business Goal

Cho phép user đã đăng nhập xem effective notification preferences theo từng `event_type`, gồm giá trị đang áp dụng và cờ cho biết setting đó lấy từ DB (explicit) hay từ default policy.

## 2. API Contract

- **Method:** `GET`
- **URL:** `/api/v1/notification/notification-settings`
- **Auth:** Bearer JWT (user access token)
- **Query params:** none

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "Notification settings retrieved successfully",
  "data": {
    "settings": [
      {
        "eventType": "POST_LIKED",
        "allowPush": true,
        "allowEmail": true,
        "allowInApp": false,
        "explicitSetting": true
      },
      {
        "eventType": "USER_FOLLOWED",
        "allowPush": true,
        "allowEmail": true,
        "allowInApp": false,
        "explicitSetting": false
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 500 | 500 | Lỗi hệ thống / DB unavailable |

## 5. Business Rules

- `user_id` lấy từ JWT subject.
- Response luôn gồm **toàn bộ** `event_type` được support trong `NotificationDefaultChannelPolicy` (22 loại).
- Danh sách được sort theo `eventType` tăng dần.
- Nếu user có row trong `user_notification_settings` cho `event_type` → dùng giá trị DB, `explicitSetting: true`.
- Nếu không có row → merge default policy, `explicitSetting: false`.
- Không bao giờ trả settings của user khác.

## 6. Edge Cases

- User mới chưa có row nào → tất cả items dùng default, `explicitSetting: false`.
- User đã qua `USER_CREATED` handler → thường có đủ 22 row explicit sau khi initialize.
- Một phần event type có row, phần còn lại fallback default.

## 7. Data Dependencies

- Table: `user_notification_settings`
- Default policy: `NotificationDefaultChannelPolicy` (application config)

## 8. FE Integration Notes

- Dùng `explicitSetting` để hiển thị badge “mặc định” vs “đã tùy chỉnh” nếu cần.
- Settings screen nên map theo `eventType`; không hard-code danh sách nếu backend thêm event type mới.
- Cập nhật settings dùng `PUT /notification-settings/{eventType}` (FR_UpdateNotificationSettings).
