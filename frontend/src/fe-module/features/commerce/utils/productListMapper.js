import { resolveDevMediaUrl } from "../../../shared/utils/getClientUploadOrigin";

export function mapProductItem(item) {
  if (!item) return null;
  return {
    productId: item.product_id,
    title: item.title,
    thumbnailUrl: resolveDevMediaUrl(item.thumbnail_url),
    shopId: item.shop_id,
    shopName: item.shop_name,
    categoryId: item.category_id,
    condition: item.condition,
    status: item.status,
    price: item.price,
    salePrice: item.sale_price,
    effectivePrice: item.effective_price,
    promotionStartAt: item.promotion_start_at ?? item.promotionStartAt ?? null,
    promotionEndAt: item.promotion_end_at ?? item.promotionEndAt ?? null,
    inStock: item.in_stock,
    lowStock: item.low_stock,
    ratingAvg: item.rating_avg,
    endAt:
      item.promotion_end_at ??
      item.promotionEndAt ??
      item.end_at ??
      item.endAt ??
      item.sale_end_at ??
      null,
    shopVacation: item.shop_vacation,
    vacationMessage: item.vacation_message,
  };
}

export function mapProductListResponse(data) {
  const items = (data?.items || []).map(mapProductItem).filter(Boolean);
  const pagination = data?.pagination || {};
  return {
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
