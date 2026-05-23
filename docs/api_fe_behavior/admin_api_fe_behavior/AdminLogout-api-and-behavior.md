# Admin Logout – API & Behavior

## 1. Business Goal

Cho phép admin đăng xuất khỏi admin portal bằng cách revoke refresh session qua **Auth Service**. Admin Service không lưu session state.

## 2. API Contract

### 2.1 Luồng khuyến nghị (FE → Auth trực tiếp)

| Method | URL (Auth Service) | Auth |
|--------|-------------------|------|
| POST | `/api/v1/auth/admin/logout` | **Bearer required** |

**Headers:**

```
Authorization: Bearer <access_token>
```

**Request body:**

```json
{
  "refresh_token": "opaque-refresh-from-login"
}
```

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Dang xuat admin thanh cong.",
  "data": null
}
```

Sau logout, refresh token **không** dùng được cho `POST /api/v1/auth/admin/token/refresh`. Access token có thể còn hiệu lực đến khi hết hạn (MVP không blacklist access token).

### 2.2 Luồng tùy chọn (BFF trên Admin Service)

`admin.auth.login.gateway-enabled=true` + `admin.integrations.auth.enabled=true`:

| Method | URL (Admin Service) | Auth |
|--------|----------------------|------|
| POST | `/admin/api/v1/auth/logout` | **Bearer required** |

Admin Service xác thực JWT, forward Bearer + `refresh_token` sang Auth.

## 3. Response – Error

| HTTP | Mô tả |
|------|--------|
| 401 | Thiếu/invalid JWT |
| 403 | Refresh token thuộc user khác (không được logout session người khác) |
| 503 | Gateway bật nhưng Auth integration tắt / Auth down |

**Không** log `refresh_token` hoặc `access_token`.

## 4. Business Rules

- Chỉ logout session **của chính admin** (JWT `sub` khớp `refresh_token_sessions.user_id`).
- Session không tồn tại hoặc đã logout → **idempotent success** (200).
- Auth Service cập nhật `refresh_token_sessions` → `LOGGED_OUT`.
- Admin Service không ghi DB.

## 5. FE Integration

1. Gửi `Authorization: Bearer` + `refresh_token` trong body.
2. Xóa access/refresh token khỏi storage (client-side).
3. Redirect về trang login.
4. Không gọi refresh sau logout.

## 6. Edge Cases

- **Double logout:** 200, không lỗi.
- **Gateway tắt:** FE gọi Auth trực tiếp.
- **Access token còn hạn:** API Admin vẫn có thể 401 sau khi refresh fail — FE nên xóa token local ngay.

## 7. Data Dependencies

| Service | Storage |
|---------|---------|
| Auth Service | `refresh_token_sessions` |
| Admin Service | — |

## 8. Related

- FR: `docs/feature_requirements/admin/FR_AdminLogout.md`
- Login: `AdminLogin-api-and-behavior.md`
- Refresh: `RefreshAdminToken-api-and-behavior.md`
- Flow: `docs/business_flow/admin_business_flow/admin-auth-authorization-flow.md`
