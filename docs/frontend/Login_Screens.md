# Login Screens Spec - 2Hands Frontend

Version: 1.0
Scope: Quy dinh implementation cho man hinh Login va cac state lien quan.

---

## 1. Muc tieu

Tai lieu nay la implementation-ready spec cho FE Login, duoc tong hop tu:
- `docs/engineering-rules/fe-master-context.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/engineering-rules/design-system.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/login-api-and-behavior.md`
- `frontend/Stitch/login_screen/LOGINSCREEN_DESIGN.md`
- `frontend/Stitch/login_screen/loginscreen.png`

Uu tien khi conflict:
1. `fe-master-context.md`
2. `frontend-api-integration.md`
3. `frontend-convention.md`
4. `design-system.md`
5. `login-api-and-behavior.md`
6. Stitch design docs

---

## 2. Tech stack va quy tac implementation (bat buoc)

- React + Vite + JavaScript + Tailwind CSS
- Khong dung TypeScript (`.ts`, `.tsx`)
- Component file: `.jsx`
- API call khong dat trong JSX page/component
- API layer phai unwrap response wrapper truoc khi tra ve UI layer

Response wrapper backend:

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

---

## 3. Route map cho login module

- Login page: `/auth/login`
- Register page: `/auth/register`
- Forgot password page: `/auth/forgot-password`
- Verify email page (redirect target): `/auth/verify-email` (neu project chua co thi tao placeholder route)
- OAuth callback success route: `/oauth/success`
- OAuth callback failure route: `/oauth/failure`
- Home/default authenticated route: `/`

Luu y:
- Neu co `redirectUrl` hop le trong query, uu tien redirect theo query sau login ACTIVE.
- Protected routes phai qua auth guard.

---

## 4. UI/UX specification (theo design system + Stitch)

## 4.1 Layout tong quan
- Form login nam center, max width 420-520px.
- Khoang cach field: 16px.
- CTA chinh full width tren mobile.
- Co khu vuc social login:
  - Continue with Google
  - Continue with Facebook
- Co link Forgot password va link sang Register.

## 4.2 Field bat buoc
- `email`
- `password` + toggle show/hide

## 4.3 State bat buoc tren screen
- Idle
- Inline validation
- Submitting (loading)
- API error state
- Success + redirect state

## 4.4 Accessibility
- Dung semantic `form`, `label`, `button`, `input`
- Field loi: `aria-invalid=true` + `aria-describedby`
- Tab order hop ly
- Contrast dat WCAG AA

---

## 5. API contract cho login flow

## 5.1 Email login

Endpoint:
- `POST /api/v1/auth/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

Headers:
- `Content-Type: application/json`
- optional `X-Device-Id`

Success 200 (rut gon):

```json
{
  "code": 200,
  "success": true,
  "message": "Dang nhap thanh cong.",
  "data": {
    "access_token": "...",
    "refresh_token": "...",
    "expires_in": 1800,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "status": "ACTIVE"
    }
  }
}
```

Error can ho tro:
- `400`: payload/format invalid
- `401`: wrong email/password (generic message)
- `403`: account suspended
- `429`: rate limit
- `500`: internal error

## 5.2 OAuth login

Start endpoints:
- `GET /oauth2/authorization/google`
- `GET /oauth2/authorization/facebook`

Frontend behavior:
- On click social button -> redirect browser den start endpoint
- Show loading overlay trong luc redirect
- Callback success/failure route xu ly finalize UI feedback

---

## 6. Business flow va redirect rules

## 6.1 Email login flow
1. Validate form client-side
2. Submit login API
3. Neu success:
   - luu `access_token`, `refresh_token`
   - cap nhat auth state
   - fetch `GET /api/v1/users/me` (khuyen nghi)
   - redirect:
     - neu `user.status = PENDING_VERIFICATION` -> `/auth/verify-email`
     - nguoc lai -> `redirectUrl` hop le hoac `/`
4. Neu fail:
   - map theo `code` va `errors[]`

## 6.2 Token va session policy
- Uu tien luu access token in-memory, co the fallback local storage theo policy hien tai.
- Refresh token quan ly tap trung, khong expose vao log.
- Khi gap `401` trong protected request:
  - thu refresh 1 lan qua `/api/v1/auth/refresh`
  - refresh fail -> clear session + redirect `/auth/login`

---

## 7. Validation rules

Client-side:
- `email`: required, format hop le, max 255
- `password`: required, non-empty

Server-side:
- Neu `errors[]` co field -> map vao field
- Neu khong co field -> hien global form error bang `message`

---

## 8. Error mapping guideline

- `400`: hien field/global validation message
- `401`: "Email hoac mat khau khong chinh xac."
- `403`: "Tai khoan hien khong kha dung."
- `429`: thong bao rate limit + de xuat thu lai sau
- `500`: thong bao chung, cho phep retry

Rule:
- Uu tien hien `errors[field]`
- Sau do moi hien `message`
- Khong show stack trace/raw error

---

## 9. Component decomposition de xuat

Trong `features/auth`:
- `pages/LoginPage.jsx`
- `components/LoginForm.jsx`
- `components/SocialLoginButtons.jsx`
- `components/AuthAlert.jsx`
- `schemas/authSchemas.js`
- `api/authApi.js`
- `hooks/useLoginFlow.js` (neu can)

Chuc nang tach rieng:
- UI component chi render + emit events
- Business flow login va redirect dat trong hook/service layer

---

## 10. Integration voi MSW (dev)

Muc tieu:
- FE code full flow du backend chua san sang.

Can mock toi thieu:
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/users/me`
- OAuth redirect behavior (mock callback route UX)

Test data de xuat:
- Active user
- Pending verification user
- Invalid credential case (`401`)
- Rate limit case (`429`)

---

## 11. Acceptance criteria (Definition of Done)

Login screen duoc xem la hoan thanh khi:
- Dung UI theo Stitch + design system
- Dung API contract wrapper, khong parse raw response trong component
- Ho tro email login + social redirect buttons
- Ho tro day du state: loading/error/success/validation
- Redirect dung theo `user.status` va `redirectUrl`
- Xu ly `400/401/403/429/500` dung message strategy
- Co route guard cho protected routes
- Khong log password/token
- Pass lint/build trong project frontend

---

## 12. Prompt template de giao AI code Login

```txt
Implement Login screen based on:
- docs/frontend/login_screens.md
- docs/engineering-rules/fe-master-context.md
- docs/engineering-rules/frontend-convention.md
- docs/engineering-rules/design-system.md
- docs/engineering-rules/frontend-api-integration.md
- docs/api-FE_behavior/login-api-and-behavior.md
- frontend/Stitch/login_screen/LOGINSCREEN_DESIGN.md
- frontend/Stitch/login_screen/loginscreen.png

Requirements:
1) React + Vite + JavaScript + Tailwind only
2) No TypeScript
3) Build full login flow with API integration and error mapping
4) Handle status redirect:
   - PENDING_VERIFICATION -> /auth/verify-email
   - ACTIVE -> redirectUrl or /
5) Include social login redirects to:
   - /oauth2/authorization/google
   - /oauth2/authorization/facebook
6) Keep code feature-first in features/auth

After coding:
- list created/updated files
- explain redirect and error handling flow
- self-check with acceptance criteria in docs/frontend/login_screens.md
```

