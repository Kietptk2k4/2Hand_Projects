# Mobile Commerce UI Map - 2Hands

Version: 1.0
Owner: Mobile Team
Purpose: Map every web commerce buyer surface to its mobile route, components, and design references — so AI ports layout correctly (single column, screens not modals, no seller/admin).

---

## 1) Layout principle (web vs mobile)

| Web (`CommerceShell`) | Mobile |
|-----------------------|--------|
| `CommerceBuyerSidebar` + content (`lg:sticky`, hidden below `lg`) | **Omit sidebar** — category nav inline on home; orders/cart via header icons |
| `CommerceSellerSidebar` on seller paths | **Never mobile v1** — defer Phase 9 |
| `CommerceMobileCartButton` floating FAB | Header cart icon + optional tab badge |
| `CartFlyAnimationContext` fly-to-cart | **Skip v1** — toast + badge count |
| `CreateUserAddressModal` / `UserAddressFormModal` | **Full screen** `app/commerce/addresses.jsx` |
| `ProductMediaLightbox` | RN `Modal` + full-screen `Image` |
| Grid product list | `FlatList` `numColumns={2}` |
| `NotificationBell` on commerce home | **Defer** |

---

## 2) Route map (expo-router)

```text
app/
├── (tabs)/shop.jsx                         # CommerceHomePage
├── commerce/
│   ├── _layout.jsx
│   ├── search.jsx                          # CommerceSearchPage
│   ├── categories/[categoryId].jsx         # CommerceCategoryProductsPage
│   ├── products/[productId].jsx            # CommerceProductDetailPage
│   ├── products/[productId]/reviews.jsx    # CommerceProductReviewsPage
│   ├── shops/[shopId].jsx                  # CommerceShopProductsPage
│   ├── shops/[shopId]/reviews.jsx          # CommerceShopReviewsPage
│   ├── cart.jsx                            # CommerceCartPage
│   ├── addresses.jsx                       # CommerceUserAddressesPage
│   ├── checkout/index.jsx                  # CommerceCheckoutPage
│   ├── checkout/payment-result.jsx         # CommerceCheckoutPaymentResultPage
│   ├── checkout/success.jsx                # CommerceCheckoutSuccessPage
│   ├── orders/index.jsx                    # CommerceOrderListPage
│   ├── orders/[orderId].jsx                # CommerceOrderDetailPage
│   ├── orders/[orderId]/shipments/[shipmentId].jsx  # CommerceShipmentTrackingPage
│   └── reviews/new.jsx                     # CommerceWriteEditReviewPage
│       reviews/[reviewId]/edit.jsx
```

Deep link: `twohands://commerce/checkout/payment-result?paymentId=<uuid>`

---

## 3) ROUTES helpers (`src/shared/constants/routes.js`)

```javascript
commerceHome: "/(tabs)/shop",
commerceSearch: "/commerce/search",
commerceCategoryProducts: (categoryId) => ({
  pathname: "/commerce/categories/[categoryId]",
  params: { categoryId: String(categoryId) },
}),
commerceProductDetail: (productId) => ({
  pathname: "/commerce/products/[productId]",
  params: { productId: String(productId) },
}),
commerceProductReviews: (productId) => ({
  pathname: "/commerce/products/[productId]/reviews",
  params: { productId: String(productId) },
}),
commerceShopProducts: (shopId) => ({
  pathname: "/commerce/shops/[shopId]",
  params: { shopId: String(shopId) },
}),
commerceShopReviews: (shopId) => ({
  pathname: "/commerce/shops/[shopId]/reviews",
  params: { shopId: String(shopId) },
}),
commerceCart: "/commerce/cart",
commerceAddresses: "/commerce/addresses",
commerceCheckout: "/commerce/checkout",
commerceCheckoutPaymentResult: (paymentId) => ({
  pathname: "/commerce/checkout/payment-result",
  params: { paymentId: String(paymentId) },
}),
commerceCheckoutSuccess: (orderId) => ({
  pathname: "/commerce/checkout/success",
  params: orderId ? { orderId: String(orderId) } : {},
}),
commerceOrders: "/commerce/orders",
commerceOrderDetail: (orderId) => ({
  pathname: "/commerce/orders/[orderId]",
  params: { orderId: String(orderId) },
}),
commerceShipmentTracking: (orderId, shipmentId) => ({
  pathname: "/commerce/orders/[orderId]/shipments/[shipmentId]",
  params: { orderId: String(orderId), shipmentId: String(shipmentId) },
}),
commerceReviewCreate: (productId, orderId) => ({
  pathname: "/commerce/reviews/new",
  params: {
    ...(productId ? { productId: String(productId) } : {}),
    ...(orderId ? { orderId: String(orderId) } : {}),
  },
}),
commerceReviewEdit: (reviewId) => ({
  pathname: "/commerce/reviews/[reviewId]/edit",
  params: { reviewId: String(reviewId) },
}),
```

