# Mobile Commerce Screen Checklist - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Definition of Done per commerce buyer screen — loading, error, empty, pagination, auth, PayOS, and navigation — so AI and reviewers verify each screen before merge.

---

## How to use

1. Pick one screen from the table below.
2. Read its **Web source**, **API behavior** docs, and **Stitch** folder.
3. Implement route under `app/commerce/` or `(tabs)/shop` + logic under `src/features/commerce/`.
4. Check every box in **States**, **Actions**, and **Quality** before marking done.
5. Cross-check `mobile/docs/mobile-commerce-ui-map.md` for component mapping.

**Global rules (all screens):**

- [ ] No `axios` / `fetch` in `app/*.jsx`
- [ ] JWT attached via `commerceApiClient` (not manual headers in screens)
- [ ] Response unwrapped per `mobile/docs/mobile-api-integration.md`
- [ ] Colors from `useThemeColors()` — not deprecated static `colors` import
- [ ] Product/review images via `resolveDevMediaUrl()` before `<Image />`
- [ ] UTF-8 encoding on new files (Windows)
- [ ] Vietnamese copy matches web where specified below
- [ ] No seller or admin API calls / routes

---

## Screen index

| # | Screen | Route | Phase |
|---|--------|-------|-------|
| 1 | Commerce home | `app/(tabs)/shop.jsx` | 2 |
| 2 | Search products | `app/commerce/search.jsx` | 3 |
| 3 | Category products | `app/commerce/categories/[categoryId].jsx` | 3 |
| 4 | Product detail | `app/commerce/products/[productId].jsx` | 4 |
| 5 | Product reviews | `app/commerce/products/[productId]/reviews.jsx` | 4 |
| 6 | Shop products | `app/commerce/shops/[shopId].jsx` | 4 |
| 7 | Shop reviews | `app/commerce/shops/[shopId]/reviews.jsx` | 4 |
| 8 | Cart | `app/commerce/cart.jsx` | 5 |
| 9 | User addresses | `app/commerce/addresses.jsx` | 6 |
| 10 | Checkout | `app/commerce/checkout/index.jsx` | 7 |
| 11 | Payment result | `app/commerce/checkout/payment-result.jsx` | 7 |
| 12 | Checkout success | `app/commerce/checkout/success.jsx` | 7 |
| 13 | Order list | `app/commerce/orders/index.jsx` | 8 |
| 14 | Order detail | `app/commerce/orders/[orderId].jsx` | 8 |
| 15 | Shipment tracking | `app/commerce/orders/[orderId]/shipments/[shipmentId].jsx` | 8 |
| 16 | Write / edit review | `app/commerce/reviews/new.jsx`, `.../edit.jsx` | 8 |

---

## 1) Commerce home

| Field | Value |
|-------|-------|
| **Route** | `app/(tabs)/shop.jsx` |
| **Web** | `frontend/src/fe-module/features/commerce/pages/CommerceHomePage.jsx` |
| **Stitch** | `frontend/stitch/commerce_home/` |
| **API** | `ViewProductList-api-and-behavior.md`, categories |
| **Hooks** | `useProductList`, `useCommerceCategories`, `useCommerceAddToCart` |
| **Components** | `CommerceHomeHero`, `CommerceCategoryNav`, `ProductCard`, `ProductListSortSelect`, `ProductListSkeleton` |

### States

- [ ] **Loading (initial):** `ProductListSkeleton` grid
- [ ] **Loading more:** footer `ActivityIndicator` on `onEndReached`
- [ ] **Error:** message + **Thử lại**
- [ ] **Empty:** no products message per web
- [ ] **Ready:** 2-column `FlatList` of `ProductCard`

### Actions & navigation

- [ ] Sort select refetches list
- [ ] Category chip → category products route
- [ ] Tap product → product detail
- [ ] Tap shop name on card → shop products (if shown)
- [ ] Add to cart / buy now on card
- [ ] Hero search → search screen
- [ ] Header cart icon → cart (Phase 5+)

### Quality

- [ ] Thumbnails use `resolveDevMediaUrl`
- [ ] Out-of-stock: disabled add-to-cart
- [ ] `useThemeColors()` throughout

---

## 2) Search products

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/search.jsx` |
| **Web** | `CommerceSearchPage.jsx` |
| **API** | `SearchProduct-api-and-behavior.md` |
| **Hooks** | `useProductSearch` |

### States

- [ ] **Idle:** search input, hint for min keyword length
- [ ] **Loading / loading more / error / empty / ready** — same pattern as home grid

### Actions

- [ ] Debounced search submit
- [ ] Product tap → detail
- [ ] Back returns to previous screen

---

## 3) Category products

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/categories/[categoryId].jsx` |
| **Web** | `CommerceCategoryProductsPage.jsx` |
| **API** | `FilterProductsByCategory-api-and-behavior.md` |
| **Hooks** | `useCategoryProducts` |

### States & actions

- [ ] Same grid states as home
- [ ] Category name in header
- [ ] Sort + pagination

---

