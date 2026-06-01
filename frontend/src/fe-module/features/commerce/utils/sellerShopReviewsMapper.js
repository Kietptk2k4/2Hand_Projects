function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

function mapMedia(media) {
  return (media || []).map((item) => ({
    mediaId: pick(item, "mediaId", "media_id"),
    url: item.url,
    mediaType: pick(item, "mediaType", "media_type"),
  }));
}

function mapSellerReply(reply) {
  if (!reply) return null;
  return {
    replyId: pick(reply, "replyId", "reply_id"),
    content: reply.content,
    createdAt: pick(reply, "createdAt", "created_at"),
  };
}

export function mapSellerShopReviewItem(review) {
  if (!review) return null;

  return {
    reviewId: pick(review, "reviewId", "review_id"),
    orderItemId: pick(review, "orderItemId", "order_item_id"),
    productNameSnapshot: pick(review, "productNameSnapshot", "product_name_snapshot"),
    rating: review.rating,
    comment: review.comment,
    status: review.status,
    createdAt: pick(review, "createdAt", "created_at"),
    media: mapMedia(review.media),
    sellerReply: mapSellerReply(review.seller_reply ?? review.sellerReply),
  };
}

export function mapSellerShopReviewsResponse(data) {
  if (!data) {
    return {
      shopId: null,
      ratingSummary: { ratingAvg: 0, ratingCount: 0 },
      reviews: [],
      pagination: null,
    };
  }

  const summary = data.rating_summary || {};

  return {
    shopId: pick(data, "shopId", "shop_id"),
    ratingSummary: {
      ratingAvg: summary.rating_avg ?? 0,
      ratingCount: summary.rating_count ?? 0,
    },
    reviews: (data.reviews || []).map(mapSellerShopReviewItem).filter(Boolean),
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

export function mapReplyToReviewResponse(data) {
  if (!data) return null;
  return {
    replyId: pick(data, "replyId", "reply_id"),
    reviewId: pick(data, "reviewId", "review_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    content: data.content,
    createdAt: pick(data, "createdAt", "created_at"),
  };
}
