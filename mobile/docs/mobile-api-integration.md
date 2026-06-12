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

## 4. Service Clients

| Client file | Base URL env | Status |
|-------------|--------------|--------|
| `authApiClient.js` | `EXPO_PUBLIC_AUTH_SERVICE_BASE_URL` | Exists |
| `socialApiClient.js` | `EXPO_PUBLIC_SOCIAL_SERVICE_BASE_URL` | Exists |
| `commerceApiClient.js` | `EXPO_PUBLIC_COMMERCE_SERVICE_BASE_URL` | Phase 0 — create for commerce module |
| `notificationApiClient.js` | `EXPO_PUBLIC_NOTIFICATION_SERVICE_BASE_URL` | Defer |

### commerceApiClient

Mirror `frontend/src/fe-module/services/http/commerceApiClient.js`:

- Base URL: `EXPO_PUBLIC_COMMERCE_SERVICE_BASE_URL` (local emulator: `http://10.0.2.2:3003`)
- All commerce paths prefixed **`/commerce/api/v1`** (e.g. `GET /commerce/api/v1/products`)
- Same JWT attach + 401 refresh interceptor pattern as `authApiClient.js`
- Unwrap via `src/features/commerce/api/commerceApiResponse.js` (port from web)

```javascript
// Example
const { data } = await commerceApiClient.get("/commerce/api/v1/products", {
  params: { page: 0, size: 20 },
});
```



---

## 5. PayOS (commerce checkout)

Commerce-service port **3003**. Payment endpoints under `/commerce/api/v1/payments/`.

**Flow:**

1. Checkout `CreateOrder` returns `payment_id`; initial response may have `payos_checkout_url: null`
2. `POST /commerce/api/v1/payments/{paymentId}/payos-checkout-url` → open URL
3. Mobile: `expo-web-browser` `openBrowserAsync(payosUrl)` (prefer over raw `Linking.openURL` for return handling)
4. Return via deep link: `twohands://commerce/checkout/payment-result?paymentId={uuid}`
5. **Poll** `GET /commerce/api/v1/payments/{paymentId}/status` until terminal state — do not assume paid from browser dismiss alone
6. On success → navigate to checkout success or order detail

Register `twohands` scheme in `app.json` linking config.

Retry: when status allows, call payos-checkout-url again (see web `CommerceCheckoutPaymentResultPage`).

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

---

## 8. Dev media URLs (`EXPO_PUBLIC_DEV_HOST`)

Product and review images from MinIO may use `localhost` or `127.0.0.1` — unreachable from a physical phone.

| Variable | Purpose |
|----------|---------|
| `EXPO_PUBLIC_DEV_HOST` | PC LAN IP (no protocol), e.g. `192.168.1.4` — rewrites media host in `resolveDevMediaUrl()` |

Set in `mobile/.env` alongside service base URLs. Android emulator uses `10.0.2.2` for API URLs; media rewrite uses the same helper (`mobile/src/shared/utils/resolveDevMediaUrl.js`).

**Rule:** pass every commerce product/review image URL through `resolveDevMediaUrl()` before `<Image />`.

