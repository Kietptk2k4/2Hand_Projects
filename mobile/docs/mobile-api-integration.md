# Mobile API Integration - 2Hands

Version: 1.0

---

## 1. Environment Variables

Copy `mobile/.env.example` to `mobile/.env` (gitignored).

| Variable | Service | Local emulator URL |
|----------|---------|-------------------|
| `EXPO_PUBLIC_AUTH_SERVICE_BASE_URL` | auth-service :3001 | `http://10.0.2.2:3001` |
| `EXPO_PUBLIC_COMMERCE_SERVICE_BASE_URL` | commerce-service :3003 | `http://10.0.2.2:3003` |
| `EXPO_PUBLIC_SOCIAL_SERVICE_BASE_URL` | social-service :3002 | `http://10.0.2.2:3002` |
| `EXPO_PUBLIC_NOTIFICATION_SERVICE_BASE_URL` | notification-service :3005 | `http://10.0.2.2:3005` |

**Physical device:** replace `10.0.2.2` with PC LAN IP (e.g. `192.168.1.4`). Phone and PC must be on same WiFi.

Restart Metro after `.env` changes: `npx expo start --clear`.

Native apps are not subject to browser CORS; backend `CORS_ALLOWED_ORIGINS` is for web only.

---

## 2. Response Envelope

All services return:

```json
{
  "code": 200,
  "success": true,
  "message": "...",
  "data": {},
  "errors": null,
  "timestamp": "..."
}
```

Use `unwrapResponse()` from `src/services/http/apiResponse.js`:

- Success: return `data`
- Failure: throw `{ code, message, errors }`

Use `mapAxiosError()` in catch blocks for HTTP errors.

---

## 3. Authentication

### Login

```http
POST {AUTH_BASE}/api/v1/auth/login
{ "email": "...", "password": "..." }
```

Response `data`: `access_token`, `refresh_token`, `user`.

Store tokens via `src/services/auth/tokenStorage.js` (SecureStore).

### Authorized requests

`authApiClient` attaches `Authorization: Bearer <access_token>`.

On `401`, `authRefreshService.refreshAccessTokenOnce()` calls:

```http
POST {AUTH_BASE}/api/v1/auth/refresh
{ "refresh_token": "..." }
```

### Logout

```http
POST {AUTH_BASE}/api/v1/auth/logout
{ "refresh_token": "..." }
```

Then `clearSessionTokens()` locally.

---

## 4. Service Clients (to add)

Mirror web clients in `frontend/src/fe-module/services/http/`:

| Client file (future) | Base URL env |
|---------------------|--------------|
| `commerceApiClient.js` | `EXPO_PUBLIC_COMMERCE_SERVICE_BASE_URL` |
| `socialApiClient.js` | `EXPO_PUBLIC_SOCIAL_SERVICE_BASE_URL` |
| `notificationApiClient.js` | `EXPO_PUBLIC_NOTIFICATION_SERVICE_BASE_URL` |

Each should reuse the same refresh interceptor pattern as `authApiClient.js`.

---

## 5. PayOS (commerce)

1. Checkout returns `payment_id`; `payos_checkout_url` may be null
2. `POST /commerce/api/v1/payments/{paymentId}/payos-checkout-url`
3. Open URL with `expo-web-browser` or `Linking.openURL`
4. Return via deep link `twohands://commerce/checkout/payment-result?paymentId=...`
5. Poll `GET /commerce/api/v1/payments/{paymentId}/status` — do not assume paid from redirect

---

## 6. Error UX

| HTTP | Mobile behavior |
|------|-----------------|
| 400 | Show field errors from `errors` array |
| 401 | Refresh token or redirect to login |
| 403 | Block action, show message |
| 429 | Rate limit message |
| 5xx | Generic retry message |

Never log passwords or tokens.

---

## 7. API Behavior Docs

Per-feature specs live in repo root:

`docs/api_fe_behavior/auth_api_fe_behavior/`  
`docs/api_fe_behavior/commerce_api_fe_behavior/`  
`docs/api_fe_behavior/social_api_fe_behavior/`  
`docs/api_fe_behavior/notification_api_fe_behavior/`

Read the relevant file before implementing any screen.
