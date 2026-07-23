function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapReviewModerationDetail(data) {
  if (!data) return null;

  return {
    reviewId: pick(data, "reviewId", "review_id"),
    orderItemId: pick(data, "orderItemId", "order_item_id"),
    productId: pick(data, "productId", "product_id"),
    productTitle: pick(data, "productTitle", "product_title"),
    productThumbnailUrl: pick(data, "productThumbnailUrl", "product_thumbnail_url"),
    buyerId: pick(data, "buyerId", "buyer_id"),
    buyerDisplayName: pick(data, "buyerDisplayName", "buyer_display_name"),
    buyerAvatarUrl: pick(data, "buyerAvatarUrl", "buyer_avatar_url"),
    sellerId: pick(data, "sellerId", "seller_id"),
    shopId: pick(data, "shopId", "shop_id"),
    shopName: pick(data, "shopName", "shop_name"),
    rating: data.rating,
    comment: data.comment,
    status: data.status,
    createdAt: pick(data, "createdAt", "created_at"),
  };
}
