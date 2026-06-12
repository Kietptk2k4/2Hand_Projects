# Mobile Commerce Scope - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Define what the native commerce module includes, excludes, and defers — so AI does not build seller/admin flows in buyer MVP or confuse commerce with social discovery.

---

## 1) Context

- **Web reference (complete buyer + seller):** `frontend/src/fe-module/features/commerce/` (~168 files)
- **API contracts (shared web + mobile):** `docs/api_fe_behavior/commerce_api_fe_behavior/` (74 files)
- **Functional requirements:** `docs/feature_requirements/commerce/` where present
- **Product vertical:** fashion second-hand C2C — see `docs/product-vision/fashion-secondhand-vertical.md`
- **Visual reference:** `frontend/stitch/commerce_home/` (more stitch folders TBD per screen)
- **Mobile stack:** Expo SDK 56, JavaScript, expo-router — see `mobile/docs/mobile-master-context.md`

Commerce on web is the **source of truth for business flow and API mapping**. Mobile reimplements UI with React Native primitives; it does not duplicate API behavior docs.

---

## 2) Critical distinction: Buyer vs Seller vs Admin

| Area | Mobile MVP | Web reference | Notes |
|------|------------|---------------|-------|
| **Buyer** (browse, cart, checkout, orders) | **Phase 1 — in scope** | `CommerceHomePage`, cart, checkout, orders | Tab **Cửa hàng** + commerce stack |
| **Seller** (shop, products, seller orders) | **Phase 2 — defer** | `CommerceSeller*Page`, `/commerce/seller/*` | Web-only for mobile v1 |
| **Admin** (moderation, finance) | **Never mobile** | `CommerceAdmin*Page`, `/admin/commerce/*` | Web admin console only |

On web, buyer pages use `CommerceBuyerSidebar`; seller pages use `CommerceSellerSidebar` inside `CommerceShell`. Mobile buyer v1 has **no seller sidebar** — only buyer navigation patterns.

---

## 3) MVP Phase 1 (buyer v1) — In Scope

### 3.1 Core navigation

| Item | Notes |
|------|-------|
| Shop tab | `(tabs)/shop.jsx` replaces placeholder — commerce home |
| Commerce stack | `app/commerce/*` for search, product, cart, checkout, orders |
| Auth gate | Cart, checkout, addresses, orders require JWT; 401 → session expired |
| Deep link scheme | `twohands://` — payment result: `twohands://commerce/checkout/payment-result?paymentId=...` |
| Social product tag entry | `PostProductTagsBlock` → `ROUTES.commerceProductDetail(productId)` |

### 3.2 Buyer screens and flows

| Flow | Mobile v1 | Web page |
|------|-----------|----------|
| Commerce home (product list) | Yes | `CommerceHomePage.jsx` |
| Search products | Yes | `CommerceSearchPage.jsx` |
| Category products | Yes | `CommerceCategoryProductsPage.jsx` |
| Product detail | Yes | `CommerceProductDetailPage.jsx` |
| Product reviews (read) | Yes | `CommerceProductReviewsPage.jsx` |
| Shop products | Yes | `CommerceShopProductsPage.jsx` |
| Shop reviews (read) | Yes | `CommerceShopReviewsPage.jsx` |
| Cart | Yes | `CommerceCartPage.jsx` |
| User addresses (CRUD) | Yes | `CommerceUserAddressesPage.jsx` |
| Checkout | Yes | `CommerceCheckoutPage.jsx` |
| PayOS payment result | Yes | `CommerceCheckoutPaymentResultPage.jsx` |
| Checkout success | Yes | `CommerceCheckoutSuccessPage.jsx` |
| Order list | Yes | `CommerceOrderListPage.jsx` |
| Order detail | Yes | `CommerceOrderDetailPage.jsx` |
| Shipment tracking | Yes | `CommerceShipmentTrackingPage.jsx` |
| Write / edit product review | Yes | `CommerceWriteEditReviewPage.jsx` |

### 3.3 API layer to port (in `mobile/src/features/commerce/api/`)

Mirror web module names; prefix all paths with `/commerce/api/v1`:

