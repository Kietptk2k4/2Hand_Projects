# Mobile Commerce RN Adaptations - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Document how to port web commerce UI/logic to React Native — CommerceShell, lists, modals, PayOS, GHN, cart badge, ProductCard, theme, and media.

Read together with `mobile/docs/mobile-commerce-ui-map.md` and `frontend/src/fe-module/features/commerce/`.

---

## 1) Mindset

| Web | Mobile |
|-----|--------|
| Reference for **business flow**, API calls, error messages | Implementation target |
| `CommerceShell` + buyer/seller sidebars | Tab + stack only — **buyer v1, no seller sidebar** |
| Tailwind + HTML + modal overlays | `View`, `Text`, `Pressable`, `FlatList`, full screens |
| Desktop grid + sticky sidebar | Single column + inline sections |
| Copy-paste JSX | Port **hooks + api**; rebuild **components** |

---

## 2) CommerceShell → mobile

**Web:** `CommerceShell.jsx` wraps pages with optional `CommerceBuyerSidebar` / `CommerceSellerSidebar` (desktop `lg+` only) and `CommerceMobileCartButton` FAB.

**Mobile buyer v1:**

- **Do not** port `CommerceBuyerSidebar` as a column — use:
  - Shop tab = home
  - Header icons: search, cart, orders (optional)
  - Category nav inline on home (`CommerceCategoryNav`)
- **Do not** port `CommerceSellerSidebar` — seller is Phase 9 defer
- Replace `CommerceMobileCartButton` FAB with header cart icon + badge count
- Skip `CartFlyAnimationContext` — toast on add-to-cart is sufficient for v1

```javascript
// app/commerce/_layout.jsx — header right
<Pressable onPress={() => router.push(ROUTES.commerceCart)}>
  <MaterialIcons name="shopping-cart" size={24} color={colors.onSurface} />
  {cartCount > 0 ? <CartBadge count={cartCount} /> : null}
</Pressable>
```

---

## 3) Modals → screens

| Web modal / dialog | Mobile |
|--------------------|--------|
| `CreateUserAddressModal` / `UserAddressFormModal` | Inline form on `addresses.jsx` or pushed sub-screen |
| `ProductMediaLightbox` | RN `Modal` + full-screen `Image` |
| `CancelOrderConfirmDialog` | `Alert.alert` or confirm `Modal` |
| `AdminRestoreProductDialog` | **Never mobile** |

Use `router.push()` / `router.back()` from `expo-router`. Pass params via route, not modal state.

---

## 4) Lists & pagination

### 4.1 Product grid

**Web:** CSS grid with load-more or infinite scroll.

**Mobile:**

```javascript
<FlatList
  data={items}
  numColumns={2}
  keyExtractor={(item) => item.productId}
  renderItem={({ item }) => (
    <ProductCard product={item} onOpenProduct={openProduct} onAddToCart={addToCart} />
  )}
  onEndReached={loadMore}
  onEndReachedThreshold={0.4}
  columnWrapperStyle={{ gap: 12 }}
  ListFooterComponent={isLoadingMore ? <ActivityIndicator /> : null}
/>
```

- Use page size from `productListConstants.js` (align with web)
- Guard `loadMore` with `hasNext` from API meta
- Debounce `onEndReached` to avoid double fetch

### 4.2 Order list / reviews list

Single-column `FlatList` — same pagination pattern.

---

## 5) PayOS + expo-web-browser + deep link

**Web:** `window.open(payosUrl)` or redirect; return via route query.

**Mobile flow:**

1. `CreateOrder` → receive `payment_id`
2. `POST /commerce/api/v1/payments/{paymentId}/payos-checkout-url`
3. Open URL with `expo-web-browser`:

```javascript
import * as WebBrowser from "expo-web-browser";

await WebBrowser.openBrowserAsync(payosUrl, {
  showInRecents: true,
  createTask: false,
});
```

4. Configure return URL / deep link: `twohands://commerce/checkout/payment-result?paymentId={uuid}`
5. On `payment-result` screen: **poll** `GET /commerce/api/v1/payments/{paymentId}/status` — do not assume paid from browser dismiss alone
6. Terminal success → `checkout/success` or order detail

Register scheme in `app.json`:

```json
"scheme": "twohands"
```

Test on Android emulator with commerce-service + PayOS sandbox/mock.

---

## 6) GHN address picker

**Web:** `GhnAddressFields.jsx` with `<select>` for province / district / ward.

