# Mobile Commerce Implementation Order - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Step-by-step build sequence, dependencies, and file checklist so AI implements commerce buyer module in the correct order without breaking auth/social or building seller/admin flows.

---

## 1) Prerequisites (must be done first)

| Step | Status | Files / notes |
|------|--------|---------------|
| Auth login works | Exists | `app/(auth)/login.jsx`, `src/features/auth/api/authApi.js` |
| Token storage + refresh | Exists | `tokenStorage.js`, `authApiClient.js`, `authRefreshService.js` |
| Social v1 done | Exists | `socialApiClient.js`, feed, post detail, product tags stub |
| Env commerce URL | Configured | `EXPO_PUBLIC_COMMERCE_SERVICE_BASE_URL` in `.env` |
| commerce-service running | Dev | Port **3003**, prefix `/commerce/api/v1` |
| `(tabs)/shop.jsx` placeholder | Exists | Replace in Phase 1 |
| Read commerce docs | Required | `mobile-commerce-scope.md`, `mobile-commerce-ui-map.md`, this file |

**Gate:** User can log in, browse social feed, and open Shop tab before commerce API work.

---

## 2) Implementation phases

```text
Phase 0: HTTP + constants + ROUTES
    ↓
Phase 1: Shop tab shell + commerce stack layout
    ↓
Phase 2: Commerce home (product list + categories)
    ↓
Phase 3: Search + category products
    ↓
Phase 4: Product detail + shop products + reviews read
    ↓
Phase 5: Cart + add-to-cart + badge
    ↓
Phase 6: User addresses + GHN picker
    ↓
Phase 7: Checkout + PayOS + payment result + success
    ↓
Phase 8: Orders + tracking + write/edit review + social tag wiring
    ↓
Phase 9 (defer): Seller flows — web only until buyer v1 done
```

Each phase should be mergeable and testable on emulator before the next.

---

## 3) Phase 0 — Commerce HTTP foundation

**Goal:** Call commerce API with same auth/unwrap pattern as web.

### Create

| File | Action |
|------|--------|
| `src/services/http/commerceApiClient.js` | Create — mirror `frontend/.../commerceApiClient.js`; base URL from `EXPO_PUBLIC_COMMERCE_SERVICE_BASE_URL`; paths prefixed `/commerce/api/v1` |
| `src/features/commerce/api/commerceApiResponse.js` | Port `unwrapResponse`, `mapAxiosError` from web |
| `src/shared/constants/routes.js` | Add `ROUTES.commerce*` helpers (see ui-map) |
| `src/features/commerce/constants/productListConstants.js` | Port page size, sort options |

### Port API modules (minimal for Phase 2)

| File | Priority |
|------|----------|
| `productListApi.js` | P0 |
| `categoriesApi.js` | P0 |
| `productListKeys.js` | P0 |

### Verify

- Manual: `GET /commerce/api/v1/products?page=0&size=20` returns unwrapped items with valid JWT.

---

## 4) Phase 1 — Commerce stack + shop tab shell

**Goal:** Shop tab navigates to commerce home placeholder; commerce stack registered.

### Create

| File | Action |
|------|--------|
| `app/commerce/_layout.jsx` | Stack navigator with header |
| `app/(tabs)/shop.jsx` | Replace placeholder — wire to `CommerceHomeScreen` stub |
| `app/_layout.jsx` | Register `commerce` stack |

### Verify

- [ ] Login → Shop tab opens without crash
- [ ] Can push `/commerce/search` (empty screen OK)

---

## 5) Phase 2 — Commerce home

**Goal:** Product grid, sort, category nav, skeleton/error/empty, pagination.

### Port hooks

| File | Source (web) |
|------|----------------|
| `hooks/useProductList.js` | `useProductList.js` |
| `hooks/useCommerceCategories.js` | `useCommerceCategories.js` |

### Create components

| File | Source (web) |
|------|----------------|
| `components/CommerceHomeScreen.jsx` | `CommerceHomePage.jsx` |
| `components/CommerceHomeHero.jsx` | `CommerceHomeHero.jsx` |
| `components/CommerceCategoryNav.jsx` | Category nav from home sidebar |
| `components/ProductCard.jsx` | `ProductCard.jsx` |
| `components/ProductListSkeleton.jsx` | `ProductListSkeleton.jsx` |
| `components/ProductListSortSelect.jsx` | `ProductListSortSelect.jsx` |
| `utils/formatVndPrice.js` | Port from social/commerce |

### Update screen

