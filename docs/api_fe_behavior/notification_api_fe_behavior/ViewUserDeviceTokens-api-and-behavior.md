# View User Device Tokens – API & Behavior

## 1. Business Goal

Cho phép user đã đăng nhập xem danh sách thiết bị/token đã đăng ký của mình, với token được mask để bảo mật.

## 2. API Contract

- **Method:** `GET`
- **URL:** `/api/v1/notification/device-tokens`
- **Auth:** Bearer JWT (user access token)
- **Query params:** none

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "Device tokens retrieved successfully",
  "data": {
    "items": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "deviceType": "ANDROID",
        "maskedDeviceToken": "****9999",
        "active": true,
        "lastUsedAt": "2026-05-24T12:00:00Z",
        "createdAt": "2026-05-20T08:00:00Z",
        "updatedAt": "2026-05-24T12:00:00Z"
      }
    ]
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

Response **không** bao giờ trả raw `deviceToken`.

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 500 | 500 | Lỗi hệ thống / DB unavailable |

## 5. Business Rules

- `user_id` lấy từ JWT subject.
- Chỉ trả token có `user_id = current_user_id`.
- Mỗi item gồm: `id`, `deviceType`, `maskedDeviceToken`, `active`, `lastUsedAt`, `createdAt`, `updatedAt`.
- Mask format: `****` + 4 ký tự cuối của token.
- Sort: active trước (`is_active DESC`), sau đó `updated_at DESC`.

## 6. Edge Cases

- User chưa có token → `items: []`.
- Token inactive vẫn hiển thị (audit/revoke UI).
- Token của user khác → không bao giờ xuất hiện.

## 7. Data Dependencies

- Table: `user_device_tokens`
- Index hỗ trợ: `idx_user_device_tokens_user_active`

## 8. FE Integration Notes

- Dùng `id` và `maskedDeviceToken` để hiển thị danh sách thiết bị.
- Revoke cần raw token từ client local storage — không lấy từ API này.
- Refresh sau register/revoke.