**Mobile:**

- Three `Picker` / bottom-sheet selects or searchable modals
- Port `useGhnAddressOptions` — fetch provinces, then districts on province change, wards on district change
- Reset dependent fields when parent changes (same as web)
- API: `ghnAddressApi.js` under `/commerce/api/v1/ghn/...`

Wrap form in `KeyboardAvoidingView` + `ScrollView keyboardShouldPersistTaps="handled"`.

---

## 7) Cart badge

**Web:** Sidebar cart link + fly animation pulse token.

**Mobile v1:**

- `useCart` or lightweight `useCartItemCount` hook
- Badge on commerce stack header cart icon
- Optional: badge on Shop tab in `(tabs)/_layout.jsx`
- Refetch cart count after add/remove and on focus (`useFocusEffect`)

---

## 8) ProductCard

Port from `frontend/.../components/ProductCard.jsx`:

| Web behavior | Mobile |
|--------------|--------|
| `formatVndPrice` | Port util — show sale price strikethrough when on sale |
| `ProductImageStickers` | Condition / sale badges overlay |
| `inStock` / `OUT_OF_STOCK` | Disable add-to-cart + buy now |
| 1-of-1 inventory | Max quantity 1 everywhere |
| Thumbnail | `resolveDevMediaUrl(product.thumbnailUrl)` |
| Card tap | `Pressable` → product detail |
| Shop link | Separate `Pressable` on shop name — stop propagation |

**Styling:** `useThemeColors()` — card background `surfaceContainerLowest`, border `outlineVariant`, radius 16.

Use `expo-image` for thumbnails with placeholder.

---

## 9) Theme (`useThemeColors`)

**Do not** import deprecated static `colors` in new commerce screens.

```javascript
import { useThemeColors } from "../../../shared/theme/useThemeColors";

export function ProductCard(props) {
  const colors = useThemeColors();
  // StyleSheet or inline styles referencing colors.*
}
```

Matches account and social modules already on mobile.

---

## 10) Media (`resolveDevMediaUrl`)

Product thumbnails, gallery images, and review media may return MinIO URLs with `localhost` / `127.0.0.1`.

```javascript
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";

<Image source={{ uri: resolveDevMediaUrl(product.thumbnailUrl) }} />
```

**Env:** set `EXPO_PUBLIC_DEV_HOST` to PC LAN IP on physical device (see `mobile/.env.example`). Android emulator uses `10.0.2.2` automatically when unset.

---

## 11) Styling (no Tailwind)

| Web | Mobile |
|-----|--------|
| `className="rounded-xl border..."` | `StyleSheet.create` + `useThemeColors()` |
| `material-symbols-outlined` | `@expo/vector-icons` |
| Hover states | `Pressable` pressed opacity |

Primary CTA: full width, `minHeight: 48`, `borderRadius: 8`, background `primary`.

---

## 12) Toast / feedback

**Web:** `FeedToast` on commerce home.

**Mobile:** reuse social toast pattern or `Alert.alert` for errors; keep exact Vietnamese strings from web hooks (`useCommerceAddToCart` success messages).

---

## 13) Social product tag entry

Update `PostProductTagsBlock` when commerce Phase 4+ ships:

```javascript
import { ROUTES } from "../../../shared/constants/routes";
router.push(ROUTES.commerceProductDetail(productId));
```

Replace stub navigation from social Phase 8.

---

## 14) Features explicitly not ported (buyer v1)

| Web UI | Mobile action |
|--------|---------------|
| Seller sidebar / seller pages | Defer Phase 9 |
| Admin moderation pages | Never |
| `NotificationBell` on commerce home | Defer |
| Cart fly animation | Skip — badge only |
| MSW dev mocks | Real API only |

---

## 15) Suggested dependencies

| Package | Use |
|---------|-----|
| `expo-image` | Product thumbnails, gallery |
| `expo-image-picker` | Review media upload |
| `expo-web-browser` | PayOS checkout |
| `@expo/vector-icons` | Cart, search, order icons |

Install with `npx expo install <package>` for SDK 56 compatibility.

---

## Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-commerce-screen-checklist.md` | Per-screen DoD |
| `mobile/docs/mobile-commerce-ui-map.md` | Routes |
| `mobile/docs/mobile-api-integration.md` | commerceApiClient, PayOS |
| `mobile/docs/mobile-design-system.md` | ProductCard tokens |
