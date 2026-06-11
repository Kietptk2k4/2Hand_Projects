# Mobile Design System - 2Hands

Version: 1.0  
Source: `frontend/stitch/commerce_home/DESIGN.md` and repo design tokens.

---

## 1. Brand

- Vertical: fashion second-hand C2C marketplace + social discovery
- Tone: professional, reliable, clean
- Font: Inter (load via `@expo-google-fonts/inter` when adding custom fonts; until then system sans-serif is acceptable for scaffold)

---

## 2. Colors

Use `src/shared/theme/colors.js`:

| Token | Hex | Usage |
|-------|-----|-------|
| primary | `#0050CB` | CTA, links |
| onPrimary | `#FFFFFF` | Text on primary buttons |
| surface | `#F9F9FF` | Screen background |
| surfaceContainerLowest | `#FFFFFF` | Cards |
| onSurface | `#111C2D` | Primary text |
| onSurfaceVariant | `#424656` | Secondary text |
| outlineVariant | `#C2C6D8` | Borders |
| error | `#BA1A1A` | Errors |

Do not hardcode random hex in screens — extend `colors.js` if needed.

---

## 3. Typography (mobile scale)

| Role | Size | Weight |
|------|------|--------|
| Screen title | 24px | 700 |
| Section title | 18px | 600 |
| Body | 14–16px | 400 |
| Label | 14px | 600 |
| Caption / error | 12px | 400 |

Use `headline-*-mobile` tokens from stitch when refining polish.

---

## 4. Spacing & Layout

8px base grid: `8, 16, 24, 32`.

| Breakpoint | Layout |
|------------|--------|
| Phone (default) | Single column, 16px horizontal padding |
| Tablet (future) | Optional 2-column catalog |

- Min touch target: **44×44px**
- Primary CTA: full width on phone, `minHeight: 48`, `borderRadius: 8`
- Cards: `borderRadius: 16`, padding 16–24

---

## 5. Components (React Native)

### Button (primary)

- Background `colors.primary`
- Text `colors.onPrimary`, fontWeight 600
- Disabled: opacity 0.7 + `ActivityIndicator`

### TextInput

- Border 1px `outlineVariant`, radius 8, minHeight 48
- Error state: border `error` + caption below

### Screen states

Every screen must handle:

- Loading (`ActivityIndicator`)
- Error (banner or inline text)
- Empty (illustration + CTA optional)

---

## 6. Navigation (planned)

Authenticated shell should use bottom tabs:

| Tab | Route |
|-----|-------|
| Home | Commerce catalog |
| Feed | Social feed |
| Cart | Cart |
| Profile | Account / orders |

Use icons + labels; active tint `primary`.

---

## 7. Reference Screens (web / stitch)

When implementing a screen, check:

- `frontend/stitch/{feature}/DESIGN.md`
- `frontend/stitch/{feature}/screen.png`
- Matching page in `frontend/src/fe-module/features/`

Adapt layout for thumb reach and vertical scroll — do not shrink desktop 3-column layouts.
