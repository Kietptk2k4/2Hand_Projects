# Follow User – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập theo dõi user khác, tạo quan hệ trong `FOLLOWS` và phát event `USER_FOLLOWED` qua outbox để Notification Service xử lý.

## 2. API Contract

- **Method:** POST
- **URL:** `/api/v1/social/users/{userId}/follow`
- **Auth:** Bearer JWT (required)
- **Request Body:** Không có

### Path Parameters

| Field   | Type | Required | Mô tả                          |
|---------|------|----------|--------------------------------|
| `userId`| UUID | yes      | ID người được theo dõi (followee). |

## 3. Response – Success

**HTTP 201 Created** (tạo relation mới)

```json
{
  "code": 201,
  "success": true,
  "message": "Theo doi nguoi dung thanh cong.",
  "data": {
    "followeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "ACCEPTED",
    "createdAt": "2026-05-19T10:30:00.123Z"
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

**HTTP 200 OK** (đã follow trước đó — idempotent)

```json
{
  "code": 200,
  "success": true,
  "message": "Theo doi nguoi dung thanh cong.",
  "data": {
    "followeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "ACCEPTED",
    "createdAt": "2026-05-18T08:00:00.000Z"
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

`status` có thể là `ACCEPTED` (tài khoản public) hoặc `PENDING` (tài khoản private).

## 4. Response – Error

| HTTP | Code string              | Mô tả                                      |
|------|--------------------------|--------------------------------------------|
| 400  | `SOCIAL-400`             | Tự follow chính mình.                      |
| 401  | `SOCIAL-401`             | Không có hoặc JWT không hợp lệ.            |
| 403  | `SOCIAL-403-SUSPENDED`   | Tài khoản follower bị SUSPENDED/DELETED.    |
| 404  | `SOCIAL-404`             | Followee không tồn tại hoặc đã xóa.       |
| 500  | `SOCIAL-500`             | Lỗi server.                                |

## 5. Business Rules

- `follower_id` lấy từ JWT; `followee_id` = `{userId}` path.
- Không được `follower_id == followee_id` → HTTP 400.
- Unique `(follower_id, followee_id)`; gọi lại khi đã follow → HTTP 200, **không** ghi outbox mới.
- Profile **public** (`is_private = false` hoặc null) → `status = ACCEPTED`.
- Profile **private** (`is_private = true`) → `status = PENDING`.
- Follow mới: insert `FOLLOWS` + `OUTBOX_EVENTS` (`USER_FOLLOWED`, PENDING).
- Follower `SUSPENDED`/`DELETED` → HTTP 403.

## 6. Edge Cases

- **Follow lại user đã follow:** 200, giữ nguyên `createdAt` và `status` hiện tại.
- **Follow user private:** `status = PENDING`; following feed chỉ nhận post khi relation `ACCEPTED`.
- **Follow user không có trong projection:** 404 (chưa sync từ Auth).

## 7. Data Dependencies

| Storage    | Table/Collection   | Action                                      |
|------------|--------------------|---------------------------------------------|
| PostgreSQL | `follows`          | Insert `(follower_id, followee_id, status)`. |
| PostgreSQL | `outbox_events`    | Insert `USER_FOLLOWED` khi follow mới.      |
| MongoDB    | `user_projections` | Read-only: followee tồn tại, `is_private`.  |

## 8. FE Integration Notes

- Dùng `data.status` để hiển thị "Đang theo dõi" / "Đã gửi yêu cầu" (`PENDING`).
- Sau follow thành công, có thể refresh following feed / profile counts (khi có API profile).
- Notification: downstream consume `USER_FOLLOWED` từ broker (không gọi trực tiếp từ FE).
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