---

## 4) Page-by-page mapping (buyer)

### CommerceHomePage → `app/(tabs)/shop.jsx`

- **Stitch:** `frontend/stitch/commerce_home/`
- **Components:** `CommerceHomeHero`, `CommerceCategoryNav`, `ProductCard`, `ProductListSortSelect`, `ProductListSkeleton`
- **Hooks:** `useProductList`, `useCommerceCategories`, `useCommerceAddToCart`, `useCommerceBuyNow`

### CommerceSearchPage → `app/commerce/search.jsx`

- **Hooks:** `useProductSearch`
- **Components:** `CommerceSearchResultsHeader`, `ProductCard`

### CommerceCategoryProductsPage → `app/commerce/categories/[categoryId].jsx`

- **Hooks:** `useCategoryProducts`

### CommerceProductDetailPage → `app/commerce/products/[productId].jsx`

- **Components:** `ProductDetailGallery`, `ProductDetailShopCard`, `ProductDetailReviewsPreview`, `StarRating`
- **Hooks:** `useProductDetail`, `useCommerceAddToCart`, `useCommerceBuyNow`
- **Media:** `resolveDevMediaUrl` on all gallery URLs

### CommerceProductReviewsPage → `app/commerce/products/[productId]/reviews.jsx`

- **Hooks:** `useProductReviews`
- **Components:** `ProductReviewCard`, `ProductReviewSummaryAside`

### CommerceShopProductsPage → `app/commerce/shops/[shopId].jsx`

- **Components:** `ShopStorefrontHero`, `ProductCard`
- **Hooks:** `useShopProducts`

### CommerceShopReviewsPage → `app/commerce/shops/[shopId]/reviews.jsx`

- **Hooks:** `useShopReviews`

### CommerceCartPage → `app/commerce/cart.jsx`

- **Hooks:** `useCart`, `useValidateCartItems`
- **Components:** `CartLineItem`, `CartQuantityStepper`, `CartWarningsBanner`

### CommerceUserAddressesPage → `app/commerce/addresses.jsx`

- **Hooks:** `useUserAddresses`, `useGhnAddressOptions`
- **Components:** `UserAddressCard`, `GhnAddressFields`

### CommerceCheckoutPage → `app/commerce/checkout/index.jsx`

- **Hooks:** `useCheckout`
- **Components:** `CheckoutOrderReview`, `CheckoutPaymentMethod`

### CommerceCheckoutPaymentResultPage → `app/commerce/checkout/payment-result.jsx`

- **Hooks:** `usePaymentStatus`, `usePayOsCheckout`
- **Components:** `PaymentStatusPanel`

### CommerceCheckoutSuccessPage → `app/commerce/checkout/success.jsx`

### CommerceOrderListPage → `app/commerce/orders/index.jsx`

- **Hooks:** `useOrderList`

### CommerceOrderDetailPage → `app/commerce/orders/[orderId].jsx`

- **Hooks:** `useOrderDetail`, `useCancelOrder`, `useConfirmOrderReceived`

### CommerceShipmentTrackingPage → `app/commerce/orders/[orderId]/shipments/[shipmentId].jsx`

- **Hooks:** `useShipmentTrackingPage`

### CommerceWriteEditReviewPage → `app/commerce/reviews/new.jsx`, `app/commerce/reviews/[reviewId]/edit.jsx`