## 4) Product detail

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/products/[productId].jsx` |
| **Web** | `CommerceProductDetailPage.jsx` |
| **API** | `ViewProductDetail-api-and-behavior.md` |
| **Hooks** | `useProductDetail`, `useCommerceAddToCart`, `useCommerceBuyNow` |

### States

- [ ] **Loading:** `ProductDetailSkeleton`
- [ ] **Error:** not found / network + retry
- [ ] **Ready:** gallery, price, condition, description, shop card, reviews preview

### Actions

- [ ] Add to cart / buy now
- [ ] Open shop → shop products
- [ ] See all reviews → reviews screen
- [ ] All media URLs via `resolveDevMediaUrl`

### Auth

- [ ] Browse may be public if web allows; cart/checkout actions require login

---

## 5) Cart

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/cart.jsx` |
| **Web** | `CommerceCartPage.jsx` |
| **API** | `ViewCart`, cart mutations, `ValidateCartItems` |
| **Hooks** | `useCart`, `useValidateCartItems` |

### States

- [ ] **Loading:** `CartSkeleton`
- [ ] **Empty:** empty cart message + link to shop
- [ ] **Warnings:** `CartWarningsBanner` when items invalid
- [ ] **Error + retry**

### Actions

- [ ] Update quantity (1-of-1 max quantity rules)
- [ ] Remove line item
- [ ] Checkout → validate then navigate with `cartItemIds`
- [ ] **Auth required** — 401 → session expired

---

## 6) User addresses

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/addresses.jsx` |
| **Web** | `CommerceUserAddressesPage.jsx` |
| **API** | Address CRUD + set default + GHN lookup |
| **Hooks** | `useUserAddresses`, `useGhnAddressOptions` |

### States & actions

- [ ] List addresses with default badge
- [ ] Create / edit inline form (not web modal)
- [ ] GHN province → district → ward cascading
- [ ] Delete with confirm
- [ ] Set default
- [ ] **Auth required**

---

## 7) Checkout

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/checkout/index.jsx` |
| **Web** | `CommerceCheckoutPage.jsx` |
| **API** | `CalculateOrderTotal`, `CalculateShippingFee`, `CreateOrder` |
| **Hooks** | `useCheckout` |

### States & actions

- [ ] Select shipping address (link to addresses if none)
- [ ] Order review lines + shipping fee + total
- [ ] Payment method PayOS
- [ ] Place order → payment result with `paymentId`
- [ ] **Auth required**

---

## 8) Payment result

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/checkout/payment-result.jsx` |
| **Web** | `CommerceCheckoutPaymentResultPage.jsx` |
| **API** | `CreatePayOSCheckoutUrl`, `ViewPaymentStatus` |
| **Hooks** | `usePaymentStatus`, `usePayOsCheckout` |

### States & actions

- [ ] Invalid/missing `paymentId` → redirect shop home
- [ ] Poll payment status until terminal state
- [ ] Retry PayOS when allowed
- [ ] Success → checkout success or order detail
- [ ] Deep link entry works from `expo-web-browser` return

---

## 9) Checkout success

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/checkout/success.jsx` |
| **Web** | `CommerceCheckoutSuccessPage.jsx` |

### Actions

- [ ] Show order confirmation summary
- [ ] Navigate to order detail or continue shopping

---

## 10) Order list

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/orders/index.jsx` |
| **Web** | `CommerceOrderListPage.jsx` |
| **API** | `ViewOrderList-api-and-behavior.md` |
| **Hooks** | `useOrderList` |

### States

- [ ] Loading / error / empty (`OrderListEmptyState`) / paginated list

### Actions

- [ ] Tap order → order detail
- [ ] **Auth required**

---

## 11) Order detail

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/orders/[orderId].jsx` |
| **Web** | `CommerceOrderDetailPage.jsx` |
| **API** | `ViewOrderDetail`, cancel, confirm received |
| **Hooks** | `useOrderDetail`, `useCancelOrder`, `useConfirmOrderReceived` |

### Actions

- [ ] Timeline / status display
- [ ] Cancel order (confirm dialog)
- [ ] Confirm received when eligible
- [ ] Track shipment → tracking screen
- [ ] Write review when eligible → review create route

---

## 12) Shipment tracking

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/orders/[orderId]/shipments/[shipmentId].jsx` |
| **Web** | `CommerceShipmentTrackingPage.jsx` |
| **API** | `TrackShipment`, `ViewShipment` |
| **Hooks** | `useShipmentTrackingPage` |

### States & actions

- [ ] Timeline of tracking events
- [ ] Items in shipment section
- [ ] Error + retry

---

## 13) Product reviews (read)

| Field | Value |
|-------|-------|
| **Route** | `app/commerce/products/[productId]/reviews.jsx` |
| **Web** | `CommerceProductReviewsPage.jsx` |
| **Hooks** | `useProductReviews` |

### Quality

- [ ] Review media via `resolveDevMediaUrl`
- [ ] Pagination
- [ ] Summary stats if on web

---

## 14) Write / edit review

| Field | Value |
|-------|-------|
| **Routes** | `app/commerce/reviews/new.jsx`, `app/commerce/reviews/[reviewId]/edit.jsx` |
| **Web** | `CommerceWriteEditReviewPage.jsx` |
| **Hooks** | `useReviewFormPage`, `useUploadReviewMedia` |

### Actions

- [ ] Star rating required
- [ ] Text + optional images (`expo-image-picker`)
- [ ] Submit create or update
- [ ] **Auth required**

---

## Related documents

| Document | Role |
|----------|------|
| `mobile/docs/mobile-commerce-ui-map.md` | Routes & components |
| `mobile/docs/mobile-commerce-rn-adaptations.md` | RN patterns |
| `mobile/docs/mobile-design-system.md` | ProductCard, tokens |
