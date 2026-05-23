# Admin Login – API & Behavior

## 1. Business Goal

Cho phép admin đăng nhập admin portal qua **Auth Service** (credential/session do Auth sở hữu). Admin Service **không** lưu password hay refresh token; sau login FE dùng access token cho các API `/admin/api/v1/**`.

## 2. API Contract

### 2.1 Luồng khuyến nghị (FE → Auth trực tiếp)

| Method | URL (Auth Service) | Auth |
|--------|-------------------|------|
| POST | `/api/v1/auth/admin/login` | Public |

**Request body:**

```json
{
  "email": "admin@2hands.vn",
  "password": "Password123!"
}
```

**Headers (optional):** `X-Device-Id`, `User-Agent` (Auth dùng cho session/login history).

**Success (200):**

```json
{
  "code": 200,
  "success": true,
  "message": "Dang nhap admin thanh cong.",
  "data": {
    "access_token": "eyJ...",
    "refresh_token": "opaque-refresh",
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

JWT access token chứa claims `roles` và `permissions` — Admin Service đọc qua `JwtTokenProvider` (`FR_AuthorizeAdminApi`).

### 2.2 Luồng tùy chọn (BFF gateway trên Admin Service)

Bật khi cần single origin / gateway:

| Config | Default |
|--------|---------|
| `admin.auth.login.gateway-enabled` | `false` |
| `admin.integrations.auth.enabled` | `false` |
| `admin.integrations.auth.base-url` | `http://localhost:3001` |

| Method | URL (Admin Service) | Auth |
|--------|----------------------|------|
| POST | `/admin/api/v1/auth/login` | Public (chỉ khi gateway bật) |

Request/response envelope giống chuẩn Admin API; body tương đương Auth (email, password). Admin Service proxy sang Auth — **không** ghi DB.

Env: `ADMIN_AUTH_LOGIN_GATEWAY_ENABLED`, `ADMIN_AUTH_INTEGRATION_ENABLED`, `ADMIN_AUTH_BASE_URL`.

## 3. Response – Error

| HTTP | Nguồn | Mô tả |
|------|--------|--------|
| 401 | Auth | Sai email/password (message chung, không lộ user tồn tại) |
| 403 | Auth | User hợp lệ nhưng không có admin role/permission (`AUTH-403-ADMIN-PORTAL`) |
| 403 | Auth | Tài khoản `SUSPENDED` |
| 429 | Auth | Rate limit đăng nhập |
| 503 | Admin | Gateway bật nhưng Auth integration tắt hoặc Auth không reachable (`ADMIN-503`) |

Admin Service **không** log password, access token, refresh token.

## 4. Business Rules

- Chỉ user có role admin (`ADMIN`, `MODERATOR`, `SUPPORT`, `SUPER_ADMIN`) **hoặc** permission `ADMIN_ACCESS` được cấp token admin portal.
- User thường (chỉ role `USER`) → 403.
- Admin Service không persist credential/session.
- `admin_id` cho API sau login = JWT `sub` (UUID user).
- Refresh/logout: Auth Service — xem `RefreshAdminToken-api-and-behavior.md`, `AdminLogout-api-and-behavior.md` — không qua admin_db.

## 5. FE Integration

1. Gọi `POST /api/v1/auth/admin/login` (hoặc BFF nếu bật gateway).
2. Lưu `access_token` / `refresh_token` theo security policy FE (httpOnly cookie hoặc secure storage).
3. Mọi request Admin API: `Authorization: Bearer <access_token>`.
4. Hydrate menu: `GET /admin/api/v1/me` hoặc đọc `permissions` từ login response.
5. 401 → redirect login; 403 admin portal → thông báo không đủ quyền.

## 6. Edge Cases

- **Gateway tắt (mặc định):** FE gọi Auth trực tiếp — không có route `/admin/api/v1/auth/login`.
- **Gateway bật, integration tắt:** `POST /admin/api/v1/auth/login` → 503 hướng dẫn bật `ADMIN_AUTH_INTEGRATION_ENABLED`.
- **JWT secret:** Admin Service và Auth Service dùng cùng `JWT_ACCESS_SECRET` để verify token.
- **MFA/OAuth:** Out of scope MVP — chỉ email/password qua Auth.

## 7. Data Dependencies

| Service | Storage | Ghi chú |
|---------|---------|---------|
| Auth Service | `users`, `user_roles`, `refresh_token_sessions`, `login_logs` | Own login |
| Admin Service | — | Không ghi DB cho login |

## 8. Related

- FR: `docs/feature_requirements/admin/FR_AdminLogin.md`
- Flow: `docs/business_flow/admin_business_flow/admin-auth-authorization-flow.md`
- Authorize: `AuthorizeAdminApi-api-and-behavior.md`
- Auth implementation: `POST /api/v1/auth/admin/login` trong auth-service
