# View Social Profile – API & Behavior

## 1. Business Goal
Cho phép người dùng đã đăng nhập xem social profile của user khác (hoặc chính mình): thông tin projection local và social counters, tuân thủ privacy.

## 2. API Contract

- **Method:** GET
- **URL:** `/api/v1/social/users/{userId}/profile`
- **Auth:** Bearer JWT (required)
- **Request Body:** Không có

### Path Parameters

| Field   | Type | Required | Mô tả                    |
|---------|------|----------|--------------------------|
| `userId`| UUID | yes      | ID user cần xem profile. |

## 3. Response – Success

**HTTP 200 OK** (public hoặc có quyền xem đầy đủ)

```json
{
  "code": 200,
  "success": true,
  "message": "Lay social profile thanh cong.",
  "data": {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "displayName": "User B",
    "avatarUrl": "https://cdn.example/avatar.png",
    "isPrivate": false,
    "followerCount": 10,
    "followingCount": 5,
    "followStatus": "NONE",
    "canViewFullProfile": true
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

**HTTP 200 OK** (private, viewer chưa được chấp nhận follow)

```json
{
  "code": 200,
  "success": true,
  "message": "Lay social profile thanh cong.",
  "data": {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "displayName": "User B",
    "avatarUrl": "https://cdn.example/avatar.png",
    "isPrivate": true,
    "followerCount": null,
    "followingCount": null,
    "followStatus": "PENDING",
    "canViewFullProfile": false
  },
  "errors": null,
  "timestamp": "2026-05-19T10:30:00.123Z"
}
```

### `followStatus` values

| Value      | Mô tả                                              |
|------------|----------------------------------------------------|
| `SELF`     | Viewer đang xem profile của chính mình.             |
| `NONE`     | Chưa follow target.                                |
| `PENDING`  | Đã gửi follow, chờ chấp nhận (private account).    |
| `ACCEPTED` | Đang follow với quan hệ đã chấp nhận.              |

## 4. Response – Error

| HTTP | Code string    | Mô tả                                |
|------|----------------|--------------------------------------|
| 401  | `SOCIAL-401`   | Không có hoặc JWT không hợp lệ.      |
| 404  | `SOCIAL-404`   | User không tồn tại hoặc đã `DELETED`. |
| 500  | `SOCIAL-500`   | Lỗi server.                          |

## 5. Business Rules

- Đọc profile từ MongoDB `user_projections` (sync từ Auth).
- Social counters (`followerCount`, `followingCount`) chỉ đếm relation `ACCEPTED` trong `follows`.
- **`canViewFullProfile = true`** khi: xem chính mình, target public, hoặc viewer follow target với `ACCEPTED`.
- **Private + chưa đủ quyền:** vẫn trả `displayName`, `avatarUrl`, `isPrivate`, `followStatus`; **ẩn** counters (`null`).
- User `DELETED` / không có projection → **404**.
- Không ghi outbox; read-only.

## 6. Edge Cases

- **Xem profile chính mình:** `followStatus = SELF`, luôn có counters.
- **Private + `PENDING`:** `canViewFullProfile = false`, counters `null`.
- **Projection chưa sync từ Auth:** 404 cho đến khi có dữ liệu local.

## 7. Data Dependencies

| Storage    | Collection/Table   | Action                                      |
|------------|--------------------|---------------------------------------------|
| MongoDB    | `user_projections` | Read target profile.                        |
| PostgreSQL | `follows`          | Read relation viewer→target; count ACCEPTED. |

## 8. FE Integration Notes

- Dùng `canViewFullProfile` để quyết định hiển thị counters / nội dung nhạy cảm.
- Dùng `followStatus` cho nút Follow / Unfollow / "Đã gửi yêu cầu".
- Chi tiết bio/website: gọi Auth public profile nếu cần; API này tập trung social graph.
- Tham chiếu: `docs/engineering_rules/frontend-api-integration.md`.
