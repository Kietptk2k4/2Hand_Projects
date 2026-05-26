# Profile / Account Screen — Stitch Design Brief (2Hands)

> **Mục đích:** Copy toàn bộ file này (hoặc section **STITCH PROMPT** ở cuối) vào Google Stitch để generate **một màn hình** Profile/Account với **6 tab dọc bên phải**.  
> **UI only** — không implement API, routing, hay business logic. Dùng mock data trong design.

---

## 1. Source of truth (behavior + API)

Khi gen từng tab chi tiết, đính kèm thêm file behavior tương ứng:

| Tab | Label (VN) | API chính | File `api_fe_behavior` |
|-----|------------|-----------|---------------------------|
| 1 | Thông tin tài khoản | `GET /api/v1/users/me` | `docs/api_fe_behavior/auth_api_fe_behavior/account-info-api-and-behavior.md` |
| 2 | Chỉnh sửa hồ sơ | `PUT /api/v1/users/me/profile` | `docs/api_fe_behavior/auth_api_fe_behavior/edit-profile-api-and-behavior.md` |
| 3 | Ảnh đại diện | `POST .../avatar/upload-url` → MinIO PUT → `PATCH .../avatar` | `update-avatar-api-and-behavior.md`, `AvatarUpload-api-and-behavior.md` |
| 4 | Quyền riêng tư | `PATCH /api/v1/users/me/privacy` | `privacy-api-and-behavior.md` |
| 5 | Cài đặt | `PATCH /api/v1/users/me/settings` | `update-user-setting-api-and-behavior.md` |
| 6 | Xóa tài khoản | `POST /api/v1/users/me/soft-delete` | `soft-delete-account-api-and-behavior.md` |

**Master spec (shell + mapping 6 tab):**  
`docs/api_fe_behavior/auth_api_fe_behavior/ProfileAccount-api-and-behavior.md`

**Out of scope trên screen này:** public profile người khác, admin, logout, đổi mật khẩu (screen riêng `/account/change-password`).

---

## 2. Design system (đồng bộ auth screens hiện có)

- **App:** 2Hands — marketplace second-hand, giao diện sạch, tin cậy.
- **Font:** Inter.
- **Icons:** Google Material Symbols Outlined (`person`, `edit`, `photo_camera`, `lock`, `settings`, `delete_forever`, `check_circle`, `error`, …).
- **Colors (Tailwind tokens):**
  - Primary `#0066FF`, surface `#F9F9FF`, containers trắng.
  - Text: `on-surface` `#111C2D`, variant `#5D6472`.
  - Border: `outline-variant` `#C7CFDE`.
  - Error / danger: `#BA1A1A`, error container `#FFDAD6`.
- **Layout:** Desktop-first, responsive mobile (tab chuyển thành horizontal scroll hoặc bottom sheet trên mobile nếu cần).
- **Ngôn ngữ UI:** Tiếng Việt (không dấu hoặc có dấu đều được, ưu tiên nhất quán với auth hiện tại: "Tai khoan", "Cap nhat", …).
- **Không** hiển thị JWT, `user_id` raw, hay URL presigned đầy đủ trong UI (chỉ preview avatar).

---

## 3. Screen shell — một page, 6 tab dọc **bên phải**

```
┌─────────────────────────────────────────────────────────────────────────┐
│  App header (optional): logo 2Hands · breadcrumb "Tai khoan"           │
├──────────────────────────────────────────────┬──────────────────────────┤
│                                              │  [Tab 1] Thong tin TK    │
│   CONTENT PANEL (left, ~70%)                 │  [Tab 2] Chinh sua ho so │
│   - Title H1 theo tab dang chon              │  [Tab 3] Anh dai dien    │
│   - Subtitle mo ta ngan                      │  [Tab 4] Quyen rieng tu  │
│   - Noi dung tab (form / read-only / danger) │  [Tab 5] Cai dat         │
│                                              │  [Tab 6] Xoa tai khoan   │
│                                              │  (vertical nav, right)   │
└──────────────────────────────────────────────┴──────────────────────────┘
```

### Shell requirements

