# Session Expired Screens Spec - 2Hands Frontend

Version: 1.0
Scope: Quy dinh implementation cho modal/page Session Expired trong auth lifecycle.

---

## 1. Muc tieu

Tai lieu nay la implementation-ready spec cho FE Session Expired, duoc tong hop tu:
- `docs/engineering-rules/fe-master-context.md`
- `docs/engineering-rules/frontend-convention.md`
- `docs/engineering-rules/design-system.md`
- `docs/engineering-rules/frontend-api-integration.md`
- `docs/api-FE_behavior/RefreshAccessToken-api-and-behavior.md`
- `docs/api-FE_behavior/Logout-api-and-behavior.md`
- `frontend/stitch/session_expired_screen/Session_Expired_Design.md`
- `frontend/stitch/session_expired_screen/Session_Expired_Code.html`

Luu y ve naming asset:
- Trong thu muc Stitch hien co file `Session_Expired_Design.md` va `Session_Expired_Code.html`.
- Khong tim thay file anh `Session_Expired_Screen.png` trong thu muc nay tai thoi diem tao tai lieu.

Uu tien khi conflict:
1. `fe-master-context.md`
2. `frontend-api-integration.md`
3. `frontend-convention.md`
4. `design-system.md`
5. `RefreshAccessToken-api-and-behavior.md`
6. `Logout-api-and-behavior.md`
7. Stitch design/code assets

---

## 2. Tech stack va quy tac implementation (bat buoc)

- React + Vite + JavaScript + Tailwind CSS
- Khong dung TypeScript (`.ts`, `.tsx`)
- Component file: `.jsx`
- API layer tach rieng khoi UI, khong goi API truc tiep trong presentational component
- Khong log access token/refresh token

Response wrapper backend (refresh fail 401):

```json
{
  "code": 401,
  "success": false,
  "message": "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai.",
  "data": null,
  "errors": null,
  "timestamp": "2026-05-15T10:00:00Z"
}
```

---

## 3. Route map va display mode

Session expired UX co 2 mode:

1. **Default mode (khuyen nghi):** modal overlay tren context hien tai  
2. **Fallback mode (optional):** dedicated route page `/auth/session-expired`

Lien quan route:
- Login page: `/auth/login`
- Optional fallback page: `/auth/session-expired`

---

## 4. UI/UX specification (theo design system + Stitch)

## 4.1 Layout tong quan

Theo Stitch HTML:
- Nen context bi blur/dim
- Modal center card
- Icon `lock_clock`
- Title: "Session expired"
- Subtitle: "Please login again to continue your work."
- Actions:
  - Primary: `Sign In`
  - Secondary: `Close`

## 4.2 Behavior cua actions

- `Sign In`:
  - clear local session state (access token + refresh token + user cache)
  - dieu huong den `/auth/login`
- `Close`:
  - dong modal
  - neu page hien tai la protected va da mat session, app van phai block action nhay cam
  - co the redirect login ngay neu policy security cua team yeu cau

## 4.3 Accessibility

- Modal trap focus
- Esc de close (neu policy cho phep)
- `role="dialog"` + `aria-modal="true"`
- focus vao CTA chinh khi modal mo
- contrast dat WCAG AA

---

## 5. API contract va trigger rules

## 5.1 Trigger tu network layer

Session expired co the xuat hien khi:
- request protected API bi `401`
- hoac refresh flow tra `401` sau khi da thu refresh

Refresh endpoint:
- `POST /api/v1/auth/refresh`
- request body: `{ "refresh_token": "..." }`

Rule bat buoc:
1. Khi request protected fail `401`, thu refresh **1 lan**
2. Neu refresh success:
   - cap nhat access token
   - retry request goc 1 lan
3. Neu refresh fail `401`:
   - clear auth state
   - hien modal/page Session Expired
   - dieu huong login sau action nguoi dung (hoac ngay lap tuc theo policy)

## 5.2 Logout endpoint lien quan

`POST /api/v1/auth/logout` idempotent, co the goi khi user chu dong logout.
Trong session-expired case:
- khong bat buoc goi logout neu refresh token da invalid.
- uu tien clear local state ngay lap tuc de dam bao an toan.

