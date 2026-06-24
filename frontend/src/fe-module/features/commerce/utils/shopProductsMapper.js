import { mapProductItem } from "./productListMapper";
import { resolveDevMediaUrl } from "../../../shared/utils/getClientUploadOrigin";

function mapShopSummary(shop) {
  if (!shop) return null;
  return {
    shopId: shop.shop_id,
    shopName: shop.shop_name,
    description: shop.description,
    avatarUrl: resolveDevMediaUrl(shop.avatar_url),
    coverUrl: resolveDevMediaUrl(shop.cover_url),
    ratingAvg: shop.rating_avg,
    ratingCount: shop.rating_count,
    shopVacation: shop.shop_vacation,
    vacationMessage: shop.vacation_message,
    sellerId: shop.seller_id ?? null,
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
