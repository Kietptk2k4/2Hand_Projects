# Change Password Screens Spec - 2Hands Frontend

Version: 1.0
Scope: Quy dinh implementation cho man hinh Change Password va flow lien quan.

---

## 1. Muc tieu

Tai lieu nay la implementation-ready spec cho FE Change Password, duoc tong hop tu:
- `docs/engineering-rules/fe-master-context.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/engineering-rules/design-system.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/ChangePassword-api-and-behavior.md`
- `frontend/stitch/change_password_screen/Change_Password_Screens_Design.md`
- `frontend/stitch/change_password_screen/Change_Password_Screens_Code.html`

Luu y ve naming asset Stitch:
- Tai thu muc Stitch hien co file dang so nhieu: `Change_Password_Screens_*`.
- Tai lieu nay van dung ten target output theo yeu cau: `Change_Password_screens.md`.

Uu tien khi conflict:
1. `fe-master-context.md`
2. `frontend-api-integration.md`
3. `frontend-convention.md`
4. `design-system.md`
5. `ChangePassword-api-and-behavior.md`
6. Stitch design/code assets

---

## 2. Tech stack va quy tac implementation (bat buoc)

- React + Vite + JavaScript + Tailwind CSS
- Khong dung TypeScript (`.ts`, `.tsx`)
- Component file: `.jsx`
- API call khong dat trong JSX page/component
- API layer unwrap response wrapper truoc khi tra ve UI layer
- Khong log password/token

Response wrapper backend:

