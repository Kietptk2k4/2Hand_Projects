import { mapReviewBuyerFields } from "./reviewParticipantMapper";

export function mapShopReviewsResponse(data) {
  if (!data) return null;

  const summary = data.rating_summary || {};

  return {
    shopId: data.shop_id,
    shopName: data.shop_name,
    shopAvatarUrl: data.shop_avatar_url ?? null,
    sellerId: data.seller_id ?? null,
    ratingSummary: {
      ratingAvg: summary.rating_avg ?? 0,
      ratingCount: summary.rating_count ?? 0,
    },
    reviews: (data.reviews || []).map((review) => ({
      reviewId: review.review_id,
      ...mapReviewBuyerFields(review),
      productName: review.product_name_snapshot,
      rating: review.rating,
      comment: review.comment,
      createdAt: review.created_at,
      media: (review.media || []).map((item) => ({
        mediaId: item.media_id,
        url: item.url,
        mediaType: item.media_type,
      })),
      sellerReply: review.seller_reply
        ? {
            replyId: review.seller_reply.reply_id,
            content: review.seller_reply.content,
            createdAt: review.seller_reply.created_at,
          }
        : null,
    })),
    pagination: {
      page: data.pagination?.page ?? 1,
      limit: data.pagination?.limit ?? 20,
      totalItems: data.pagination?.total_items ?? 0,
      totalPages: data.pagination?.total_pages ?? 0,
      hasNext: Boolean(data.pagination?.has_next),
    },
  };
}
