# Check Admin Role – API & Behavior

## 1. Business Goal

Kiểm tra admin hiện tại có role phù hợp (`MODERATOR`, `SUPPORT`, `SUPER_ADMIN`) để truy cập nhóm tính năng admin portal. Role lấy từ JWT claims (MVP) hoặc Auth Service khi integration bật.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/me/roles` | Bearer JWT (required) |
| GET | `/admin/api/v1/me/roles/check` | Bearer JWT (required) |

### Query parameters (`/check`)

| Param | Required | Mô tả |
|-------|----------|--------|
| `role` | Yes | Mã role: `MODERATOR`, `SUPPORT`, `SUPER_ADMIN` |

**Lưu ý:** Không nhận `admin_id` từ client — luôn từ JWT.

## 3. Response – Success (200)

**List roles** (`GET /admin/api/v1/me/roles`):

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach role admin thanh cong.",
  "data": {
    "admin_id": "uuid",
    "roles": ["MODERATOR", "SUPPORT"]
  },
  "errors": null,
  "timestamp": "..."
}
```

**Check role** (`GET /admin/api/v1/me/roles/check?role=MODERATOR`):

```json
{
  "code": 200,
  "success": true,
  "message": "Kiem tra role admin thanh cong.",
  "data": {
    "admin_id": "uuid",
    "role_code": "MODERATOR",
    "granted": true
  },
  "errors": null,
  "timestamp": "..."
}
```

`granted: false` vẫn HTTP 200 — dùng cho UI; enforcement mutation dùng `requireAnyRole()` → 403.

## 4. Response – Error

| HTTP | Mô tả |
|------|--------|
| 401 | Thiếu/invalid JWT |
| 400 | `ADMIN-400-VALIDATION` — thiếu/sai `role`, hoặc role không thuộc MVP admin roles |
| 403 | Khi gọi API protected với `requireAnyRole()` và thiếu role |
| 503 | Auth integration bật nhưng không verify được role |

## 5. Business Rules

- Role source-of-truth: Auth Service; Admin chỉ đọc JWT / optional Auth HTTP.
- Không tin role client gửi trong body/query ngoài mã cần **kiểm tra**.
- `requireAnyRole()` chặn trước domain mutation (403).
- Endpoint `/check` không thay thế enforcement.

## 6. Edge Cases

- JWT không có claim `roles` → `roles: []`, `granted: false`.
- Role code case-insensitive input → normalize UPPER_SNAKE_CASE.
- Auth integration bật, chưa có HTTP roles API → fallback JWT only (MVP).

## 7. Data Dependencies

- Không ghi PostgreSQL.

## 8. FE Integration Notes

- `GET /admin/api/v1/me/roles` để hydrate role badges / route guards.
- `GET /admin/api/v1/me/roles/check?role=SUPPORT` trước khi mở màn support read-only.
- Kết hợp với `GET /admin/api/v1/me/permissions/check` cho fine-grained actions.
- Enforcement API destructive vẫn có thể trả 403 dù UI check là optional.
