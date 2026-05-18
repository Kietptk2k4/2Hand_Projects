# FE Master Context - 2Hands

Version: 1.0
Owner: Frontend Team
Purpose: Nguon context duy nhat de AI code FE dung huong cho auth flow.

---

## 1) Fixed Tech Stack (bat buoc)

- React
- Vite
- JavaScript (KHONG dung TypeScript)
- Tailwind CSS

Implementation rules:
- File component dung `.jsx`
- Khong tao file `.ts` hoac `.tsx`
- Uu tien utility classes cua Tailwind
- Chi viet CSS rieng khi utility classes khong phu hop

---

## 2) Priority Documents (AI phai doc truoc khi code)

### 2.1 Engineering rules (bat buoc)

- `docs/engineering-rules/frontend-convention.md`
- `docs/engineering-rules/design-system.md`
- `docs/engineering-rules/frontend-api-integration.md`

### 2.2 API behavior specs (auth)

- `docs/api-FE_behavior/register-api-and-behavior.md`
- `docs/api-FE_behavior/login-api-and-behavior.md`
- `docs/api-FE_behavior/RefreshAccessToken-api-and-behavior.md`
- `docs/api-FE_behavior/Logout-api-and-behavior.md`
- `docs/api-FE_behavior/ForgotPassword -api-and-behavior.md`
- `docs/api-FE_behavior/ChangePassword-api-and-behavior.md`
- `docs/api-FE_behavior/Verify-api-and-behavior.md`
- `docs/api-FE_behavior/ProfileAccount-api-and-behavior.md`

---

## 3) Core API Contract (khong duoc sai)

- Base path: `/api/v1`
- Auth endpoints: `/api/v1/auth/*`
- User account endpoints: `/api/v1/users/me/*`

Response wrapper:

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

Rules:
- Component/page KHONG parse raw HTTP response truc tiep.
- API layer phai unwrap response va map loi field-level.
- Frontend phai ho tro field `snake_case` dung theo contract backend.

---

## 4) Architecture Rules for FE Module

De xuat structure:

```txt
2HAND_PROJECTS/Frontend/
├── app/
├── features/
│   └── auth/
│       ├── api/
│       ├── hooks/
│       ├── pages/
│       ├── components/
│       └── schemas/
├── shared/
└── services/http/
```

Rules:
- Khong call API truc tiep trong JSX.
- Khong dat business logic trong presentational component.
- Tiep can theo feature-first (`features/auth`).

---

## 5) UX & Quality Gates (bat buoc cho moi screen)

Moi man hinh auth/account phai co:
- Loading state
- Error state
- Success state
- Validation state (client + server)

Khac:
- Route guard cho protected pages
- Xu ly `401/403/429` dung convention
- Khong log password/token

---

## 6) Definition of Done cho task AI code

Task duoc xem la hoan thanh khi:
- Dung stack React + Vite + JavaScript + Tailwind
- Dung contract API va mapping loi
- UI dung design system
- Code pass lint
- Co test co ban cho flow vua lam (neu task yeu cau test)
- AI liet ke ro file da tao/sua

---

## 7) Prompt Template (copy/paste cho moi task)

```txt
Implement [TEN SCREEN/FLOW] using React + Vite + JavaScript + Tailwind.

Before coding, read and follow strictly:
- docs/engineering-rules/fe-master-context.md
- docs/engineering-rules/frontend-convention.md
- docs/engineering-rules/design-system.md
- docs/engineering-rules/frontend-api-integration.md
- [api behavior file lien quan trong docs/api-FE_behavior]

Requirements:
1) Build UI dung design system
2) Integrate API dung response wrapper va error mapping
3) Co loading/error/success/validation states
4) Khong su dung TypeScript
5) Refactor nhe neu can nhung khong pha vo behavior

After coding:
- List all created/updated files
- Explain ngắn gọn flow
- Self-check against Definition of Done
```

---

## 8) Conflict Resolution Rule

Neu co xung dot giua tai lieu:
1. `fe-master-context.md`
2. `frontend-api-integration.md`
3. `frontend-convention.md`
4. `design-system.md`
5. `docs/api-FE_behavior/*`

AI phai:
- Chon theo thu tu uu tien tren
- Neu van mo ho, ghi ro assumption trong output

