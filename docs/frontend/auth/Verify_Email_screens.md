# Verify Email Screens Spec - 2Hands Frontend

Version: 1.0
Scope: Quy dinh implementation cho man hinh Verify Email va cac state lien quan.

---

## 1. Muc tieu

Tai lieu nay la implementation-ready spec cho FE Verify Email, duoc tong hop tu:
- `docs/engineering-rules/fe-master-context.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/engineering-rules/design-system.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/Verify-api-and-behavior.md`
- `frontend/stitch/verify_email_screen/Verify_Email_Design.md`
- `frontend/stitch/verify_email_screen/Verify_Email_Code.html`

Luu y ve naming asset:
- Trong thu muc Stitch hien co file `Verify_Email_Design.md` va `Verify_Email_Code.html`.
- Khong tim thay file anh `Verify_Email_Screen.png` trong thu muc tai thoi diem tao tai lieu.

Uu tien khi conflict:
1. `fe-master-context.md`
2. `frontend-api-integration.md`
3. `frontend-convention.md`
4. `design-system.md`
5. `Verify-api-and-behavior.md`
6. Stitch design/code assets

---

## 2. Tech stack va quy tac implementation (bat buoc)

- React + Vite + JavaScript + Tailwind CSS
- Khong dung TypeScript (`.ts`, `.tsx`)
- Component file: `.jsx`
- API call khong dat trong JSX page/component
- API layer unwrap response wrapper truoc khi tra ve UI layer
- Khong log token xac thuc hoac du lieu nhay cam

Response wrapper backend (success):

