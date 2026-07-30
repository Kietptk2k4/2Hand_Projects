# Access Token Invalidation – API & Behavior

## 1. Business Goal

Invalidate active JWT access tokens immediately when admin/user revokes all sessions, bans, suspends, changes password, or soft-deletes an account — not only refresh tokens in `refresh_token_sessions`.

Cross-browser/tab logout is enforced by:

1. **Redis marker** `auth:token:invalid-before:{userId}` (epoch milliseconds)
2. **JWT filter check** on auth, social, commerce, admin, notification services
3. **FE notification handler** for `USER_SUSPENDED` / `USER_BANNED` (immediate UI logout when poll receives enforcement notification)

## 2. Redis Contract

| Key | Value | TTL |
|-----|-------|-----|
| `auth:token:invalid-before:{userId}` | Epoch ms when invalidation started | `jwt.access-expiration` + 1 minute |

**Write:** Auth Service `RevokeAllUserSessionsService.revokeAll(userId)` after `revokeAllByUserId`.

**Read:** Each service `JwtAuthenticationFilter` rejects token when `token.iat < invalid-before`.

## 3. Triggers (Auth Service)

| Action | Invalidates access tokens |
|--------|---------------------------|
| Logout all sessions (`POST /users/me/sessions/logout-all`) | Yes |
| Admin suspend/ban (`ApplyUserEnforcement`) | Yes |
| Change password | Yes |
| Soft delete account | Yes |
| Assign/revoke role (force re-login) | Yes |
| Admin revoke all sessions | Yes |
| Single session logout / revoke one session | No (refresh only) |

## 4. API Behavior After Invalidation

Protected endpoints return **401 Unauthorized** (no authentication set) when JWT was issued before invalidation timestamp.

FE axios interceptor → refresh attempt → refresh fails (session revoked / user suspended) → `clearSession()` + session expired modal.

## 5. FE Integration

- Polling notifications (`NotificationBadgeContext`, 15s): `USER_SUSPENDED` / `USER_BANNED` → `clearSession()` + enforcement message immediately.
- Any API 401 after invalidation follows existing refresh-then-logout flow.

## 6. Operational Notes

- Requires **shared Redis** across auth + resource services (default `Infrastructure/docker-compose.yml`).
- Test profile uses `NoopAccessTokenInvalidationStore` / `NoopAccessTokenInvalidationChecker` (no Redis invalidation in unit tests).
- Access tokens issued **after** invalidation timestamp remain valid until natural expiry.

## 7. Related Docs

- `docs/api_fe_behavior/auth_api_fe_behavior/LogoutAllSesssion-api-and-behavior.md`
- `docs/api_fe_behavior/admin_api_fe_behavior/BanUser-api-and-behavior.md`
- `docs/api_fe_behavior/admin_api_fe_behavior/SuspendUser-api-and-behavior.md`
- `docs/business_flow/admin_business_flow/user-enforcement-flow.md`
