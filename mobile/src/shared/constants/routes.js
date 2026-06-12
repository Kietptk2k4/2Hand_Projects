export const ROUTES = {
  login: "/(auth)/login",
  feed: "/(tabs)/feed",
  profile: "/(tabs)/profile",
  shop: "/(tabs)/shop",
  postDetail: (postId, { focusComments = false } = {}) => ({
    pathname: "/post/[postId]",
    params: {
      postId,
      ...(focusComments ? { focusComments: "1" } : {}),
    },
  }),
  postCreate: ({ pickMedia = false } = {}) => ({
    pathname: "/post/create",
    ...(pickMedia ? { params: { pickMedia: "1" } } : {}),
  }),
  postEdit: (postId) => ({
    pathname: "/post/[postId]/edit",
    params: { postId },
  }),
  userProfile: (userId) => `/profile/${userId}`,
  profileFollowers: (userId) => `/profile/${userId}/followers`,
  profileFollowing: (userId) => `/profile/${userId}/following`,
  saved: "/saved",
  search: "/search",
  suggestions: "/suggestions",
  hashtag: (tag) => `/hashtag/${encodeURIComponent(String(tag).replace(/^#+/, ""))}`,
  account: "/account",
  accountInfo: "/account/info",
  accountEdit: "/account/edit",
  accountAvatar: "/account/avatar",
  accountPrivacy: "/account/privacy",
  accountSettings: "/account/settings",
  accountDelete: "/account/delete",
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
  commerceAddressCreate: "/commerce/addresses/create",
  commerceAddressEdit: (addressId) => ({
    pathname: "/commerce/addresses/[addressId]",
    params: { addressId: String(addressId) },
  }),
  commerceCheckout: "/commerce/checkout",
  commerceCheckoutPaymentResult: (paymentId) => ({
    pathname: "/commerce/checkout/payment-result",
    params: { paymentId: String(paymentId) },
  }),
  commerceCheckoutSuccess: (orderId) => ({
    pathname: "/commerce/checkout/success",
    ...(orderId ? { params: { orderId: String(orderId) } } : {}),
  }),
  commerceOrders: "/commerce/orders",
  commerceOrderDetail: (orderId) => ({
    pathname: "/commerce/orders/[orderId]",
    params: { orderId: String(orderId) },
  }),
  commerceShipmentTracking: (orderId, shipmentId) => ({
    pathname: "/commerce/orders/[orderId]/shipments/[shipmentId]",
    params: {
      orderId: String(orderId),
      shipmentId: String(shipmentId),
    },
  }),
  commerceReviewCreate: (productId, orderId, orderItemId) => ({
    pathname: "/commerce/reviews/new",
    params: {
      ...(productId ? { productId: String(productId) } : {}),
      ...(orderId ? { orderId: String(orderId) } : {}),
      ...(orderItemId ? { orderItemId: String(orderItemId) } : {}),
    },
  }),
  commerceReviewEdit: (reviewId) => ({
    pathname: "/commerce/reviews/[reviewId]/edit",
    params: { reviewId: String(reviewId) },
  }),
};
