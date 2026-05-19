# Unfollow User – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập hủy quan hệ theo dõi với user khác bằng cách xóa record trong `FOLLOWS`.

## 2. API Contract

- **Method:** DELETE
- **URL:** `/api/v1/social/users/{userId}/follow`
- **Auth:** Bearer JWT (required)
- **Request Body:** Không có

### Path Parameters

| Field   | Type | Required | Mô tả                          |
|---------|------|----------|--------------------------------|
| `userId`| UUID | yes      | ID người được hủy theo dõi (followee). |

## 3. Response – Success

**HTTP 200 OK** (đã có relation và đã xóa)

```json
{
  "code": 200,
  "success": true,
  "message": "Huy theo doi nguoi dung thanh cong.",
  "data": {
    "followeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "wasFollowing": true
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

**HTTP 200 OK** (chưa từng follow — idempotent)

```json
{
  "code": 200,
  "success": true,
  "message": "Huy theo doi nguoi dung thanh cong.",
  "data": {
    "followeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "wasFollowing": false
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

## 4. Response – Error

| HTTP | Code string              | Mô tả                                      |
|------|--------------------------|--------------------------------------------|
| 401  | `SOCIAL-401`             | Không có hoặc JWT không hợp lệ.            |
| 403  | `SOCIAL-403-SUSPENDED`   | Tài khoản follower bị SUSPENDED/DELETED.    |
| 500  | `SOCIAL-500`             | Lỗi server.                                |

## 5. Business Rules

- `follower_id` lấy từ JWT; `followee_id` = `{userId}` path.
- Có relation → delete `FOLLOWS`, `wasFollowing: true`.
- Không có relation → **không** lỗi, HTTP 200, `wasFollowing: false` (idempotent).
- **Không** ghi `OUTBOX_EVENTS` / không bắt buộc event unfollow trong MVP.
- Gọi DELETE lặp lại khi đã unfollow → vẫn 200 an toàn.

## 6. Edge Cases

- **Unfollow user chưa từng follow:** 200, `wasFollowing: false`.
- **Unfollow chính mình:** 200 idempotent (không có relation).
- **Following feed:** Sau unfollow, post của followee không còn trong following feed (relation `ACCEPTED` đã mất).

## 7. Data Dependencies

| Storage    | Table     | Action                                      |
|------------|-----------|---------------------------------------------|
| PostgreSQL | `follows` | Delete `(follower_id, followee_id)`.        |
| MongoDB    | `user_projections` | Read-only: kiểm tra status follower. |

## 8. FE Integration Notes

- Luôn coi HTTP 200 là thành công; dùng `data.wasFollowing` nếu cần phân biệt “đã bỏ follow thật” vs “chưa follow”.
- Cập nhật UI nút Follow / trạng thái graph sau response.
- Cặp với `POST .../follow` để toggle trải nghiệm.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
