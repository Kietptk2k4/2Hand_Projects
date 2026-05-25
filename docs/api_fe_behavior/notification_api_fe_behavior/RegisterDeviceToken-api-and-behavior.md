# Register Device Token – API & Behavior

## 1. Business Goal

Đăng ký hoặc kích hoạt lại FCM device token của user sau login, để push delivery có thể gửi notification tới thiết bị.

## 2. API Contract

- **Method:** `POST`
- **URL:** `/api/v1/notification/device-tokens`
- **Auth:** Bearer JWT (user access token)
- **Request body:**

```json
{
  "deviceType": "ANDROID",
  "deviceToken": "fcm-token-value"
}
```

`deviceType` hỗ trợ: `IOS`, `ANDROID`, `WEB` (case-insensitive).

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "Device token registered successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "deviceType": "ANDROID",
    "active": true,
    "createdAt": "2026-05-24T12:00:00Z",
    "updatedAt": "2026-05-24T12:00:01Z",
    "lastUsedAt": "2026-05-24T12:00:01Z",
    "alreadyRegistered": false
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

Response **không** trả raw `deviceToken`.

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 400 | 400 | `deviceType` không hợp lệ, thiếu/blank token, token > 512 ký tự |
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 500 | 500 | Lỗi hệ thống / DB unavailable |

## 5. Business Rules

- `user_id` lấy từ JWT subject; không nhận `userId` từ body.
- Upsert theo `device_token` (unique toàn hệ thống).
- Luôn set `is_active = true`, cập nhật `last_used_at` và `updated_at = now()`.
- Nếu token đã tồn tại: giữ `id`, giữ `created_at`, cập nhật `user_id` và `device_type` theo request hiện tại (reassign khi user đăng nhập trên cùng thiết bị).
- `alreadyRegistered: true` khi cùng user + cùng `deviceType` + token đã active trước đó.
- Không log full token; server chỉ log masked suffix (`****1234`).

## 6. Edge Cases

- Gọi lại cùng token → idempotent, không tạo row mới.
- Token inactive (revoked) → reactivate.
- Token thuộc user khác → reassign sang user hiện tại.
- Token có whitespace → trim trước khi lưu.

## 7. Data Dependencies

- Table: `user_device_tokens`

## 8. FE Integration Notes

- Gọi sau login và khi FCM refresh token.
- Lưu `id` nếu cần cho revoke/view sau này.
- Không hiển thị raw token trong UI/debug log.
- Logout nên gọi `DELETE /device-tokens/{deviceToken}` (FR_RevokeDeviceToken).
