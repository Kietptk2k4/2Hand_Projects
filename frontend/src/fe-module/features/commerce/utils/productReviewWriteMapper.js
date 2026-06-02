function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapReviewContextResponse(data) {
  if (!data) return null;

  return {
    orderItemId: pick(data, "orderItemId", "order_item_id"),
    orderId: pick(data, "orderId", "order_id"),
    productId: pick(data, "productId", "product_id"),
    status: data.status,
    productNameSnapshot: pick(data, "productNameSnapshot", "product_name_snapshot"),
    imageSnapshot: pick(data, "imageSnapshot", "image_snapshot"),
    shopNameSnapshot: pick(data, "shopNameSnapshot", "shop_name_snapshot"),
    finalPrice: pick(data, "finalPrice", "final_price"),
    completedAt: pick(data, "completedAt", "completed_at"),
    hasReview: Boolean(data.has_review ?? data.hasReview),
    reviewId: pick(data, "reviewId", "review_id"),
  };
}

export function mapReviewForEditResponse(data) {
  if (!data) return null;

  return {
    reviewId: pick(data, "reviewId", "review_id"),
    orderItemId: pick(data, "orderItemId", "order_item_id"),
    orderId: pick(data, "orderId", "order_id"),
    productId: pick(data, "productId", "product_id"),
    rating: data.rating,
    comment: data.comment ?? "",
    status: data.status,
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
    productNameSnapshot: pick(data, "productNameSnapshot", "product_name_snapshot"),
    imageSnapshot: pick(data, "imageSnapshot", "image_snapshot"),
    shopNameSnapshot: pick(data, "shopNameSnapshot", "shop_name_snapshot"),
    finalPrice: pick(data, "finalPrice", "final_price"),
    completedAt: pick(data, "completedAt", "completed_at"),
    mediaCount: pick(data, "mediaCount", "media_count") ?? 0,
  };
}

export function mapCreateReviewResponse(data) {
  if (!data) return null;

  return {
    reviewId: pick(data, "reviewId", "review_id"),
    orderItemId: pick(data, "orderItemId", "order_item_id"),
    productId: pick(data, "productId", "product_id"),
    rating: data.rating,
    comment: data.comment,
    status: data.status,
  };
}

export function mapMyProductReviewResponse(data) {
  if (!data) return null;

  const hasReview = Boolean(data.has_review ?? data.hasReview);

  return {
    hasReview,
    reviewId: pick(data, "reviewId", "review_id"),
    productId: pick(data, "productId", "product_id"),
    orderItemId: pick(data, "orderItemId", "order_item_id"),
    rating: data.rating ?? null,
    comment: data.comment ?? "",
    status: data.status ?? null,
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
    canEdit: Boolean(data.can_edit ?? data.canEdit),
  };
}

export function mapUpdateReviewResponse(data) {
  if (!data) return null;

  return {
    reviewId: pick(data, "reviewId", "review_id"),
    orderItemId: pick(data, "orderItemId", "order_item_id"),
    rating: data.rating,
    comment: data.comment,
    ratingChanged: Boolean(data.rating_changed ?? data.ratingChanged),
  };
}

export function mapUploadReviewMediaResponse(data) {
  if (!data) return { media: [] };

  return {
    media: (data.media || []).map((item) => ({
      id: item.id,
      url: item.url,
      type: item.type,
    })),
  };
}