| File | Work |
|------|------|
| `app/(tabs)/shop.jsx` | Compose home screen |

### API behavior docs

- `ViewProductList-api-and-behavior.md`

### Verify

- [ ] Grid renders with `resolveDevMediaUrl` on thumbnails
- [ ] Sort changes refetch list
- [ ] Category tap → category route (can be stub until Phase 3)
- [ ] `useThemeColors()` on all components
- [ ] Error + **Thử lại**; empty state copy from checklist

---

## 6) Phase 3 — Search + category products

**Goal:** Keyword search and category-filtered product lists.

### Create routes

| File |
|------|
| `app/commerce/search.jsx` |
| `app/commerce/categories/[categoryId].jsx` |

### Port

| File | Source |
|------|--------|
| `hooks/useProductSearch.js` | web |
| `hooks/useCategoryProducts.js` | web |
| `api/productSearchApi.js` | web |
| `api/categoryProductsApi.js` | web |
| `components/CommerceSearchScreen.jsx` | `CommerceSearchPage.jsx` |
| `components/CommerceCategoryProductsScreen.jsx` | `CommerceCategoryProductsPage.jsx` |

### Verify

- [ ] Search debounce + min keyword length (web constants)
- [ ] Category title from API or param
- [ ] Pagination on both screens

---

## 7) Phase 4 — Product detail + shop + reviews read

**Goal:** Full product page; shop storefront; paginated reviews.

### Create routes

| File |
|------|
| `app/commerce/products/[productId].jsx` |
| `app/commerce/products/[productId]/reviews.jsx` |
| `app/commerce/shops/[shopId].jsx` |
| `app/commerce/shops/[shopId]/reviews.jsx` |

### Port hooks / API

| File |
|------|
| `hooks/useProductDetail.js`, `useShopProducts.js`, `useProductReviews.js`, `useShopReviews.js` |
| `hooks/useCommerceAddToCart.js`, `useCommerceBuyNow.js` |
| `api/productDetailApi.js`, `shopProductsApi.js`, `productReviewsApi.js`, `shopReviewsApi.js`, `cartApi.js` |

### Verify

- [ ] Out-of-stock disables add-to-cart (1-of-1 inventory)
- [ ] Gallery carousel for product media
- [ ] Shop card navigates to shop products
- [ ] Reviews preview → full reviews screen

---

## 8) Phase 5 — Cart

**Goal:** View cart, update quantity, remove items, validate before checkout.

### Create

| File |
|------|
| `app/commerce/cart.jsx` |
| `hooks/useCart.js`, `useValidateCartItems.js` |
| `components/CartScreen.jsx`, `CartLineItem.jsx`, `CartQuantityStepper.jsx`, `CartWarningsBanner.jsx` |

### Cart badge

- Header cart icon on commerce stack + optional tab badge (see rn-adaptations)

### Verify

- [ ] Auth required; 401 → login
- [ ] Warnings banner when validation fails
- [ ] Checkout button passes selected `cartItemIds`

---

## 9) Phase 6 — User addresses

**Goal:** CRUD addresses with GHN province/district/ward picker.

### Create

| File |
|------|
| `app/commerce/addresses.jsx` |
| `hooks/useUserAddresses.js`, `useGhnAddressOptions.js` |
| `api/userAddressApi.js`, `ghnAddressApi.js` |
| `components/UserAddressesScreen.jsx`, `UserAddressCard.jsx`, `GhnAddressFields.jsx` |

### Verify

- [ ] Create / edit / delete / set default
- [ ] GHN cascading selects load correctly
- [ ] Form validation matches web

---

## 10) Phase 7 — Checkout + PayOS

**Goal:** Create order, open PayOS in browser, handle deep link return, poll payment status.

### Create routes

| File |
|------|
| `app/commerce/checkout/index.jsx` |
| `app/commerce/checkout/payment-result.jsx` |
| `app/commerce/checkout/success.jsx` |

### Port

| File |
|------|
| `hooks/useCheckout.js`, `usePayOsCheckout.js`, `usePaymentStatus.js` |
| `api/checkoutApi.js`, `paymentApi.js` |
| `components/CheckoutScreen.jsx`, `PaymentStatusPanel.jsx` |

### Native

- `expo-web-browser` for PayOS URL
- Deep link: `twohands://commerce/checkout/payment-result?paymentId=`
- Register linking in `app.json`

### Verify

- [ ] Shipping fee recalculates when address changes
- [ ] PayOS flow on emulator (mock or sandbox)
- [ ] Poll until terminal payment status
- [ ] Success screen shows order summary

