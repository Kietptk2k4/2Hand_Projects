function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapAdminReviewListItem(item) {
  if (!item) return null;

  return {
    reviewId: pick(item, "reviewId", "review_id"),
    orderItemId: pick(item, "orderItemId", "order_item_id"),
    productId: pick(item, "productId", "product_id"),
    productTitle: pick(item, "productTitle", "product_title"),
    productThumbnailUrl: pick(item, "productThumbnailUrl", "product_thumbnail_url"),
    buyerId: pick(item, "buyerId", "buyer_id"),
    buyerDisplayName: pick(item, "buyerDisplayName", "buyer_display_name"),
    buyerAvatarUrl: pick(item, "buyerAvatarUrl", "buyer_avatar_url"),
    sellerId: pick(item, "sellerId", "seller_id"),
    rating: item.rating,
    comment: item.comment,
    status: item.status,
    createdAt: pick(item, "createdAt", "created_at"),
  };
}

export function mapAdminReviewListResponse(data) {
  if (!data) {
    return { items: [], pagination: null };
  }

  return {
    items: (data.items || []).map(mapAdminReviewListItem).filter(Boolean),
    pagination: data.pagination
      ? {
          page: data.pagination.page,
          limit: data.pagination.limit,
          totalItems: data.pagination.total_items ?? data.pagination.totalItems,
          totalPages: data.pagination.total_pages ?? data.pagination.totalPages,
          hasNext: Boolean(data.pagination.has_next ?? data.pagination.hasNext),
        }
      : null,
  };
}

export function mapModerateReviewPayload({ action, reason }) {
  return {
    action,
    reason: String(reason ?? "").trim(),
  };
}

export function mapModerateReviewResponse(data) {
  if (!data) return null;

  return {
    reviewId: pick(data, "reviewId", "review_id"),
    orderItemId: pick(data, "orderItemId", "order_item_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    buyerId: pick(data, "buyerId", "buyer_id"),
    rating: data.rating,
    status: data.status,
    previousStatus: pick(data, "previousStatus", "previous_status"),
    alreadyModerated: Boolean(data.already_moderated ?? data.alreadyModerated),
    sellerRatingAvg: data.seller_rating_avg ?? data.sellerRatingAvg,
    sellerRatingCount: data.seller_rating_count ?? data.sellerRatingCount,
    moderatedAt: pick(data, "moderatedAt", "moderated_at"),
  };
}
