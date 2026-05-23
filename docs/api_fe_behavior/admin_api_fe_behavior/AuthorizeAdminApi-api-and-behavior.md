# Authorize Admin API – API & Behavior

## 1. Business Goal

Đảm bảo mọi Admin API dưới `/admin/api/v1/**` được xác thực JWT và kiểm tra permission/role trước khi chạy use case — từ chối request không hợp lệ, không thực hiện domain mutation khi unauthorized.

## 2. API Contract

**Áp dụng cho:** Tất cả endpoint ` /admin/api/v1/**` (trừ actuator health/info).

**Auth:** `Authorization: Bearer <access_token>` (token do Auth Service cấp khi admin login).

**Endpoint tham chiếu (không bắt buộc permission riêng):**

| Method | URL | Mô tả |
|--------|-----|--------|
| GET | `/admin/api/v1/me` | Trả về `admin_id`, `roles`, `permissions` từ JWT |
| GET | `/admin/api/v1/authorization-probe/user-suspend` | Probe — yêu cầu `USER_SUSPEND` |

**Endpoint khác (moderation, enforcement, …):** Mỗi FR gắn permission cụ thể qua `@RequireAdminPermission` hoặc `AdminAuthorizationService.requirePermission(...)` trong use case.

## 3. Response – Success

`GET /admin/api/v1/me` (200):

```json
{
  "code": 200,
  "success": true,
  "message": "Authenticated admin profile",
  "data": {
    "admin_id": "uuid",
    "roles": ["MODERATOR"],
    "permissions": ["USER_SUSPEND"]
  },
  "errors": null,
  "timestamp": "..."
}
```

## 4. Response – Error

| HTTP | code (body) | Mô tả |
|------|-------------|--------|
| 401 | 401 | Thiếu/invalid JWT — `Authentication required` |
| 403 | 403 | JWT hợp lệ nhưng thiếu permission/role |
| 503 | 503 | `ADMIN-503` — Auth integration bật nhưng không verify được permission (fail closed) |

## 5. Business Rules

- Authentication (JWT) trước authorization (permission).
- `admin_id` chỉ lấy từ JWT subject/claims — không tin `admin_id` từ body.
- Hành động destructive cần **permission explicit** trong JWT (không chỉ role).
- Permission check chạy trước `@Transactional` / trước logic mutation (aspect hoặc đầu use case).
- Không log token/password/secret.
- `admin.integrations.auth.enabled=false` (mặc định): chỉ tin JWT claims.
- `admin.integrations.auth.enabled=true`: fallback gọi Auth `GET /api/v1/admin/users/{adminId}/permissions` khi claim thiếu permission.

## 6. Edge Cases

- Token hết hạn / sai secret → 401.
- Token hợp lệ nhưng không có claim `permissions` → 403 trên endpoint yêu cầu permission.
- Auth Service down khi integration bật → 503 (không cho phép mutation).
- Invalid Bearer prefix hoặc empty token → 401 trên protected route.

## 7. Data Dependencies

- Không ghi DB cho authorization thành công.
- Optional: `admin_action_logs` cho failed/critical access (future FR).

## 8. FE Integration Notes

- Admin login qua **Auth Service**; lưu access token theo policy FE.
- Gửi `Authorization: Bearer` cho mọi request Admin API.
- Dùng `GET /admin/api/v1/me` để hydrate menu theo `permissions`.
- Khi nhận 403, hiển thị thông báo thiếu quyền — không retry mutation.
- Permission codes tham chiếu: `USER_SUSPEND`, `PRODUCT_REMOVE`, `REVIEW_HIDE`, `SHOP_SUSPEND`, `SYSTEM_CONFIG_UPDATE`, `SYSTEM_CONFIG_VIEW`, `SYSTEM_ANNOUNCEMENT_CREATE`, `SYSTEM_ANNOUNCEMENT_UPDATE`, `SYSTEM_ANNOUNCEMENT_PUBLISH`, `SYSTEM_ANNOUNCEMENT_CANCEL`, `ADMIN_AUDIT_VIEW`, `ORDER_SUPPORT_READ`, …