```json
{
  "code": 200,
  "success": true,
  "message": "Xac thuc email thanh cong.",
  "data": {
    "user_id": "uuid-1234-5678",
    "email_verified": true,
    "status": "ACTIVE"
  },
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

---

## 3. Route map cho verify-email module

- Verify email page: `/auth/verify-email`
- Login page: `/auth/login`
- Home page: `/` (neu product quyet dinh redirect ve home sau verify)

Auth rule:
- Verify email la public route.
- Khong can JWT de xac thuc token verify.

---

## 4. UI/UX specification (theo design system + Stitch)

## 4.1 Layout tong quan

Theo Stitch HTML:
- Trang mang tinh chat transactional, toi gian nav/footer.
- Card center (max width ~560px).
- Phan visual tren (illustration + icon mail verification).
- Content ben duoi:
  - title + subtitle
  - alert area (error state example)
  - form nhap ma token
  - helper links (Resend code / Contact support)

## 4.2 Form controls bat buoc

- Input token:
  - field key backend: `token`
  - UI co the hien label "6-Digit Code" neu dung OTP format
- CTA chinh: `Verify Email`
- Optional helper actions:
  - Resend Code (placeholder neu API chua co)
  - Contact Support

## 4.3 State UX bat buoc

- Idle
- Inline validation
- Submitting/loading
- Success state (toast/banner + redirect)
- Error state:
  - invalid/expired token (`400`)
  - server error (`500`)

## 4.4 Accessibility

- Dung semantic `form`, `label`, `button`, `input`
- `aria-invalid=true` + `aria-describedby` cho field loi
- Keyboard submit support (Enter)
- Contrast dat WCAG AA

---

## 5. API contract cho verify-email flow

Endpoint:
- `POST /api/v1/auth/verify-email`
- Auth: Public

Request body:

```json
{
  "token": "verify_token_or_otp"
}
```

Validation backend:
- `token`: required, non-empty

Success:
- HTTP `200`
- `email_verified = true`
- `status = ACTIVE`

Errors:
- `400`: token invalid/expired/used
- `500`: internal error

Example 400:

```json
{
  "code": 400,
  "success": false,
  "message": "Token xac thuc khong hop le hoac da het han.",
  "data": null,
  "errors": [
    { "field": "token", "reason": "INVALID_OR_EXPIRED" }
  ],
  "timestamp": "2026-05-15T10:00:00Z"
}
```

---

## 6. Business flow va redirect rules

## 6.1 Main flow
1. User nhap token/OTP
2. FE validate token format co ban (khong rong, co the constrain do dai)
3. Submit `/api/v1/auth/verify-email`
4. Neu success `200`:
   - hien success message
   - redirect `/auth/login` (mac dinh theo spec)
   - hoac redirect `/` neu product decision da chot khac
5. Neu error:
   - `400`: giu user o verify page + hien thong diep invalid/expired
   - `500`: hien retry message

## 6.2 Idempotency note

Theo spec backend co recommendation idempotent cho truong hop da verify.
Frontend can:
- Khong crash neu nhan success message an toan cho token da dung truoc do.
- Van dua user ve login/home theo policy redirect.

---

## 7. Validation rules FE

Client-side:
- `token`: required
- optional: regex/dai token theo implementation (6-digit OTP hoac token string)

Server-side:
- Neu co `errors[]` field-level -> bind vao input token
- Neu khong co field -> hien global message tu `message`

---

## 8. Error mapping guideline

- `400`: "Token khong hop le hoac da het han."
- `500`: thong bao chung + cho phep retry

Rule:
- Uu tien `errors[field]` neu co
- Sau do moi hien `message`
- Khong show stack trace/noi dung internal

---

## 9. Mapping tu Stitch code sang implementation

Can giu:
- card center + visual header minh hoa
- title/subtitle huong dan ro rang
- input token + button verify
- helper links "Resend Code", "Contact Support"

Can chinh de dung logic project:
- Field submit backend phai la `token` (khong dung key UI thuong goi `verification-code` trong payload).
- Link `Resend Code` chi la optional; neu API chua co thi disable hoac open support flow.
- Error alert trong demo stitch la mock state; implementation can render conditionally theo response.

---

## 10. Component decomposition de xuat

Trong `features/auth`:
- `pages/VerifyEmailPage.jsx`
- `components/VerifyTokenForm.jsx`
- `components/VerifyStatusAlert.jsx`
- `components/VerifyHeroVisual.jsx`
- `schemas/authSchemas.js`
- `api/authApi.js`
- `hooks/useVerifyEmailFlow.js` (neu can)

Rule:
- UI component khong goi API truc tiep.
- Submit/redirect logic dat trong hook/page container.

---

## 11. Integration voi MSW (dev)

Muc tieu:
- FE code full flow du backend verify-email endpoint chua expose that.

Can mock toi thieu:
- `POST /api/v1/auth/verify-email`

Case de mock:
- success `200`
- invalid/expired `400`
- server error `500`

---

## 12. Acceptance criteria (Definition of Done)

Verify email screen duoc xem la hoan thanh khi:
- Dung UI theo Stitch + design system
- Dung API contract `/api/v1/auth/verify-email`
- Co loading/error/success/validation states
- Handle dung `400/500`
- On success redirect theo policy da chot (mac dinh `/auth/login`)
- Khong log token verify
- Pass lint/build trong project frontend

---

## 13. Prompt template de giao AI code Verify Email

```txt
Implement Verify Email screen based on:
- docs/frontend/Verify_Email_screens.md
- docs/engineering-rules/fe-master-context.md
- docs/engineering-rules/frontend-convention.md
- docs/engineering-rules/design-system.md
- docs/engineering-rules/frontend-api-integration.md
- docs/api-FE_behavior/Verify-api-and-behavior.md
- frontend/stitch/verify_email_screen/Verify_Email_Design.md
- frontend/stitch/verify_email_screen/Verify_Email_Code.html

Requirements:
1) React + Vite + JavaScript + Tailwind only
2) No TypeScript
3) Implement full verify-email flow with API integration
4) Submit payload must use key `token`
5) Handle 400/500 with proper UI alerts
6) On success 200: show success then redirect to /auth/login
7) Keep feature-first structure under features/auth

After coding:
- list created/updated files
- explain token validation and redirect logic
- self-check with acceptance criteria in docs/frontend/Verify_Email_screens.md
```

