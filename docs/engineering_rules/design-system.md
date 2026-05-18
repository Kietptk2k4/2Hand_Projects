# Design System Guidelines for Auth Flows

Version: 1.0
Scope: UI cho cac man hinh lien quan den `auth-service` (auth + user account).

---

## 1. Nguyen tac thiet ke

- Uu tien ro rang, de doc, de thao tac.
- Trang thai bao mat va loi phai hien thi minh bach.
- Dong bo voi brand tone "professional + reliable".
- Toi gian visual clutter trong cac form auth.

---

## 2. Mau sac

Su dung palette tu design system tong:
- Primary: `#0066FF` (CTA chinh)
- Secondary: `#6063EE`
- Surface: `#F9F9FF`
- On-surface text: `#111C2D`
- Error: `#BA1A1A`
- Success: xanh semantic cua app

Rule:
- Khong hardcode mau random trong component auth.
- Trang thai focus, error, success cua input phai nhat quan.

---

## 3. Typography

- Font: `Inter`
- Hierarchy de xuat:
  - Page title: 24-32px, weight 600-700
  - Section title: 18-20px, weight 600
  - Body/form text: 14-16px
  - Helper/error text: 12-14px
- Message quan trong (bao mat, canh bao) khong dung mau de thay the cho thu bac chu.

---

## 4. Spacing va layout

- Scale co ban: `8, 16, 24, 32, 40, 64`
- Auth screen layout:
  - Container center
  - Max width form: 420-520px
  - Khoang cach field: 16px
  - Khoang cach section: 24px
- User account screen:
  - Group theo card section (profile, security, preferences)
  - Padding card: 16-24px

---

## 5. Border, radius, elevation

- Radius:
  - Input/Button: 8px
  - Card: 16px
  - Modal: 24px
- Elevation:
  - Card thuong: level 1/2 nhe
  - Modal va popup quan trong: level 3
- Border:
  - Input default: 1px neutral
  - Focus: 2px primary
  - Error: 1-2px error

---

## 6. Component standards cho auth

### 6.1 Input
- Bat buoc co label ro rang.
- Password field co toggle show/hide.
- Khi loi:
  - vi tri helper text o duoi field
  - field co `aria-invalid=true`
- Cac field tu backend contract:
  - email/password
  - confirm password
  - current/new/confirm new password
  - display name, avatar url, website

### 6.2 Button
- Primary button cho action chinh (Login, Register, Save).
- Secondary/Ghost cho action phu.
- Trang thai:
  - default, hover, active, disabled, loading
- Loading phai co spinner + disable click duplicate.

### 6.3 Feedback components
- Inline field error cho validation.
- Form-level alert cho loi chung (`message` tu API).
- Success toast/banner cho action cap nhat thanh cong.
- Rate limit (`429`) can thong diep than thien va de hieu.

### 6.4 Account settings blocks
- Tach block ro:
  - Profile
  - Avatar
  - Privacy
  - Appearance mode (`LIGHT | DARK | SYSTEM`)
  - Soft delete account
- Soft delete la khu vuc "danger zone":
  - mau nhan biet
  - xac nhan password
  - canh bao irrecoverable neu policy yeu cau

---

## 7. State UX cho moi screen

Moi screen auth/account bat buoc co:
- Loading state
- Empty state (neu co du lieu rong)
- Error state (network/server)
- Success state

Khong de man hinh "trang" khi request dang chay.

---

## 8. Accessibility (a11y)

- Tat ca control keyboard accessible.
- Thu tu tab hop ly theo flow form.
- Dung semantic element (`button`, `form`, `label`).
- Input error can lien ket voi helper text bang `aria-describedby`.
- Contrast dat toi thieu WCAG AA cho text quan trong.

---

## 9. Responsive behavior

- Mobile first.
- Breakpoint de xuat:
  - Mobile: < 768
  - Tablet: 768 - 1279
  - Desktop: >= 1280
- Tren mobile:
  - CTA full-width trong form auth
  - sticky bottom action chi dung khi can, khong che form field

---

## 10. Copywriting va i18n

- Message hien tai cua backend chu yeu la tieng Viet khong dau/English.
- Frontend phai:
  - cho phep map message backend -> copy than thien
  - khong hardcode mot ngon ngu duy nhat neu app co i18n
- Cac text bao mat can ngan gon, khong mo ho.