---

## 11) Phase 8 — Orders + tracking + reviews write

**Goal:** Order history, detail actions, shipment tracking, create/edit product review.

### Create routes

| File |
|------|
| `app/commerce/orders/index.jsx` |
| `app/commerce/orders/[orderId].jsx` |
| `app/commerce/orders/[orderId]/shipments/[shipmentId].jsx` |
| `app/commerce/reviews/new.jsx` |
| `app/commerce/reviews/[reviewId]/edit.jsx` |

### Port

| File |
|------|
| `hooks/useOrderList.js`, `useOrderDetail.js`, `useCancelOrder.js`, `useConfirmOrderReceived.js` |
| `hooks/useShipmentTrackingPage.js`, `useReviewFormPage.js`, `useUploadReviewMedia.js` |
| `api/orderApi.js`, `orderListApi.js`, `orderDetailApi.js`, `shipmentApi.js`, `productReviewWriteApi.js` |

### Social wiring

- Update `PostProductTagsBlock` → `ROUTES.commerceProductDetail(productId)`

### Verify

- [ ] Cancel order confirm dialog
- [ ] Confirm received when eligible
- [ ] Tracking timeline renders GHN events
- [ ] Review form with star rating + optional media upload

---

## 12) Phase 9 (defer) — Seller

| Task | Web reference |
|------|---------------|
| Create shop | `CommerceCreateShopPage.jsx` |
| Shop settings | `CommerceShopSettingsPage.jsx` |
| Seller products | `CommerceSellerProductListPage.jsx`, form pages |
| Seller orders / shipments | Seller order/shipment pages |
| Seller analytics | `CommerceSellerAnalyticsPage.jsx` |

Do **not** start until buyer Phases 0–8 complete and tested.

---

## 13) Suggested AI task breakdown (one PR each)

| # | Task | Deliverable |
|---|------|-------------|
| 1 | Phase 0 | `commerceApiClient` + response helpers + ROUTES |
| 2 | Phase 1–2 | Shop tab + commerce home grid |
| 3 | Phase 3 | Search + category products |
| 4 | Phase 4 | Product detail + shop + reviews read |
| 5 | Phase 5 | Cart + badge |
| 6 | Phase 6 | Addresses + GHN |
| 7 | Phase 7 | Checkout + PayOS |
| 8 | Phase 8 | Orders + tracking + review write + social tags |

---

## 14) Testing checklist (manual on emulator)

1. Start commerce-service (3003) + auth (3001).
2. Login; open Shop tab — products load.
3. Search keyword; open category; open product detail.
4. Add to cart; open cart; update quantity.
5. Add address with GHN picker.
6. Checkout → PayOS browser → return via deep link → success.
7. View order list and detail; track shipment if available.
8. Write product review from order or product page.
9. Tap product tag on social post → product detail.

---

## 15) Files not to create on mobile (buyer v1)

| Web-only | Reason |
|----------|--------|
| `CommerceSellerSidebar`, seller pages | Phase 9 defer |
| `CommerceAdmin*` pages | Never mobile |
| `CartFlyAnimationContext` | Optional polish — badge sufficient |
| MSW mock handlers | Mobile hits real API |

---

## 16) Related documents

| Document | When to read |
|----------|--------------|
| `mobile/docs/mobile-commerce-scope.md` | Before any commerce task |
| `mobile/docs/mobile-commerce-ui-map.md` | Per screen |
| `mobile/docs/mobile-commerce-screen-checklist.md` | Before marking done |
| `mobile/docs/mobile-commerce-rn-adaptations.md` | Web → RN patterns |
| `mobile/docs/mobile-convention.md` | Naming |
| `mobile/AGENTS.md` | Agent entry |

---

## 17) Prompt template

```text
Implement Phase [N] of mobile commerce per mobile/docs/mobile-commerce-implementation-order.md.

Read first:
- mobile/docs/mobile-commerce-scope.md
- mobile/docs/mobile-commerce-ui-map.md
- mobile/docs/mobile-commerce-implementation-order.md (Phase [N])
- mobile/docs/mobile-commerce-screen-checklist.md
- mobile/docs/mobile-commerce-rn-adaptations.md
- docs/api_fe_behavior/commerce_api_fe_behavior/[relevant].md

Port from frontend/src/fe-module/features/commerce/ where listed.
Do not modify backend. UTF-8 files only. No API calls in app/*.jsx.
Use useThemeColors() and resolveDevMediaUrl() on all product/review images.
Do not implement seller or admin flows.
```