- **Page title (khi chưa vào tab cụ thể):** `Tai khoan cua toi`
- **Subtitle:** `Quan ly ho so, quyen rieng tu va cai dat ung dung`
- Tab đang active: nền `surface-container`, border primary, icon + label.
- Tab inactive: text variant, hover nhẹ.
- Tab **6 (Xóa tài khoản):** màu danger (text/icon đỏ), tách visual khỏi 5 tab trên.
- Mỗi tab có **một primary action** riêng (trừ Tab 1 read-only có thể chỉ có "Lam moi" optional).
- **Global loading:** skeleton khi "đang tải" `GET /users/me` lần đầu (ảnh hưởng mọi tab).
- **Toast area** (góc trên phải hoặc dưới form): success / error — thiết kế placeholder.

### Mock user (dùng trong Stitch)

```json
{
  "user": {
    "email": "kiet@example.com",
    "status": "ACTIVE",
    "email_verified": true,
    "phone": null,
    "last_login_at": "2026-05-20T08:00:00Z"
  },
  "profile": {
    "display_name": "Kiet Tran",
    "avatar_url": "https://placehold.co/120x120/png?text=KT",
    "bio": "Backend engineer @ 2Hands",
    "website": "https://example.com",
    "social_links": {
      "github": "https://github.com/kiet",
      "facebook": "https://facebook.com/kiet"
    },
    "is_private": false
  },
  "settings": {
    "appearance_mode": "SYSTEM"
  }
}
```

---

## 4. Tab 1 — Thông tin tài khoản (read-only)

**Ref:** `account-info-api-and-behavior.md`

### Sections (3 khối card)

1. **Tai khoan**
   - Email (read-only)
   - Trang thai: badge `ACTIVE` / `PENDING_VERIFICATION` / `SUSPENDED`
   - Email da xac thuc: icon check / warning
   - So dien thoai: `Chua cap nhat` nếu null
   - Lan dang nhap gan nhat: format datetime VN hoặc `Chua cap nhat`

2. **Ho so**
   - Ten hien thi, avatar thumbnail tròn 64px
   - Bio, Website (link), Social links (danh sách key + URL clickable)
   - Che do rieng tu: `Cong khai` / `Rieng tu` (snapshot từ `is_private`)

3. **Cai dat (tom tat)**
   - Giao dien: `Sang` / `Toi` / `Theo he thong` map từ `LIGHT` / `DARK` / `SYSTEM`

### States

- Loading: skeleton 3 cards.
- Error banner: `Khong tai duoc thong tin tai khoan` + nút `Thu lai`.
- Không có nút Submit chính; có thể có link `Chinh sua ho so` chuyển sang Tab 2 (chỉ visual).

---

## 5. Tab 2 — Chỉnh sửa hồ sơ (form)

**Ref:** `edit-profile-api-and-behavior.md`

### Fields

| Field | Label VN | Required | Ghi chú |
|-------|----------|----------|---------|
| `display_name` | Ten hien thi | Có | max 100, counter |
| `bio` | Gioi thieu | Không | textarea, max 500, counter |
| `website` | Website | Không | URL http/https |
| `social_links` | Mang xa hoi | Không | Dynamic list: dropdown/platform + URL, max 10 rows, nút `Them lien ket` / xóa row |

Gợi ý platform keys: `facebook`, `github`, `instagram`, `tiktok`, `other`.

### Actions

- Primary: `Luu thay doi` (loading spinner khi submit).
- Success toast: `Cap nhat ho so thanh cong.`
- Inline errors dưới field (400): ví dụ website `URL khong hop le`.

---

## 6. Tab 3 — Ảnh đại diện (upload flow)

**Ref:** `update-avatar-api-and-behavior.md`, `AvatarUpload-api-and-behavior.md`

### Layout

- **Preview lớn:** avatar tròn 120–160px, border outline.
- **Trạng thái hiện tại:** `Anh dai dien hien tai`
- **Chọn file:** nút `Chon anh` (image picker) — accept JPEG, PNG, WebP.
- **Hint:** `Toi da 5MB · Dinh dang: JPG, PNG, WebP`
- **Progress bar** khi đang upload (mock 45%).
- **Steps indicator (optional):** `1. Chon anh` → `2. Tai len` → `3. Luu`

### Actions

- Primary: `Cap nhat anh dai dien` (disabled khi chưa chọn file hoặc đang upload).
- Secondary: `Huy` (reset preview về ảnh cũ).

### Error placeholders (static trong design)