| API module | Behavior doc(s) |
|------------|-----------------|
| `productListApi.js` | SearchProduct, FilterProductsByCategory, ViewProductList |
| `productDetailApi.js` | ViewProductDetail |
| `productReviewsApi.js` | ViewProductReviews |
| `productReviewWriteApi.js` | CreateProductReview, UpdateProductReview, ViewMyProductReview, UploadReviewMedia |
| `shopProductsApi.js` | ViewShopProducts (public shop catalog) |
| `shopReviewsApi.js` | ViewShopReviews, ViewPublicShopReviews |
| `cartApi.js` | ViewCart, CreateCart, AddCartItem, UpdateCartItemQuantity, RemoveCartItem, ValidateCartItems |
| `userAddressApi.js` | CreateUserAddress, UpdateUserAddress, DeleteUserAddress, SetDefaultUserAddress, ListUserAddresses |
| `ghnAddressApi.js` | GHN province/district/ward lookup |
| `checkoutApi.js` | CalculateOrderTotal, CalculateShippingFee, CreateOrder |
| `paymentApi.js` | CreatePayOSCheckoutUrl, ViewPaymentStatus |
| `orderApi.js` | ViewOrderList, ViewOrderDetail, CancelOrder, ConfirmOrderReceived |
| `shipmentApi.js` | TrackShipment, ViewShipment |
| `categoriesApi.js` | Category tree for home nav |

Shared HTTP client: `mobile/src/services/http/commerceApiClient.js` (mirror `frontend/src/fe-module/services/http/commerceApiClient.js`).

Response helpers: port `commerceApiResponse.js` (`unwrapResponse`, `mapAxiosError`) — same pattern as web.

### 3.4 Media URLs (`resolveDevMediaUrl`)

Product thumbnails and review media may return MinIO URLs with `localhost` or `127.0.0.1`. On physical devices those hosts are unreachable.

- Use `resolveDevMediaUrl()` from `mobile/src/shared/utils/resolveDevMediaUrl.js` for **every** product/review image URL before passing to `<Image source={{ uri }} />`.
- Set `EXPO_PUBLIC_DEV_HOST` to PC LAN IP on physical device (see `mobile/.env.example`).
- Android emulator: `getDevMediaHost()` returns `10.0.2.2` automatically when `EXPO_PUBLIC_DEV_HOST` is unset.

### 3.5 Non-functional requirements (buyer v1)

- Loading, error (+ retry), and empty states on every list screen
- Pagination via `FlatList` `onEndReached` (page size aligned with web `productListConstants.js`)
- JWT via existing auth refresh flow; no tokens in logs
- Field mapping: support `snake_case` from backend per API behavior docs
- UI tokens via `useThemeColors()` — see `mobile/docs/mobile-design-system.md`
- Vietnamese copy matches web where specified in screen checklist
- 1-of-1 closet inventory: out-of-stock products show disabled add-to-cart (same as web `ProductCard`)

---

## 4) Out of Scope (buyer v1)

Do **not** implement in mobile buyer v1 unless explicitly requested:

| Item | Reason |
|------|--------|
| **Seller flows** | Phase 2 — create shop, seller products, seller orders, seller shipments, seller analytics |
| **Admin moderation** | Web-only — `CommerceAdminShopModerationPage`, review/product removal |
| **Notification bell on commerce home** | Depends on mobile notification module |
| **Desktop buyer sidebar** | `CommerceBuyerSidebar` hidden on mobile web — use tab + stack headers |
| **Cart fly animation** | Web `CartFlyAnimationContext` — optional polish; badge count is sufficient for v1 |
| **Backend / Kafka / outbox** | Server-side only |
| **Mock MSW handlers** | Web dev only — mobile hits real commerce-service |

### v1 UX for deferred seller entry

If web copy references "Bán hàng" or seller dashboard links:

1. Hide the row on mobile, or
2. Show **"Tính năng đang được phát triển."** — do not stub seller APIs.

---

## 5) Phase 2 (seller — defer)

| Feature | Web reference |
|---------|---------------|
| Create shop | `CommerceCreateShopPage.jsx` |
| Shop settings | `CommerceShopSettingsPage.jsx` |
| Seller product list / form | `CommerceSellerProductListPage.jsx`, `CommerceSellerProductFormPage.jsx` |
| Seller orders | `CommerceSellerOrderListPage.jsx`, `CommerceSellerOrderDetailPage.jsx` |
| Seller shipments | `CommerceSellerShipmentListPage.jsx`, `CommerceSellerShipmentDetailPage.jsx` |
| Seller shop reviews | `CommerceSellerShopReviewsPage.jsx` |
| Seller analytics | `CommerceSellerAnalyticsPage.jsx` |

