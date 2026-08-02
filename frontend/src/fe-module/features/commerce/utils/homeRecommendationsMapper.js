import { resolveDevMediaUrl } from "../../../shared/utils/getClientUploadOrigin";

export function mapHomeRecommendationItem(item) {
  if (!item) return null;
  const id = item.id ?? item.product_id ?? item.productId;
  if (!id) return null;
  const shop = item.shop || {};
  const rating = item.rating || {};
  return {
    productId: id,
    title: item.title,
    thumbnailUrl: resolveDevMediaUrl(item.thumbnail ?? item.thumbnail_url ?? item.thumbnailUrl),
    shopId: shop.id ?? item.shop_id ?? item.shopId,
    shopName: shop.name ?? item.shop_name ?? item.shopName,
    price: item.price,
    salePrice: null,
    effectivePrice: item.price,
    inStock: true,
    lowStock: false,
    ratingAvg: rating.avg ?? rating.rating_avg ?? item.rating_avg ?? null,
    ratingCount: rating.count ?? rating.rating_count ?? item.rating_count ?? 0,
    shopVacation: false,
    vacationMessage: null,
  };
}

export function mapHomeRecommendationsResponse(data) {
  const items = (data?.items || []).map(mapHomeRecommendationItem).filter(Boolean);
  return {
    requestId: data?.request_id ?? data?.requestId ?? null,
    rankingMode: data?.ranking_mode ?? data?.rankingMode ?? null,
    modelName: data?.model_name ?? data?.modelName ?? null,
    modelVersion: data?.model_version ?? data?.modelVersion ?? null,
    items,
  };
}
