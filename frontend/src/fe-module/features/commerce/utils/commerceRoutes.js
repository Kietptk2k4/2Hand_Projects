import { APP_ROUTES } from "../../../shared/constants/routes";

export function buildCommerceCategoryPath(categoryId) {
  return APP_ROUTES.commerceCategoryProducts.replace(":categoryId", categoryId);
}

export function buildCommerceShopPath(shopId) {
  return APP_ROUTES.commerceShopProducts.replace(":shopId", shopId);
}
