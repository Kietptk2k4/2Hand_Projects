# Frontend Convention for Auth Service

Version: 1.0
Scope: Frontend modules that integrate with `auth-service` APIs.

---

## 1. Muc tieu

Tai lieu nay quy dinh convention frontend de:
- dong bo voi contract cua `auth-service`
- giam bug khi tich hop auth flow
- de review, de onboarding

---

## 1.1 File encoding (Windows)

Mọi file frontend (`*.js`, `*.jsx`, `*.ts`, `*.tsx`, `*.md`) phải là **UTF-8 without BOM**.

UTF-16 (thường do agent/editor trên Windows) gây `SyntaxError: Invalid or unexpected token` ở dòng 1 khi Vite load module.

Chi tiết: `docs/engineering_rules/file-encoding-standards.md`  
Cursor rule: `.cursor/rules/file-encoding-utf8.mdc`

---

## 2. Cau truc thu muc de xuat

```txt
2HAND_PROJECTS/Frontend/
├── app/
│   ├── providers/
│   ├── router/
│   └── store/
├── features/
│   └── auth/
│       ├── api/
│       ├── hooks/
│       ├── pages/
│       ├── components/
│       └── schemas/
├── shared/
│   ├── ui/
│   ├── lib/
│   └── constants/
└── services/
    └── http/
```

Rule:
- Khong goi API truc tiep trong page/component.
- Moi endpoint auth phai di qua `features/auth/api`.
- Logic parse response/error dat trong service hook, khong dat trong JSX.

---

## 3. Naming convention

- Component: `PascalCase` (`LoginForm`, `AccountSettingsCard`)
- Hook: `useSomething` (`useLoginMutation`, `useMeQuery`)
- Function/variable: `camelCase`
- Constant: `UPPER_SNAKE_CASE`
- File:
  - component: `PascalCase.jsx`
  - hook/service: `kebab-case` hoac `camelCase`, nhung phai dong nhat trong module

---

## 4. API va data mapping convention

`auth-service` su dung:
- URL base: `/api/v1/auth` va `/api/v1/users/me`
- Response wrapper:

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
- Doc du lieu tu `response.data.data`, khong doc truc tiep root.
- Su dung response wrapper object nhat quan (`code`, `success`, `message`, `data`, `errors`, `timestamp`).
- Chuyen field `snake_case` tu backend sang model frontend neu can, nhung map tai API layer.
  - Vi du: `refresh_token`, `confirm_password`, `appearance_mode`.

---

## 5. Auth state va token convention

- Access token luu in-memory (uu tien), hoac storage theo policy cua app.
- Refresh token:
  - Neu backend tra body token: luu va quan ly tap trung.
  - Neu doi sang cookie trong tuong lai: khong doi contract UI logic page.
- Moi request can auth phai inject `Authorization: Bearer <access_token>`.
- Header `X-Device-Id` phai duoc gui khi login neu app co dinh danh device.

---

## 6. Data fetching convention

Khuyen nghi dung React Query:
- Query:
  - `authKeys.me()`
- Mutation:
  - `login`, `register`, `refreshToken`, `logout`, `forgotPassword`, `changePassword`
  - `updateProfile`, `updateAvatar`, `togglePrivacy`, `updateSettings`, `softDelete`

Rule:
- Sau login/refresh success: cap nhat token + invalidate `me`.
- Sau update user setting/profile success: invalidate `me`.
- Khong viet `fetch` trong `useEffect` de thay the cho query library.

---

## 7. Validation convention

- Form validation chia 2 lop:
  - client-side (required, format, min/max)
  - server-side (errors tu API)
- Field map theo backend contract:
  - `email`, `password`, `confirm_password`
  - `current_password`, `new_password`, `confirm_new_password`
  - `display_name`, `avatar_url`, `is_private`, `appearance_mode`
- Error backend `errors[]` phai map dung vao field UI.

---

## 8. Error handling convention

- Neu `success = false`, UI phai uu tien hien:
  1. `errors[field]` cho field-level
  2. `message` cho form/global
- Neu `401`: trigger flow refresh token (neu app dang ky), fail thi logout.
- Neu `403`: hien thong bao khong du quyen.
- Neu `429`: hien thong bao rate limit than thien, co cooldown neu can.

---

## 9. Route guard convention

- Public pages:
  - Login, Register, Forgot Password, OAuth callback
- Protected pages:
  - Profile, Account Settings, Security Settings
- Rule:
  - Chua co token -> redirect login
  - Co token nhung `me` fail 401 -> clear session + redirect login

---

## 10. Testing convention

- Unit test:
  - mapper API response
  - auth reducer/store
  - custom hooks
- Integration test (frontend):
  - login -> me -> logout
  - update profile/settings flow
  - xu ly 401/403/429
- Mock API bang MSW de kiem tra contract response wrapper.

---

## 11. Logging va security tren frontend

- Khong log:
  - password
  - refresh token
  - access token
- Khong hardcode secret/env trong source.
- Retry policy co gioi han, tranh spam endpoint auth.

