import { APP_ROUTES } from "../../../shared/constants/routes";

export function buildCommerceCategoryPath(categoryId) {
  return APP_ROUTES.commerceCategoryProducts.replace(":categoryId", categoryId);
}

export function buildCommerceShopPath(shopId) {
  return APP_ROUTES.commerceShopProducts.replace(":shopId", shopId);
}

export function buildCommerceProductReviewsPath(productId, { rating } = {}) {
  const base = APP_ROUTES.commerceProductReviews.replace(":productId", productId);
  if (rating == null) return base;
  const params = new URLSearchParams({ rating: String(rating) });
  return `${base}?${params.toString()}`;
}
