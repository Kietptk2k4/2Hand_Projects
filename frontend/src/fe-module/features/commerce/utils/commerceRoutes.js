import { APP_ROUTES } from "../../../shared/constants/routes";

const SELLER_PATH_PREFIX = "/commerce/seller";
const CREATE_SHOP_PATH = APP_ROUTES.commerceCreateShop;

export function isCommerceSellerAreaPath(pathname) {
  return pathname.startsWith(SELLER_PATH_PREFIX);
}

/** Seller management UI — excludes onboarding create-shop flow. */
export function isCommerceSellerShellPath(pathname) {
  return isCommerceSellerAreaPath(pathname) && pathname !== CREATE_SHOP_PATH;
}

export function buildCommerceCategoryPath(categoryId) {
  return APP_ROUTES.commerceCategoryProducts.replace(":categoryId", categoryId);
}

export function buildCommerceShopPath(shopId) {
  return APP_ROUTES.commerceShopProducts.replace(":shopId", shopId);
}

export function buildCommerceSellerOrderDetailPath(orderId) {
  return APP_ROUTES.commerceSellerOrderDetail.replace(":orderId", orderId);
}

export function buildCommerceSellerProductEditPath(productId, { step } = {}) {
  const base = APP_ROUTES.commerceSellerProductEdit.replace(":productId", productId);
  if (step == null) return base;
  const params = new URLSearchParams({ step: String(step) });
  return `${base}?${params.toString()}`;
}

export function buildCommerceShopReviewsPath(shopId, { rating } = {}) {
  const base = APP_ROUTES.commerceShopReviews.replace(":shopId", shopId);
  if (rating == null) return base;
  const params = new URLSearchParams({ rating: String(rating) });
  return `${base}?${params.toString()}`;
}

export function buildCommerceProductReviewsPath(productId, { rating } = {}) {
  const base = APP_ROUTES.commerceProductReviews.replace(":productId", productId);
  if (rating == null) return base;
  const params = new URLSearchParams({ rating: String(rating) });
  return `${base}?${params.toString()}`;
}
