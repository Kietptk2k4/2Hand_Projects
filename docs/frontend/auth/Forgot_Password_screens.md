# Forgot Password Screens Spec - 2Hands Frontend

Version: 1.0
Scope: Quy dinh implementation cho man hinh Forgot Password va cac state lien quan.

---

## 1. Muc tieu

Tai lieu nay la implementation-ready spec cho FE Forgot Password, duoc tong hop tu:
- `docs/engineering-rules/fe-master-context.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/engineering-rules/design-system.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/ForgotPassword -api-and-behavior.md`
- `frontend/stitch/forgot_password_screen/Forgot_Password_Design.md`
- `frontend/stitch/forgot_password_screen/Forgot_Password_Code.html`

Luu y ve naming asset:
- Trong thu muc Stitch hien co ten file `Forgot_Password_Design.md` va `Forgot_Password_Code.html`.
- Khong tim thay file anh `Forgot_Password_Screen.png` trong thu muc nay tai thoi diem tao tai lieu.

Uu tien khi conflict:
1. `fe-master-context.md`
2. `frontend-api-integration.md`
3. `frontend-convention.md`
4. `design-system.md`
5. `ForgotPassword -api-and-behavior.md`
6. Stitch design/code assets

---

## 2. Tech stack va quy tac implementation (bat buoc)

- React + Vite + JavaScript + Tailwind CSS
- Khong dung TypeScript (`.ts`, `.tsx`)
- Component file: `.jsx`
- API call khong dat trong JSX page/component
- API layer phai unwrap response wrapper truoc khi tra ve UI layer
- Khong log password/token/reset token

Response wrapper backend:

```json
{
  "code": 200,
  "success": true,
  "message": "Neu email hop le, chung toi da gui huong dan dat lai mat khau.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

---

## 3. Route map cho forgot-password module

- Forgot password page: `/auth/forgot-password`
- Login page: `/auth/login`
- Optional check-email page: `/auth/check-email` (neu team muon tach UX)

Auth rule:
- Forgot Password la public route.
- Khong can JWT de goi API.

---

## 4. UI/UX specification (theo design system + Stitch)

## 4.1 Layout tong quan

Theo Stitch HTML:
- Header mini voi brand `2Hands` (transactional layout, toi gian)
- Main card center (max width ~420-520px)
- Footer info links

Card content:
- Title: "Forgot Password"
- Subtitle huong dan nguoi dung nhap email
- 1 input email + submit CTA
- link "Back to Login"

## 4.2 Form controls bat buoc

- Single field: `email`
- Submit CTA: `Send reset link`
- Link quay lai login

## 4.3 State UX bat buoc

- Idle
- Inline validation
- Submitting/loading
- Success state (privacy-safe)
- Error state (`400`, `429`, `500`)

Luu y anti-enumeration:
- Neu email ton tai hay khong ton tai, UI success phai hien thong diep trung lap.
- Khong duoc thong bao "email khong ton tai".

## 4.4 Accessibility

- Dung semantic `form`, `label`, `button`, `input`
- Field loi: `aria-invalid=true` + `aria-describedby`
- Contrast dat WCAG AA
- Keyboard navigation day du

---

## 5. API contract cho forgot-password flow

Endpoint:
- `POST /api/v1/auth/forgot-password`
- Auth: Public

Request body:

```json
{
  "email": "user@example.com"
}
```

Validation backend:
- `email`: required, format hop le, max 255

Success:
- HTTP `200`
- anti-enumeration message neutral

Errors:
- `400`: invalid payload
- `429`: too many attempts
- `500`: internal error

---

## 6. Business flow va privacy rules

## 6.1 Main flow
1. User nhap email
2. FE validate client-side
3. Submit API `/api/v1/auth/forgot-password`
4. Neu `200`:
   - hien neutral success message
   - optional route sang `check-email` page
5. Neu `400/429/500`:
   - hien message theo mapping strategy

## 6.2 Anti-enumeration (bat buoc)

Frontend phai dam bao:
- Khong phan biet UI thong diep giua email ton tai va khong ton tai.
- Khong de user suy luan account existence tu text thong bao.

Message de xuat:
- "Neu email hop le, chung toi da gui huong dan dat lai mat khau."

---

## 7. Validation rules FE

Client-side:
- `email`: required
- format email hop le
- max length 255

Server-side:
- Neu co `errors[]` field-level -> bind vao field email
- Neu khong co field -> hien global message

---

## 8. Error mapping guideline

- `400`: field validation loi email
- `429`: "Ban thao tac qua nhanh, vui long thu lai sau."
- `500`: thong bao retry chung

Rule:
- Uu tien `errors[field]`
- Sau do moi hien `message`
- Khong show raw stack trace

---

## 9. Mapping tu Stitch code sang implementation

Can giu:
- Minimal top header + centered card
- Email field + CTA + Back to Login link
- Error banner/alert visual
- Footer section

Can chinh de dung logic project:
- CTA submit chi disabled khi loading hoac invalid
- Loading spinner chi hien khi submitting
- Success state phai dung anti-enumeration message neutral
- Link "Back to Login" phai dieu huong that den `/auth/login`

---

## 10. Component decomposition de xuat

Trong `features/auth`:
- `pages/ForgotPasswordPage.jsx`
- `components/ForgotPasswordForm.jsx`
- `components/AuthAlert.jsx`
- `schemas/authSchemas.js`
- `api/authApi.js`
- `hooks/useForgotPasswordFlow.js` (neu can)

Rule:
- UI component khong tu goi API truc tiep.
- Submit/error mapping flow dat trong hook/page container.

---

## 11. Integration voi MSW (dev)

Muc tieu:
- FE code full flow du backend chua hoan tat.

Can mock toi thieu:
- `POST /api/v1/auth/forgot-password`

Case de mock:
- Success `200` (email ton tai/khong ton tai deu same message)
- Invalid payload `400`
- Rate limit `429`
- Internal error `500`

---

## 12. Acceptance criteria (Definition of Done)

Forgot password screen duoc xem la hoan thanh khi:
- Dung UI theo Stitch + design system
- Dung API contract `/api/v1/auth/forgot-password`
- Ho tro day du state: loading/error/success/validation
- Hien thi dung privacy-safe success message
- Khong leak account existence qua UI thong bao
- Link "Back to Login" hoat dong den `/auth/login`
- Xu ly dung `400/429/500`
- Khong log sensitive values
- Pass lint/build trong project frontend

---

## 13. Prompt template de giao AI code Forgot Password

```txt
Implement Forgot Password screen based on:
- docs/frontend/Forgot_Password_screens.md
- docs/engineering-rules/fe-master-context.md
- docs/engineering-rules/frontend-convention.md
- docs/engineering-rules/design-system.md
- docs/engineering-rules/frontend-api-integration.md
- docs/api-FE_behavior/ForgotPassword -api-and-behavior.md
- frontend/stitch/forgot_password_screen/Forgot_Password_Design.md
- frontend/stitch/forgot_password_screen/Forgot_Password_Code.html

Requirements:
1) React + Vite + JavaScript + Tailwind only
2) No TypeScript
3) Implement full forgot-password flow with API integration
4) Keep anti-enumeration UX:
   - always show neutral success message on 200
5) Handle 400/429/500 with proper field/global messages
6) Keep feature-first structure under features/auth
7) Back to Login link routes to /auth/login

After coding:
- list created/updated files
- explain anti-enumeration and error handling flow
- self-check with acceptance criteria in docs/frontend/Forgot_Password_screens.md
```

