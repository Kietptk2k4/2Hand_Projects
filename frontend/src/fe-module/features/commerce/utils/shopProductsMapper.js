import { mapProductItem } from "./productListMapper";

function mapShopSummary(shop) {
  if (!shop) return null;
  return {
    shopId: shop.shop_id,
    shopName: shop.shop_name,
    description: shop.description,
    avatarUrl: shop.avatar_url,
    coverUrl: shop.cover_url,
    ratingAvg: shop.rating_avg,
    ratingCount: shop.rating_count,
    shopVacation: shop.shop_vacation,
    vacationMessage: shop.vacation_message,
  };
}

export function mapShopProductsResponse(data) {
  const items = (data?.items || []).map(mapProductItem).filter(Boolean);
  const pagination = data?.pagination || {};

  return {
    shop: mapShopSummary(data?.shop),
    items,
    pagination: {
      page: pagination.page,
      limit: pagination.limit,
      totalItems: pagination.total_items,
      totalPages: pagination.total_pages,
      hasNext: pagination.has_next,
    },
  };
}