- File quá lớn: `Tep vuot qua 5MB`
- Loai file: `Dinh dang khong duoc ho tro`
- Rate limit: `Ban thao tac qua nhieu, thu lai sau`

**Không** hiển thị ô nhập `avatar_url` thủ công cho end-user — flow là chọn file → upload → lưu.

---

## 7. Tab 4 — Quyền riêng tư

**Ref:** `privacy-api-and-behavior.md`

### Content

- Card với **toggle lớn** (switch): `Che do ho so rieng tu`
- Trạng thái OFF: subtitle `Ho so cong khai — moi nguoi xem duoc bio, website va lien ket`
- Trạng thái ON: subtitle `Chi hien thi ten va anh dai dien voi nguoi khac`
- Info box (icon info):  
  `Khi bat che do rieng tu, nguoi xem cong khai chi thay ten hien thi va anh dai dien. Bio, website va mang xa hoi se bi an.`
- Toggle auto-save hoặc nút `Ap dung` — thiết kế **toggle + saving spinner** trên switch.

### States

- Saving: switch disabled + small loader.
- Failed: toggle revert + error text đỏ phía dưới.

---

## 8. Tab 5 — Cài đặt (giao diện / theme)

**Ref:** `update-user-setting-api-and-behavior.md`

### Control — chọn một trong ba (segmented control hoặc radio cards)

| Value | Label VN | Icon gợi ý |
|-------|----------|------------|
| `LIGHT` | Sang | `light_mode` |
| `DARK` | Toi | `dark_mode` |
| `SYSTEM` | Theo he thong | `routine` |

- Mô tả `SYSTEM`: `Tu dong theo cai dat thiet bi cua ban`
- Primary: `Luu cai dat`
- Preview nhỏ (optional): 2 thumbnail light/dark mock bên cạnh selector.

---

## 9. Tab 6 — Xóa tài khoản (danger zone)

**Ref:** `soft-delete-account-api-and-behavior.md`

### Layout

- Viền/card màu danger nhạt (`error-container` background).
- Icon `warning` + title: `Vung nguy hiem`
- Body text:  
  `Hanh dong nay se vo hieu hoa tai khoan va dang xuat tat ca thiet bi. Ban co the khong dang nhap lai bang tai khoan nay. Du lieu co the duoc xu ly theo chinh sach luu tru cua 2Hands.`
- Field: `Mat khau hien tai` (password, show/hide toggle).
- Primary danger button: `Xoa tai khoan` (outline/filled đỏ).

### Modal — bắt buộc thiết kế frame riêng

**Trigger:** click `Xoa tai khoan` khi đã nhập password.

| Element | Nội dung |
|---------|----------|
| Title | `Xac nhan xoa tai khoan?` |
| Body | `Ban sap xoa vinh vien quyen truy cap vao tai khoan nay. Hanh dong khong the hoan tac tu phia ban.` |
| Actions | `Huy` (secondary) · `Xoa tai khoan` (danger, loading state variant) |

Thiết kế thêm **variant success redirect** (optional frame): full-screen nhẹ `Tai khoan da duoc xoa` + nút `Ve trang dang nhap`.

---

## 10. Shared UX patterns (mọi tab)

| Pattern | Mô tả |
|---------|--------|
| Inline validation | Border đỏ field + text lỗi nhỏ phía dưới |
| Submit loading | Primary button disabled + spinner |
| Success toast | Xanh/neutral, icon check, tự dismiss |
| Error toast / banner | Đỏ hoặc error container, nút `Thu lai` cho 500 |
| 401 (design note) | Banner: `Phien het han, vui long dang nhap lai` |
| Empty/null | Text xám `Chua cap nhat` |

---

## 11. Deliverables cho Stitch

Generate **một design** desktop (1440px) gồm:

1. **Default view:** Tab 1 active, đủ data mock.
2. **Variants** (có thể frames riêng):
   - Tab 2 — form filled + 1 field lỗi
   - Tab 3 — upload progress 45%
   - Tab 4 — toggle ON
   - Tab 5 — `DARK` selected
   - Tab 6 — danger zone + **confirmation modal** overlay
3. **Mobile** (390px): tab nav chuyển xuống dưới hoặc hamburger "Menu tai khoan".