---

## 6. Business flow (end-to-end)

### Flow A - Silent recovery success
1. API protected fail `401`
2. Goi refresh
3. Refresh success `200`
4. Retry request goc
5. User khong thay session expired UI

### Flow B - Session expired
1. API protected fail `401`
2. Goi refresh
3. Refresh fail `401`
4. Clear auth local
5. Hien Session Expired modal/page
6. User bam `Sign In` -> redirect login

### Flow C - Hard fallback
1. App detect token/storage corrupt hoac unauthorized lien tiep
2. Bo qua modal, redirect thang `/auth/login` voi flash message session expired

---

## 7. Error mapping guideline

Nguon thong diep uu tien:
1. `message` tu backend
2. message fallback frontend

Display de xuat:
- Default: "Phien dang nhap da het han, vui long dang nhap lai."
- Khong hien stack trace/noi dung internal

---

## 8. State management rules

Khi session expired:
- clear `access_token`
- clear `refresh_token`
- clear current user profile cache (React Query/store)
- invalidate query `authKeys.me` (neu dang dung)

Tranh loop:
- danh dau request da retry 1 lan de khong refresh vo han

---

## 9. Mapping tu Stitch code sang implementation

Can giu:
- modal center UI, icon + title + subtitle + 2 actions
- overlay blur background
- tone mau/spacing theo design system

Can chinh de dung logic project:
- `Sign In` phai trigger clear-session + route login that
- `Close` behavior phai co policy ro (dong modal hoac redirect login neu route protected)
- khong phu thuoc static dashboard mockup trong HTML; modal phai reusable tren moi protected screen

---

## 10. Component decomposition de xuat

Trong `features/auth`:
- `components/SessionExpiredModal.jsx`
- `pages/SessionExpiredPage.jsx` (optional fallback)
- `hooks/useSessionRecovery.js`

Trong `services/http`:
- `apiClient` interceptor detect 401
- `authRefreshService` xu ly refresh va retry

Trong `app/providers`:
- provider global de mo SessionExpired modal

---

## 11. Integration voi MSW (dev)

Muc tieu:
- test duoc ca flow silent refresh va flow session expired.

Can mock toi thieu:
- `POST /api/v1/auth/refresh`:
  - case `200` success
  - case `401` expired/revoked
- protected endpoint sample (`GET /api/v1/users/me`) tra `401` de trigger flow

Checklist test:
- request goc retry dung 1 lan khi refresh success
- modal hien khi refresh fail
- local session bi clear
- `Sign In` redirect dung

---

## 12. Acceptance criteria (Definition of Done)

Session Expired duoc xem la hoan thanh khi:
- Co interceptor flow 401 -> refresh -> retry hoac fail
- Khong refresh loop vo han
- Khi refresh fail:
  - clear auth local
  - hien session-expired modal/page
  - user co the vao login nhanh
- UI dung tone Stitch + design system
- Khong log token nhay cam
- Pass lint/build trong project frontend

---

## 13. Prompt template de giao AI code Session Expired

```txt
Implement Session Expired UX based on:
- docs/frontend/Session_Expired_screens.md
- docs/engineering-rules/fe-master-context.md
- docs/engineering-rules/frontend-convention.md
- docs/engineering-rules/design-system.md
- docs/engineering-rules/frontend-api-integration.md
- docs/api-FE_behavior/RefreshAccessToken-api-and-behavior.md
- docs/api-FE_behavior/Logout-api-and-behavior.md
- frontend/stitch/session_expired_screen/Session_Expired_Design.md
- frontend/stitch/session_expired_screen/Session_Expired_Code.html

Requirements:
1) React + Vite + JavaScript + Tailwind only
2) No TypeScript
3) Implement 401 handling: refresh once, retry original request once
4) If refresh fails 401: clear auth state and show Session Expired modal/page
5) Sign In action routes to /auth/login
6) Avoid infinite refresh loop

After coding:
- list created/updated files
- explain retry/refresh/session-clear flow
- self-check with acceptance criteria in docs/frontend/Session_Expired_screens.md
```