Documented as **Phase 9** in `mobile-commerce-implementation-order.md`. Do not start until buyer Phase 0–8 is complete and tested.

---

## 6) Admin — never mobile

| Web route | Reason |
|-----------|--------|
| `/admin/commerce/shops` | Admin shop moderation |
| `/admin/commerce/reviews` | Admin review moderation |
| `/admin/commerce/products` | Admin product removal |

No mobile routes, no API stubs, no hidden admin screens.

---

## 7) Dependencies on other mobile modules

| Module | Dependency |
|--------|------------|
| **Auth** | Login, `authApiClient`, token storage, session clear, `useAuthSession` for PayOS 401 |
| **Social** | Product tag navigation from posts → commerce product detail; `useViewCommerceProduct` pattern |
| **Notification** | Optional bell on commerce home — defer |

**Prerequisites before commerce work:**

- Working login + token storage (`src/features/auth/`, `src/services/auth/tokenStorage.js`)
- `socialApiClient.js` exists (social v1 done)
- `(tabs)/shop.jsx` placeholder exists — replace in Phase 1

---

## 8) Environment and API ports

Copy `mobile/.env.example` to `mobile/.env`:

```env
EXPO_PUBLIC_AUTH_SERVICE_BASE_URL=http://10.0.2.2:3001
EXPO_PUBLIC_SOCIAL_SERVICE_BASE_URL=http://10.0.2.2:3002
EXPO_PUBLIC_COMMERCE_SERVICE_BASE_URL=http://10.0.2.2:3003
EXPO_PUBLIC_NOTIFICATION_SERVICE_BASE_URL=http://10.0.2.2:3005
EXPO_PUBLIC_DEV_HOST=192.168.1.4
```

| Service | Port (local) | API prefix | Mobile client |
|---------|--------------|------------|---------------|
| auth-service | **3001** | `/api/v1/auth`, `/api/v1/users` | `authApiClient.js` |
| social-service | **3002** | `/api/v1/social/*` | `socialApiClient.js` (exists) |
| commerce-service | **3003** | `/commerce/api/v1/*` | `commerceApiClient.js` (Phase 0) |
| notification-service | **3005** | `/api/v1/notification/*` | defer |

**Physical device:** replace `10.0.2.2` with PC LAN IP for all `EXPO_PUBLIC_*_SERVICE_BASE_URL` values; set `EXPO_PUBLIC_DEV_HOST` to same IP (no protocol).

Restart Metro after `.env` changes: `npx expo start --clear`.

---

## 9) Definition of Done (commerce buyer feature)

- [ ] Screen listed in `mobile/docs/mobile-commerce-ui-map.md` exists under `app/commerce/` or `(tabs)/shop`
- [ ] Business logic in `src/features/commerce/` (api + hooks + components + utils)
- [ ] Matches relevant `docs/api_fe_behavior/commerce_api_fe_behavior/*` contract
- [ ] Loading / error / empty / pagination states per screen checklist
- [ ] Product/review images use `resolveDevMediaUrl`
- [ ] Screens use `useThemeColors()` (not deprecated static `colors` import)
- [ ] No axios calls in `app/*.jsx`
- [ ] PayOS flow tested on emulator (browser return + deep link)
- [ ] UTF-8 encoding on all new files (Windows)

---

## 10) Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-commerce-ui-map.md` | Web page → mobile route mapping |
| `mobile/docs/mobile-commerce-implementation-order.md` | Build sequence and file checklist |
| `mobile/docs/mobile-commerce-screen-checklist.md` | DoD per screen |
| `mobile/docs/mobile-commerce-rn-adaptations.md` | Web → React Native patterns |
| `mobile/docs/mobile-convention.md` | Naming and folder rules |
| `mobile/docs/mobile-api-integration.md` | commerceApiClient, PayOS, media |
| `mobile/docs/mobile-design-system.md` | Colors, ProductCard, commerce stitch |
