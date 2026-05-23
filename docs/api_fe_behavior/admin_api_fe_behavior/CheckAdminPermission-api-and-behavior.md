# Check Admin Permission – API & Behavior

## 1. Business Goal

Cho phép Admin FE (hoặc use case nội bộ) kiểm tra admin hiện tại có permission cụ thể hay không — trước khi hiển thị nút hành động hoặc gọi API destructive. Nguồn permission: JWT claims (MVP) hoặc Auth Service khi `admin.integrations.auth.enabled=true`.

## 2. API Contract

| Method | URL | Auth |
|--------|-----|------|
| GET | `/admin/api/v1/me/permissions/check` | Bearer JWT (required) |

### Query parameters

| Param | Required | Mô tả |
|-------|----------|--------|
| `permission` | Yes | Mã permission UPPER_SNAKE_CASE (vd: `USER_SUSPEND`) |
| `resource_type` | No | Ngữ cảnh tài nguyên (MVP: echo trong response, chưa ảnh hưởng grant) |
| `resource_id` | No | Id tài nguyên (MVP: echo trong response) |

**Lưu ý:** `admin_id` **không** nhận từ client — luôn lấy từ JWT subject.

## 3. Response – Success (200)

```json
{
  "code": 200,
  "success": true,
  "message": "Kiem tra permission admin thanh cong.",
  "data": {
    "admin_id": "uuid",
    "permission_code": "USER_SUSPEND",
    "granted": true,
    "resource_type": "USER",
    "resource_id": "target-uuid"
  },
  "errors": null,
  "timestamp": "..."
}
```

`granted: false` vẫn trả **200** — FE dùng để ẩn/hiện UI; API destructive vẫn enforce 403 qua `@RequireAdminPermission` / `requirePermission()`.

## 4. Response – Error

| HTTP | Mô tả |
|------|--------|
| 401 | Thiếu/invalid JWT |
| 400 | `ADMIN-400-VALIDATION` — thiếu `permission`, format sai, hoặc mã không thuộc danh sách admin permission đã hỗ trợ |
| 503 | Auth integration bật nhưng không verify được permission (fail closed) |

## 5. Business Rules

- JWT phải verified trước khi check.
- Không tin permission do client “tự khai báo có quyền”.
- Check từ JWT claims trước; fallback Auth `GET /api/v1/admin/users/{adminId}/permissions` khi integration bật.
- Protected mutation APIs vẫn phải gọi `requirePermission()` — endpoint check **không** thay thế enforcement.

## 6. Edge Cases

- Permission có trong JWT → `granted: true`.
- Permission không có trong JWT, integration tắt → `granted: false`.
- Permission không có, integration bật, Auth trả danh sách có mã → `granted: true`.
- Auth down khi integration bật → 503.

## 7. Data Dependencies

- Không ghi PostgreSQL.

## 8. FE Integration Notes

- Gọi trước khi render menu moderation/enforcement/config.
- Dùng `granted` để enable/disable nút; vẫn xử lý 403 khi gọi API thực thi.
- Mã permission tham chiếu `AdminPermission` constants / `GET /admin/api/v1/me` (`permissions` array).
- Ví dụ: `GET /admin/api/v1/me/permissions/check?permission=PRODUCT_REMOVE&resource_type=PRODUCT&resource_id={id}`

## 9. Supported Permission Codes (MVP)

`USER_SUSPEND`, `USER_RESTRICT`, `PRODUCT_REMOVE`, `REVIEW_HIDE`, `SHOP_SUSPEND`, `POST_MODERATE`, `COMMENT_MODERATE`, `SYSTEM_CONFIG_UPDATE`, `SYSTEM_ANNOUNCEMENT_PUBLISH`, `ADMIN_AUDIT_READ`, `ORDER_SUPPORT_READ`
