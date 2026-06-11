# Mobile Design System - 2Hands

Version: 1.1  
Source: `frontend/stitch/*/DESIGN.md` (commerce + social) and repo design tokens.

---

## 1. Brand

- Vertical: fashion second-hand C2C marketplace + social discovery
- Tone: professional, reliable, clean
- Font: Inter (load via `@expo-google-fonts/inter` when adding custom fonts; until then system sans-serif is acceptable for scaffold)

Social and commerce share the same **Service Professionalism** palette from stitch.

---

## 2. Colors

Use `src/shared/theme/colors.js`:

| Token | Hex | Usage |
|-------|-----|-------|
| primary | `#0050CB` | CTA, links, active tab |
| onPrimary | `#FFFFFF` | Text on primary buttons |
| surface | `#F9F9FF` | Screen background |
| surfaceContainerLow | `#F0F3FF` | Subtle sections (feed composer area) |
| surfaceContainerLowest | `#FFFFFF` | Cards, post cards |
| surfaceContainerHigh | `#DEE8FF` | Skeleton placeholders |
| onSurface | `#111C2D` | Primary text |
| onSurfaceVariant | `#424656` | Secondary text, timestamps |
| outline | `#727687` | Icons muted |
| outlineVariant | `#C2C6D8` | Borders, dividers |
| error | `#BA1A1A` | Errors |
| errorContainer | `#FFDAD6` | Error banner background |
| onErrorContainer | `#93000A` | Error banner text |
| secondary | `#4648D4` | Accent (hashtags, secondary actions) |

Do not hardcode random hex in screens — extend `colors.js` if needed.

Full token YAML: `frontend/stitch/social_feed/DESIGN.md` (same palette as `commerce_home`).

---

## 3. Typography (mobile scale)

Align with stitch `headline-*-mobile` and `body-*` tokens:

| Role | Size | Weight | Stitch token |
|------|------|--------|--------------|
| Screen title | 24px | 700 | `headline-lg-mobile` |
| Section title | 18–20px | 600 | `headline-sm` |
| Post author name | 16px | 600 | `body-md` bold |
| Body / caption | 14–16px | 400 | `body-md`, `body-sm` |
| Label / tab | 14px | 600 | `label` |
| Caption / error | 12px | 400 | `body-sm` |
| Stat number (profile) | 20px | 600 | — |

---

## 4. Spacing & Layout

8px base grid: `8, 16, 24, 32`.

| Context | Rule |
|---------|------|
| Phone (default) | Single column, **16px** horizontal padding |
| Feed | No 3-column desktop layout — one scrollable column |
| Post card | `marginBottom: 16`, full width inside padding |
| Tablet (future) | Optional 2-column catalog only |

- Min touch target: **44×44px**
- Primary CTA: full width on phone, `minHeight: 48`, `borderRadius: 8`
- Cards / post cards: `borderRadius: 16`, padding 16–24
- Avatar (feed): 40–48px circle; profile hero: 96px

---

## 5. Components (React Native)

### Button (primary)

- Background `colors.primary`
- Text `colors.onPrimary`, fontWeight 600
- Disabled: opacity 0.7 + `ActivityIndicator`

### TextInput

- Border 1px `outlineVariant`, radius 8, minHeight 48
- Error state: border `error` + caption below

### Feed tabs (social)

- Segmented control or two `Pressable` tabs
- Active: `primary` text or underline; inactive: `onSurfaceVariant`
- Labels: **Đề xuất** / **Đang theo dõi** (match web `feedTabs.js`)

### Post card (social)

- White card (`surfaceContainerLowest`), border `outlineVariant`, radius 16
- Header: avatar + display name + time
- Media: full width, maintain aspect ratio
- Action row: like, comment, save — min 44px touch targets
- **No share button** in v1 (out of scope)

### Screen states

Every screen must handle:

- Loading (`ActivityIndicator` or skeleton)
- Error (banner + **Thử lại** where web has retry)
- Empty (icon + message — use exact copy from web checklist)

See `mobile/docs/mobile-social-screen-checklist.md` per screen.

---

## 6. Navigation

Authenticated shell uses bottom tabs:

| Tab | Route | Label (suggested) |
|-----|-------|-------------------|
| Feed | `(tabs)/feed` | Trang chủ / Feed |
| Shop | `(tabs)/shop` | Cửa hàng (commerce — placeholder) |
| Profile | `(tabs)/profile` | Hồ sơ |

- Active tint: `primary`
- Social sub-screens (post detail, search, saved, etc.) live in root stack above tabs

Icons: `@expo/vector-icons` (MaterialIcons / Ionicons). Do not use web Material Symbols font.

---

## 7. Reference screens (stitch + web)

### Social (primary for feed/profile/post)

| Screen | Stitch folder | Web page |
|--------|---------------|----------|
| Feed | `frontend/stitch/social_feed/` | `SocialFeedPage.jsx` |
| Post detail | `frontend/stitch/post_detail/` | `PostDetailModal.jsx` |
| User profile | `frontend/stitch/user_profile/` | `SocialProfilePage.jsx` |
| Create post | `frontend/stitch/create_post/` | `PostFormModal.jsx` |
| Saved posts | `frontend/stitch/saved_post/` | `SocialSavedPostsPage.jsx` |
| Search posts | `frontend/stitch/search_post/` | `SocialSearchPostsPage.jsx` |

For each: read `DESIGN.md` + `screen.png`; use `code.html` as layout hint only (do not copy HTML/CSS).

### Commerce

| Screen | Stitch folder |
|--------|---------------|
| Home | `frontend/stitch/commerce_home/` |

### Porting rules

- Adapt for **vertical scroll** and thumb reach
- Do **not** shrink desktop 3-column social layout onto phone
- Port patterns from `mobile/docs/mobile-social-rn-adaptations.md`

---

## 8. Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-social-ui-map.md` | Which stitch folder per route |
| `mobile/docs/mobile-social-rn-adaptations.md` | FlatList, modals→screens, media |
| `mobile/docs/mobile-social-screen-checklist.md` | Empty/error copy per screen |
| `src/shared/theme/colors.js` | Runtime color tokens |
