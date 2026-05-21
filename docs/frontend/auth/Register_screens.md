# Register Screens Spec - 2Hands Frontend

Version: 1.0
Scope: Quy dinh implementation cho man hinh Register va cac state lien quan.

---

## 1. Muc tieu

Tai lieu nay la implementation-ready spec cho FE Register, duoc tong hop tu:
- `docs/engineering-rules/fe-master-context.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/engineering-rules/design-system.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/register-api-and-behavior.md`
- `frontend/stitch/register_screen/Register_Screen_Design.md`
- `frontend/stitch/register_screen/Register_Screen.png`
- `frontend/stitch/register_screen/Register_Screen_Code.html`

Uu tien khi conflict:
1. `fe-master-context.md`
2. `frontend-api-integration.md`
3. `frontend-convention.md`
4. `design-system.md`
5. `register-api-and-behavior.md`
6. Stitch design docs/assets

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
  "code": 201,
  "success": true,
  "message": "Dang ky thanh cong. Vui long kiem tra email de xac thuc.",
  "data": {
    "user_id": "uuid",
    "email": "user@example.com",
    "status": "PENDING_VERIFICATION"
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

---

## 3. Route map cho register module

- Register page: `/auth/register`
- Login page: `/auth/login`
- Verify email page (target sau register success): `/auth/verify-email`
- OAuth callback success route: `/oauth/success`
- OAuth callback failure route: `/oauth/failure`

Luu y:
- Register la public route.
- Protected route guard khong ap dung cho register page.

---

## 4. UI/UX specification (theo design system + Stitch)

## 4.1 Layout tong quan

Theo `Register_Screen.png` va `Register_Screen_Code.html`:
- Desktop:
  - 2 cot:
    - Left conceptual/branding area (chi hien desktop)
    - Right form area
  - Left area co:
    - brand "2Hands"
    - slogan
    - imagery/hero
    - community info card
- Mobile/tablet:
  - chi hien form area (left panel an)
  - brand header rut gon o tren

## 4.2 Form va controls
- Field bat buoc:
  - `email`
  - `password`
  - `confirm_password`
- CTA chinh: `Dang ky`
- Divider: "Hoac tiep tuc voi"
- Social buttons:
  - Continue with Google
  - Continue with Facebook
- Footer hint:
  - "Da co tai khoan? Dang nhap ngay"

## 4.3 State UX bat buoc
- Idle
- Inline validation
- Submitting/loading (disable inputs + button)
- Success banner sau register 201
- Error state theo ma loi API

## 4.4 Accessibility
- Dung semantic `form`, `label`, `button`, `input`
- Field loi: `aria-invalid=true` + `aria-describedby`
- Thu tu tab hop ly
- Contrast dat WCAG AA

---

## 5. API contract cho register flow

## 5.1 Email register

Endpoint:
- `POST /api/v1/auth/register`

Request:

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "confirm_password": "Password123!"
}
```

Validation backend:
- `email`: required, format, max 255
- `password`: required, 8-32 chars, uppercase + lowercase + number
- `confirm_password`: phai trung `password`

Success:
- HTTP `201`
- `status` trong `data` se la `PENDING_VERIFICATION`

Errors:
- `400`: invalid input/validation
- `409`: duplicate email
- `429`: rate limit
- `500`: internal error

## 5.2 OAuth entry from register screen

Start endpoints:
- `GET /oauth2/authorization/google`
- `GET /oauth2/authorization/facebook`

Frontend behavior:
- On click social button -> redirect browser den start endpoint
- Show loading overlay trong luc redirect
- OAuth callback route xu ly success/failure message

---

## 6. Business flow va redirect rules

## 6.1 Email register flow
1. Validate client-side
2. Submit register API
3. Neu success 201:
   - hien success message
   - redirect `/auth/verify-email` (hoac Verify OTP screen neu project map chung)
4. Neu fail:
   - map `errors[]` cho field-level
   - map `message` cho global alert

## 6.2 OAuth from register
1. User click Google/Facebook
2. Redirect sang OAuth endpoint backend
3. Backend callback + redirect ve FE success/failure route
4. FE finalize auth state tai callback pages

---

## 7. Validation rules FE

Client-side:
- `email`: required, email format, max 255
- `password`: required, 8-32, uppercase + lowercase + number
- `confirm_password`: required, match `password`

Server-side:
- Neu `errors[]` co field -> bind dung field
- Neu khong co field -> hien global message

UX rule:
- Validate on blur + on submit
- Disable register button khi form invalid hoac dang submit

---

## 8. Error mapping guideline

- `400`: hien validation field/global
- `409`: "Email da duoc su dung."
- `429`: "Ban thao tac qua nhanh, vui long thu lai sau."
- `500`: thong bao chung, cho phep retry

Rule:
- Uu tien `errors[field]`
- Sau do moi hien `message`
- Khong show raw exception

---

## 9. Mapping tu Stitch code sang implementation

Can giu:
- 2-column desktop layout
- tone mau va spacing cua design system
- social buttons + divider + login redirect text
- loading/success visual feedback

Can chinh lai cho dung logic:
- Trong `Register_Screen_Code.html`, text nut loading dang la `Dang xuat...`
- FE implementation phai sua thanh `Dang ky...`
- Link login trong HTML dang disabled; trong app that phai la link dieu huong hoat dong den `/auth/login`

---

## 10. Component decomposition de xuat

Trong `features/auth`:
- `pages/RegisterPage.jsx`
- `components/RegisterForm.jsx`
- `components/SocialRegisterButtons.jsx`
- `components/RegisterSuccessBanner.jsx`
- `components/AuthPanelHero.jsx` (left desktop panel)
- `schemas/authSchemas.js`
- `api/authApi.js`
- `hooks/useRegisterFlow.js` (neu can)

Rule:
- UI component thuong khong chua business logic API.
- Redirect/submit state dat trong hook or page container.

---

## 11. Integration voi MSW (dev)

Muc tieu:
- FE code full flow du backend chua hoan tat.

Can mock toi thieu:
- `POST /api/v1/auth/register`
- OAuth redirect behavior (co the mock callback route UX)

Test data/case de xuat:
- Register success `201`
- Duplicate email `409`
- Rate limit `429`
- Generic error `500`

---

## 12. Acceptance criteria (Definition of Done)

Register screen duoc xem la hoan thanh khi:
- Dung UI theo Stitch + design system
- Dung API contract wrapper
- Ho tro register email/password + social OAuth entry
- Ho tro state day du: loading/error/success/validation
- Redirect dung sang verify flow sau `201`
- Xu ly dung `400/409/429/500`
- Link "Dang nhap ngay" hoat dong den `/auth/login`
- Khong log password/token
- Pass lint/build trong project frontend

---

## 13. Prompt template de giao AI code Register

```txt
Implement Register screen based on:
- docs/frontend/Register_screens.md
- docs/engineering-rules/fe-master-context.md
- docs/engineering-rules/frontend-convention.md
- docs/engineering-rules/design-system.md
- docs/engineering-rules/frontend-api-integration.md
- docs/api-FE_behavior/register-api-and-behavior.md
- frontend/stitch/register_screen/Register_Screen_Design.md
- frontend/stitch/register_screen/Register_Screen.png
- frontend/stitch/register_screen/Register_Screen_Code.html

Requirements:
1) React + Vite + JavaScript + Tailwind only
2) No TypeScript
3) Build full register flow with API integration and error mapping
4) On success 201 -> show success message then redirect /auth/verify-email
5) Include social OAuth redirects:
   - /oauth2/authorization/google
   - /oauth2/authorization/facebook
6) Keep feature-first structure in features/auth
7) Fix Stitch typo button text from "Dang xuat..." to "Dang ky..."

After coding:
- list created/updated files
- explain validation, error handling, redirect flow
- self-check with acceptance criteria in docs/frontend/Register_screens.md
```

