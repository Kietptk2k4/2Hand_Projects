# Revoke Device Token – API & Behavior

## 1. Business Goal

Deactivate device token khi user logout khỏi thiết bị, để push delivery không gửi notification tới token đó nữa.

## 2. API Contract

- **Method:** `DELETE`
- **URL:** `/api/v1/notification/device-tokens/{deviceToken}`
- **Auth:** Bearer JWT (user access token)
- **Path params:** `deviceToken` — giá trị token (URL-encoded nếu có ký tự đặc biệt)

## 3. Response – Success

**HTTP 200**

```json
{
  "code": 200,
  "success": true,
  "message": "Device token revoked successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "active": false,
    "alreadyRevoked": false
  },
  "errors": null,
  "timestamp": "2026-05-24T12:00:01Z"
}
```

`alreadyRevoked: true` khi token của user đã inactive trước đó (idempotent).

## 4. Response – Error

| HTTP | code (envelope) | Mô tả |
|------|-----------------|-------|
| 400 | 400 | Token blank / quá dài |
| 401 | 401 | Thiếu hoặc JWT không hợp lệ |
| 404 | 404 | Token không tồn tại hoặc không thuộc user hiện tại |
| 500 | 500 | Lỗi hệ thống / DB unavailable |

## 5. Business Rules

- `user_id` lấy từ JWT subject.
- Chỉ revoke token thuộc user hiện tại; token của user khác trả **404** (không leak ownership).
- Soft deactivate: `is_active = false`, cập nhật `updated_at = now()`; **không** hard delete row.
- Idempotent: revoke lại token đã inactive → 200, `alreadyRevoked: true`.
- Push delivery bỏ qua token inactive (`existsActiveByUserId` = false).

## 6. Edge Cases

- Token không tồn tại → 404.
- Whitespace quanh token → trim trước khi lookup.
- Row vẫn còn trong DB sau revoke (audit/debug).

## 7. Data Dependencies

- Table: `user_device_tokens`

## 8. FE Integration Notes

- Gọi khi logout hoặc user tắt push trên thiết bị.
- Dùng cùng giá trị `deviceToken` đã register qua `POST /device-tokens`.
- URL-encode token nếu chứa ký tự đặc biệt (`:`, `/`, …).
- Không log full token ở client.