```json
{
  "code": 200,
  "success": true,
  "message": "Doi mat khau thanh cong.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

---

## 3. Route map cho change-password module

- Change password page: `/account/change-password` (hoac route account security tuong duong)
- Login page: `/auth/login`
- Account settings page: `/account` (optional back navigation)

Auth rule:
- Change password la protected screen (can JWT/session hop le).
- Neu `401` tu API -> redirect `/auth/login`.

---

## 4. UI/UX specification (theo design system + Stitch)

## 4.1 Layout tong quan

Theo Stitch HTML:
- Co top navigation va footer theo layout tong the app.
- Main card center:
  - title: "Doi mat khau"
  - subtitle security-oriented
  - form 3 truong mat khau
- Card style:
  - border + rounded + subtle elevation
  - spacing theo scale 8/16/24

## 4.2 Form controls bat buoc

- `current_password`
- `new_password`
- `confirm_new_password`
- Moi field:
  - input password
  - toggle show/hide password
- Password requirement helper/checklist cho `new_password`

## 4.3 CTA va states

- Primary CTA: `Doi mat khau`
- States:
  - idle
  - validating
  - submitting/loading (disable form + spinner)
  - success
  - error (field/global)

Luu y tu Stitch:
- Button loading text trong HTML la `Changing Password...`
- Ban production co the giu EN hoac doi VN, nhung phai nhat quan voi copy strategy.

## 4.4 Accessibility

- Dung semantic `form`, `label`, `button`
- `aria-invalid=true` + `aria-describedby` cho field loi
- Keyboard friendly cho toggle icon
- Contrast dat WCAG AA

---

## 5. API contract cho change password flow

Endpoint:
- `POST /api/v1/auth/change-password`
- Auth: Required (JWT)

Request body:

```json
{
  "current_password": "OldPassword123!",
  "new_password": "NewPassword456!",
  "confirm_new_password": "NewPassword456!"
}
```

Validation backend:
- `current_password`: required
- `new_password`: 8-32 chars, 1 uppercase + 1 lowercase + 1 number
- `confirm_new_password`: match `new_password`
- `new_password` khac `current_password`

Success:
- HTTP `200`
- `data = null`

Errors:
- `400`: payload invalid / wrong current password / weak password / mismatch
- `401`: missing/invalid JWT
- `500`: internal error

---

## 6. Business flow va redirect rules

## 6.1 Main flow
1. User nhap 3 field password
2. FE validate client-side
3. Submit API `POST /api/v1/auth/change-password`
4. Neu success `200`:
   - hien success message
   - force logout local (clear tokens/session cache)
   - redirect `/auth/login`
5. Neu error:
   - `400`: map field/global error
   - `401`: clear session + redirect login
   - `500`: show retry message

## 6.2 Security flow bat buoc

Theo business spec:
- Sau doi mat khau, backend revoke tat ca refresh sessions ACTIVE.
- FE can thong bao ro:
  - user se can dang nhap lai
  - cac phien khac bi dang xuat

---

## 7. Validation rules FE

Client-side:
- `current_password`: required
- `new_password`: required + complexity
- `confirm_new_password`: required + match
- `new_password` != `current_password`

UI helper checklist cho `new_password`:
- 8-32 characters
- at least 1 uppercase
- at least 1 lowercase
- at least 1 number

Server-side:
- Uu tien map `errors[field]` vao dung field
- Neu khong co field -> hien global error message

---

## 8. Error mapping guideline

- `400`:
  - Wrong current password -> field/global message "Mat khau hien tai khong dung."
  - Password mismatch/weak -> map vao new/confirm field
- `401`:
  - "Phien dang nhap het han. Vui long dang nhap lai."
  - clear auth + redirect login
- `500`:
  - "Co loi xay ra. Vui long thu lai."

Rule:
- Khong show raw stack trace
- Khong log raw passwords

---

## 9. Mapping tu Stitch code sang implementation

Can giu:
- layout card center, title/subtitle, 3 password inputs
- visibility toggle icon
- inline checklist cho password strength
- loading spinner state
- nav/footer style tone

Can chinh de dung logic project:
- Form submit button type phai la `submit` (khong hardcode disabled nhu mock HTML).
- Thong diep success phai dan den forced logout.
- Neu code stitch co text EN/VN tron lan, FE production can chon 1 copy strategy nhat quan.

---

## 10. Component decomposition de xuat

Trong `features/auth`:
- `pages/ChangePasswordPage.jsx`
- `components/ChangePasswordForm.jsx`
- `components/PasswordRuleChecklist.jsx`
- `components/PasswordInputWithToggle.jsx`
- `components/AuthAlert.jsx`
- `schemas/authSchemas.js`
- `api/authApi.js`
- `hooks/useChangePasswordFlow.js` (neu can)

Rule:
- UI component khong tu goi API truc tiep.
- Flow logic submit + success logout dat o hook/page container.

---

## 11. Integration voi MSW (dev)

Muc tieu:
- FE code full flow du backend chua hoan tat.

Can mock toi thieu:
- `POST /api/v1/auth/change-password`

Case de mock:
- Success `200`
- Wrong current password `400`
- Mismatch `400`
- Unauthorized `401`
- Generic `500`

---

## 12. Acceptance criteria (Definition of Done)

Change password screen duoc xem la hoan thanh khi:
- Dung UI theo Stitch + design system
- Dung API contract `/api/v1/auth/change-password`
- Co password toggle + inline requirement checklist
- Co loading/error/success/validation states
- On `200`: clear local auth + redirect `/auth/login`
- On `401`: redirect login
- Error mapping dung cho `400/500`
- Khong log password/token
- Pass lint/build trong project frontend

---

## 13. Prompt template de giao AI code Change Password

```txt
Implement Change Password screen based on:
- docs/frontend/Change_Password_screens.md
- docs/engineering-rules/fe-master-context.md
- docs/engineering-rules/frontend-convention.md
- docs/engineering-rules/design-system.md
- docs/engineering-rules/frontend-api-integration.md
- docs/api-FE_behavior/ChangePassword-api-and-behavior.md
- frontend/stitch/change_password_screen/Change_Password_Screens_Design.md
- frontend/stitch/change_password_screen/Change_Password_Screens_Code.html

Requirements:
1) React + Vite + JavaScript + Tailwind only
2) No TypeScript
3) Implement full change-password flow with field validation and API integration
4) On success 200: show success then force logout and redirect /auth/login
5) Handle 400/401/500 with proper field/global messages
6) Keep feature-first structure under features/auth

After coding:
- list created/updated files
- explain validation and forced-logout flow
- self-check with acceptance criteria in docs/frontend/Change_Password_screens.md
```