Export naming gợi ý: `Profile_Account_6Tabs_Desktop.png`, `Profile_DeleteConfirm_Modal.png`.

---

## 12. STITCH PROMPT — copy từ đây xuống dưới

```text
Design a Profile / Account settings screen for "2Hands", a Vietnamese second-hand marketplace web app.

SCOPE: UI/UX mockup only. Use realistic mock data. No API code. Vietnamese labels.

LAYOUT:
- Single authenticated page: "Tai khoan cua toi"
- LEFT (~70%): content panel changes per tab
- RIGHT (~30%): vertical tab navigation with 6 items (icons + labels):
  1. Thong tin tai khoan (person icon) — READ-ONLY
  2. Chinh sua ho so (edit icon) — FORM
  3. Anh dai dien (photo_camera icon) — AVATAR UPLOAD
  4. Quyen rieng tu (lock icon) — PRIVACY TOGGLE
  5. Cai dat (settings icon) — THEME SELECTOR
  6. Xoa tai khoan (delete icon, danger/red styling) — DANGER ZONE

DESIGN SYSTEM:
- Font Inter, primary blue #0066FF, light surface #F9F9FF, white cards, subtle borders #C7CFDE
- Material Symbols Outlined icons
- Clean, trustworthy, modern — match existing 2Hands auth screens (login/register style)

TAB 1 — Account Info (read-only, 3 cards):
- Account: email, status badge ACTIVE, email verified, phone placeholder, last login
- Profile: display name, round avatar, bio, website link, social links list, privacy snapshot
- Settings snapshot: theme mode label (Sang/Toi/Theo he thong)
- Loading skeleton state; error banner with retry

TAB 2 — Edit Profile form:
- display_name (required, max 100), bio textarea (max 500), website URL
- Dynamic social links: platform + URL rows, add/remove, max 10
- Save button "Luu thay doi", inline validation example on website field
- Success toast placeholder

TAB 3 — Update Avatar:
- Large round avatar preview, "Chon anh" button, file hints (5MB, JPG/PNG/WebP)
- Upload progress bar (mock 45%), primary "Cap nhat anh dai dien"
- Do NOT show manual URL input for users

TAB 4 — Privacy:
- Large toggle "Che do ho so rieng tu" with ON/OFF explanatory subtitles
- Info callout: when ON, public viewers only see name + avatar
- Saving state on toggle

TAB 5 — Settings:
- Segmented control or 3 radio cards: LIGHT (Sang), DARK (Toi), SYSTEM (Theo he thong)
- Save button "Luu cai dat"

TAB 6 — Delete Account:
- Danger zone card, warning copy, password field with show/hide, red "Xoa tai khoan" button
- ALSO design confirmation MODAL overlay:
  Title "Xac nhan xoa tai khoan?", destructive confirm + cancel
  Show modal as separate frame over dimmed page

MOCK USER:
- Email kiet@example.com, display_name Kiet Tran, avatar placeholder, bio, website, github+facebook links, is_private false, appearance_mode SYSTEM

RESPONSIVE:
- Desktop 1440px main artboard
- Optional mobile 390px with adapted tab navigation

ACCESSIBILITY:
- Visible focus rings, sufficient contrast, labeled form fields, aria-friendly structure

REFERENCE SPECS (for field names and behavior — do not render API paths in UI):
- ProfileAccount-api-and-behavior.md (master)
- account-info, edit-profile, update-avatar, AvatarUpload, privacy, update-user-setting, soft-delete-account behavior docs in auth_api_fe_behavior folder
```

---

## 13. Cách dùng với Stitch (gợi ý workflow)

1. **Lần 1:** Paste **Section 12 (STITCH PROMPT)** → gen shell + 6 tab (desktop).
2. **Lần 2–7 (tuỳ chọn):** Gen/refine từng tab, đính kèm file `api_fe_behavior` tương ứng + screenshot tab từ lần 1 để giữ đồng bộ.
3. Lưu output vào `frontend/stitch/profile_account_screen/` (PNG, HTML, Design notes).
4. **Composer (sau này):** implement `AccountPage` / `ProfileAccountPage` theo stitch output + đầy đủ `api_fe_behavior`.

---

*Generated from ProfileAccount master spec and 7 tab-level behavior docs in `docs/api_fe_behavior/auth_api_fe_behavior/`.*
