# Refresh Admin Token – API & Behavior

## 1. Business Goal

Cho phép admin lấy access token mới qua **Auth Service** khi token hết hạn, không lưu refresh token lifecycle trong Admin Service. JWT mới phản ánh **roles/permissions hiện tại** từ Auth RBAC.

## 2. API Contract

### 2.1 Luồng khuyến nghị (FE → Auth trực tiếp)

| Method | URL (Auth Service) | Auth |
|--------|-------------------|------|
| POST | `/api/v1/auth/admin/token/refresh` | Public |

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
  "message": "Lam moi access token admin thanh cong.",
  "data": {
    "access_token": "eyJ...",
    "expires_in": 900,
    "user": {
      "id": "uuid",
      "email": "admin@2hands.vn",
      "status": "ACTIVE"
    },
    "roles": ["ADMIN"],
    "permissions": ["ADMIN_ACCESS", "USER_SUSPEND"]
  }
}
```

**Không** trả `refresh_token` mới (session refresh token giữ nguyên cho đến khi logout/revoke).

### 2.2 Luồng tùy chọn (BFF trên Admin Service)

Cùng config gateway với login (`admin.auth.login.gateway-enabled=true` + `admin.integrations.auth.enabled=true`):

| Method | URL (Admin Service) | Auth |
|--------|----------------------|------|
| POST | `/admin/api/v1/auth/token/refresh` | Public |

## 3. Response – Error

| HTTP | Mô tả |
|------|--------|
| 401 | Refresh token invalid/expired/revoked |
| 403 | User suspended hoặc mất quyền admin portal (`AUTH-403-ADMIN-PORTAL`) |
| 429 | Rate limit refresh |
| 503 | Admin gateway bật nhưng Auth integration tắt / Auth down |

**Không** log `refresh_token` hoặc `access_token`.

## 4. Business Rules

- Refresh token phải map session `ACTIVE` trong Auth DB.
- Sau refresh, kiểm tra lại `AdminPortalAccessPolicy` (role admin hoặc `ADMIN_ACCESS`).
- Access token mới chứa `roles` + `permissions` cập nhật.
- Admin Service không persist refresh token.

## 5. FE Integration

1. Khi API Admin trả 401 (access expired), gọi refresh (Auth hoặc BFF).
2. Cập nhật stored access token; optional cập nhật `roles`/`permissions` từ response.
3. Retry request ban đầu một lần.
4. Refresh 401 → redirect login (session hết hạn).

## 6. Edge Cases

- Admin bị thu hồi role sau login → refresh 403.
- Account `SUSPENDED` → 403.
- Gateway tắt: FE gọi Auth `POST /api/v1/auth/admin/token/refresh` trực tiếp.

## 7. Data Dependencies

| Service | Storage |
|---------|---------|
| Auth Service | `refresh_token_sessions`, `user_roles`, … |
| Admin Service | — |

## 8. Related

- FR: `docs/feature_requirements/admin/FR_RefreshAdminToken.md`
- Login: `AdminLogin-api-and-behavior.md`
- Flow: `docs/business_flow/admin_business_flow/admin-auth-authorization-flow.md`