- **Hooks:** `useReviewFormPage`, `useUploadReviewMedia`, `useCreateProductReview`, `useUpdateProductReview`

---

## 5) Seller — defer (Phase 9)

| Web page | APP_ROUTES | Mobile |
|----------|------------|--------|
| CommerceCreateShopPage | `commerceCreateShop` | Defer |
| CommerceShopSettingsPage | `commerceShopSettings` | Defer |
| CommerceSellerProductListPage | `commerceSellerProducts` | Defer |
| CommerceSellerProductFormPage | `commerceSellerProductCreate` / `Edit` | Defer |
| CommerceSellerOrderListPage | `commerceSellerOrders` | Defer |
| CommerceSellerOrderDetailPage | `commerceSellerOrderDetail` | Defer |
| CommerceSellerShipmentListPage | `commerceSellerShipments` | Defer |
| CommerceSellerShipmentDetailPage | `commerceSellerShipmentDetail` | Defer |
| CommerceSellerShopReviewsPage | `commerceSellerReviews` | Defer |
| CommerceSellerAnalyticsPage | `commerceSellerAnalytics` | Defer |

---

## 6) Admin — never mobile

| APP_ROUTES | Action |
|------------|--------|
| `commerceAdminShopModeration` | Never |
| `commerceAdminReviewModeration` | Never |
| `commerceAdminProductRemoval` | Never |

---

## 7) Web APP_ROUTES → mobile quick reference

| Web (`APP_ROUTES`) | Mobile |
|--------------------|--------|
| `commerceHome` | `/(tabs)/shop` |
| `commerceSearch` | `/commerce/search` |
| `commerceCategoryProducts` | `/commerce/categories/[categoryId]` |
| `commerceProductDetail` | `/commerce/products/[productId]` |
| `commerceProductReviews` | `/commerce/products/[productId]/reviews` |
| `commerceShopProducts` | `/commerce/shops/[shopId]` |
| `commerceShopReviews` | `/commerce/shops/[shopId]/reviews` |
| `commerceCart` | `/commerce/cart` |
| `commerceAddresses` | `/commerce/addresses` |
| `commerceCheckout` | `/commerce/checkout` |
| `commerceCheckoutPaymentResult` | `/commerce/checkout/payment-result` |
| `commerceCheckoutSuccess` | `/commerce/checkout/success` |
| `commerceOrders` | `/commerce/orders` |
| `commerceOrderDetail` | `/commerce/orders/[orderId]` |
| `commerceShipmentTracking` | `/commerce/orders/[orderId]/shipments/[shipmentId]` |
| `commerceReviewCreate` | `/commerce/reviews/new` |
| `commerceReviewEdit` | `/commerce/reviews/[reviewId]/edit` |

Source: `frontend/src/fe-module/shared/constants/routes.js`

---

## 8) Component / hook maps

**Components (buyer):** `ProductCard`, `ProductListSkeleton`, `CommerceHomeHero`, `ProductDetailGallery`, `CartLineItem`, `GhnAddressFields`, `PaymentStatusPanel`, `OrderListCard`, `ShipmentTrackingTimeline`.

**Hooks (buyer):** `useProductList`, `useProductSearch`, `useCategoryProducts`, `useProductDetail`, `useCart`, `useCheckout`, `usePaymentStatus`, `useOrderList`, `useShipmentTrackingPage`, `useReviewFormPage`.

Do not port seller/admin hooks in buyer v1.

---

## 9) Social product tag entry

`PostProductTagsBlock` → `router.push(ROUTES.commerceProductDetail(productId))`.

---

## 10) Theme & media

- **Colors:** `useThemeColors()` — not deprecated static `colors` import
- **Images:** `resolveDevMediaUrl()` for every product/review URL
- **Env:** `EXPO_PUBLIC_DEV_HOST` on physical device

---

## Related documents

| Document | Role |
|----------|------|
| `mobile-commerce-scope.md` | Scope |
| `mobile-commerce-implementation-order.md` | Phases |
| `mobile-commerce-screen-checklist.md` | DoD |
| `mobile-commerce-rn-adaptations.md` | RN patterns |
