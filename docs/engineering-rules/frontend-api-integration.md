# Frontend API Integration Guide for Auth Service

Version: 1.0
Service: `auth-service`
Base path: `/api/v1`

---

## 1. Tong quan contract

Auth-service tra ve response wrapper thong nhat:

```json
{
  "code": 200,
  "success": true,
  "message": "Success",
  "data": {},
  "errors": null,
  "timestamp": "2026-05-17T09:00:00Z"
}
```

Frontend bat buoc:
- Parse payload tai `data`.
- Kiem tra `success` truoc khi dung payload.
- Hien thi `message` khi loi tong quat.
- Map `errors[]` cho field-level validation.

---

## 2. HTTP client setup

Khuyen nghi dung 1 client tap trung (`axios` hoac wrapper `fetch`):
- `baseURL`: env config
- Default headers:
  - `Content-Type: application/json`
  - `Authorization: Bearer <accessToken>` (neu co)
- Login request nen kem `X-Device-Id` neu app quan ly thiet bi.

Interceptors:
- Request:
  - attach access token
- Response:
  - neu `401`: thu refresh 1 lan
  - refresh fail: clear auth state + redirect login

---

## 3. Endpoint map (hien tai)

### 3.1 Public auth endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/forgot-password`

### 3.2 Authenticated endpoints

- `POST /api/v1/auth/change-password`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me/profile`
- `PATCH /api/v1/users/me/avatar`
- `PATCH /api/v1/users/me/privacy`
- `PATCH /api/v1/users/me/settings`
- `POST /api/v1/users/me/soft-delete`

Luu y:
- Security config yeu cau auth cho `/api/v1/users/me/**` va `POST /api/v1/auth/change-password`.

---

## 4. Request payload conventions

Mot so field backend dung `snake_case`:
- `confirm_password`
- `refresh_token`
- `current_password`
- `new_password`
- `confirm_new_password`
- `display_name`
- `social_links`
- `avatar_url`
- `is_private`
- `appearance_mode`

Rule:
- Frontend DTO gui len backend phai dung dung key contract.
- Internal UI model co the dung `camelCase`, nhung bat buoc map tai API layer.

---

## 5. Response typing conventions

De xuat shape response chung:

```js
const apiResponseShape = {
  code: 200,
  success: true,
  message: "Success",
  data: null,
  errors: null,
  timestamp: "2026-05-17T09:00:00Z"
};
```

Rule:
- Khong return raw axios response ra component.
- API layer tra du lieu da unwrap (chi tra `data` hoac object da map), khong de component parse wrapper.

---

## 6. Error handling strategy

Phan loai:
- `400/VALIDATION_ERROR`: hien field errors
- `401`: session het han/khong hop le -> refresh hoac logout
- `403`: khong co quyen/trang thai account khong hop le
- `404`: resource khong tim thay
- `409`: conflict (email trung, ...)
- `429`: rate limit (dang nhap/refresh/forgot password)
- `500`: loi he thong

UX rule:
- Khong show raw stack trace.
- Uu tien `errors[field]` > `message`.
- Rate limit nen hien countdown hoac goi y thu lai.

---

## 7. React Query integration pattern

### Query keys
- `authKeys.me`

### Mutations
- `useRegisterMutation`
- `useLoginMutation`
- `useRefreshTokenMutation`
- `useLogoutMutation`
- `useForgotPasswordMutation`
- `useChangePasswordMutation`
- `useUpdateProfileMutation`
- `useUpdateAvatarMutation`
- `useTogglePrivacyMutation`
- `useUpdateSettingsMutation`
- `useSoftDeleteMutation`

Post-success invalidation:
- Login/refresh/logout -> invalidate `me`
- Profile/avatar/privacy/settings -> invalidate `me`
- Soft delete -> clear auth cache va redirect safe screen

---

## 8. Authentication flow de xuat

### 8.1 Login
1. Submit email/password (+ optional device id)
2. Luu token theo auth policy
3. Goi `GET /users/me`
4. Cap nhat global auth state

### 8.2 Refresh
1. Gap `401` -> call `/auth/refresh`
2. Refresh success -> retry request cu
3. Refresh fail -> logout local + ve login

### 8.3 Logout
1. Call `/auth/logout` voi `refresh_token`
2. Clear auth state/cache
3. Redirect login

### 8.4 Account management
- Moi thay doi user account goi endpoint `/users/me/*`
- Sau update success phai refetch `me`.

---

## 9. Security notes cho frontend

- Khong log token/password vao console hoac analytics.
- Khong hardcode API secret trong source code.
- Gioi han retry voi endpoint auth de tranh bi coi la abuse.
- Bao dam header auth chi gui toi dung domain API.

---

## 10. Testing checklist

- Login success/fail
- Refresh success/fail + retry request
- Logout clear state dung cach
- Form validation map dung `errors[]`
- `429` handling cho login/forgot-password
- `GET /users/me` fail -> redirect login

