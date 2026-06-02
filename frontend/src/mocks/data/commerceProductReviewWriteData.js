import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { buildDetailFromSummary } from "./commerceOrderDetailData";
import { findOrderSummaryForUser } from "./commerceOrderListData";
import { buildProductDetail, isValidProductId } from "./commerceProductDetailData";
import {
  appendReviewForProduct,
  computeRatingSummary,
  getAllReviewsForProduct,
  updateReviewInProductList,
} from "./commerceProductReviewsData";

/** QA: edit existing review */
export const MOCK_REVIEW_EDIT_ID = "rv-buyer-0000-4000-8000-000000000005";
/** QA: create — order o2000000-0000-4000-8000-000000000006 */
export const MOCK_ORDER_ITEM_CREATE_ID = "oi-000006-1";
/** QA: edit — order o2000000-0000-4000-8000-000000000005 */
export const MOCK_ORDER_ITEM_EDIT_ID = "oi-000005-1";
/** QA: product after create (order 006 — first line item uses catalog index 0) */
export const MOCK_PRODUCT_ID_AFTER_CREATE = "c1000000-0000-4000-8000-000000000001";
/** QA: product for edit review (order 005 — same catalog rule) */
export const MOCK_PRODUCT_ID_FOR_EDIT = "c1000000-0000-4000-8000-000000000001";

import {
  getReviewIdForOrderItem,
  hasReviewForOrderItem,
  setReviewIdForOrderItem,
} from "./commerceProductReviewIndex";

const reviewsById = new Map();
const mediaByReviewId = new Map();

const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];
const ALLOWED_VIDEO_TYPES = ["video/mp4", "video/webm"];
const MAX_REVIEW_MEDIA = 10;
const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
const MAX_VIDEO_BYTES = 50 * 1024 * 1024;

function toPublicListReview(review) {
  return {
    review_id: review.review_id,
    rating: review.rating,
    comment: review.comment,
    created_at: review.created_at,
    media: [],
    seller_reply: null,
  };
}

function seedBuyerReviews() {
  const orderItemId = MOCK_ORDER_ITEM_EDIT_ID;
  const reviewId = MOCK_REVIEW_EDIT_ID;
  const found = findOrderItemForUser(MOCK_CART_DEMO_USER_ID, orderItemId);
  if (!found) return;

  const productId = found.item.product_id;
  const createdAt = "2026-05-10T08:00:00Z";

  const review = {
    review_id: reviewId,
    order_item_id: orderItemId,
    product_id: productId,
    order_id: found.summary.order_id,
    seller_id: found.item.seller_id,
    buyer_id: MOCK_CART_DEMO_USER_ID,
    rating: 5,
    comment: "Sản phẩm đúng mô tả, giao nhanh. Rất hài lòng.",
    status: "VISIBLE",
    created_at: createdAt,
    updated_at: createdAt,
  };

  reviewsById.set(reviewId, review);
  setReviewIdForOrderItem(orderItemId, reviewId);
  appendReviewForProduct(productId, toPublicListReview(review));
}

function findOrderItemForUser(userId, orderItemId) {
  const suffix = orderItemId.replace(/^oi-(\d{6})-\d+$/, "$1");
  if (!suffix || suffix === orderItemId) return null;

  const orderId = `o2000000-0000-4000-8000-000000${suffix}`;
  const summary = findOrderSummaryForUser(userId, orderId);
  if (!summary) return null;

  const detail = buildDetailFromSummary(summary);
  const item = detail.items.find((row) => row.order_item_id === orderItemId);
  if (!item) return null;

  return { summary, item };
}

export { getReviewIdForOrderItem, hasReviewForOrderItem } from "./commerceProductReviewIndex";

export function getReviewContextForOrderItem(userId, orderItemId) {
  const found = findOrderItemForUser(userId, orderItemId);
  if (!found) {
    return { error: "COMMERCE-404-ORDER-ITEM", status: 404 };
  }

  const { summary, item } = found;

  return {
    data: {
      order_item_id: item.order_item_id,
      order_id: summary.order_id,
      product_id: item.product_id,
      status: item.status,
      product_name_snapshot: item.product_name_snapshot,
      image_snapshot: item.image_snapshot,
      shop_name_snapshot: item.shop_name_snapshot,
      final_price: item.final_price,
      completed_at: item.completed_at,
      has_review: hasReviewForOrderItem(orderItemId),
      review_id: getReviewIdForOrderItem(orderItemId),
    },
  };
}

function findBuyerReviewForProduct(userId, productId) {
  for (const review of reviewsById.values()) {
    if (
      review.buyer_id === userId &&
      review.product_id === productId &&
      review.status === "VISIBLE"
    ) {
      return review;
    }
  }
  return null;
}

export function getMyReviewForProduct(userId, productId) {
  if (!isValidProductId(productId) || !buildProductDetail(productId)) {
    return { error: "COMMERCE-404-PRODUCT", status: 404 };
  }

  const review = findBuyerReviewForProduct(userId, productId);

  if (!review) {
    return {
      data: {
        has_review: false,
        product_id: productId,
        review_id: null,
        can_edit: false,
      },
    };
  }

  return {
    data: {
      has_review: true,
      review_id: review.review_id,
      product_id: review.product_id,
      order_item_id: review.order_item_id,
      rating: review.rating,
      comment: review.comment,
      status: review.status,
      created_at: review.created_at,
      updated_at: review.updated_at,
      can_edit: review.status === "VISIBLE",
    },
  };
}

export function getMediaCountForReview(reviewId) {
  return (mediaByReviewId.get(reviewId) || []).length;
}

function resolveMediaType(mimeType) {
  if (ALLOWED_IMAGE_TYPES.includes(mimeType)) return "IMAGE";
  if (ALLOWED_VIDEO_TYPES.includes(mimeType)) return "VIDEO";
  return null;
}

function validateMockFile(file) {
  const type = file?.type;
  const size = file?.size ?? 0;
  const mediaType = resolveMediaType(type);

  if (!mediaType) {
    return { error: "COMMERCE-400-MEDIA-TYPE", status: 400 };
  }
  if (size <= 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }
  if (mediaType === "IMAGE" && size > MAX_IMAGE_BYTES) {
    return { error: "COMMERCE-400-MEDIA-SIZE", status: 400 };
  }
  if (mediaType === "VIDEO" && size > MAX_VIDEO_BYTES) {
    return { error: "COMMERCE-400-MEDIA-SIZE", status: 400 };
  }

  return { mediaType };
}

export function uploadReviewMediaForBuyer(userId, reviewId, files) {
  const review = reviewsById.get(reviewId);
  if (!review || review.buyer_id !== userId) {
    return { error: "COMMERCE-404-REVIEW", status: 404 };
  }

  if (review.status !== "VISIBLE") {
    return { error: "COMMERCE-409-REVIEW-VISIBLE", status: 409 };
  }

  const list = files || [];
  if (!list.length) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const existing = mediaByReviewId.get(reviewId) || [];
  if (existing.length + list.length > MAX_REVIEW_MEDIA) {
    return { error: "COMMERCE-409-REVIEW-MEDIA", status: 409 };
  }

  const uploaded = [];

  for (const file of list) {
    const check = validateMockFile(file);
    if (check.error) {
      return check;
    }

    const mediaId = `rm-${crypto.randomUUID().replace(/-/g, "").slice(0, 12)}`;
    const seed = mediaId.slice(-6);
    const isVideo = check.mediaType === "VIDEO";

    uploaded.push({
      id: mediaId,
      url: isVideo
        ? `https://picsum.photos/seed/review-vid-${seed}/320/240`
        : `https://picsum.photos/seed/review-img-${seed}/320/240`,
      type: check.mediaType,
    });
  }

  mediaByReviewId.set(reviewId, [...existing, ...uploaded]);

  return { data: { media: uploaded } };
}

export function getReviewForBuyer(userId, reviewId) {
  const review = reviewsById.get(reviewId);
  if (!review || review.buyer_id !== userId) {
    return { error: "COMMERCE-404-REVIEW", status: 404 };
  }

  const found = findOrderItemForUser(userId, review.order_item_id);
  if (!found) {
    return { error: "COMMERCE-404-REVIEW", status: 404 };
  }

  const { item } = found;

  return {
    data: {
      review_id: review.review_id,
      order_item_id: review.order_item_id,
      order_id: review.order_id,
      product_id: review.product_id,
      rating: review.rating,
      comment: review.comment ?? "",
      status: review.status,
      created_at: review.created_at,
      updated_at: review.updated_at,
      media_count: getMediaCountForReview(reviewId),
      product_name_snapshot: item.product_name_snapshot,
      image_snapshot: item.image_snapshot,
      shop_name_snapshot: item.shop_name_snapshot,
      final_price: item.final_price,
      completed_at: item.completed_at,
    },
  };
}

function computeSellerRating(productId) {
  const summary = computeRatingSummary(getAllReviewsForProduct(productId));
  return {
    seller_rating_avg: summary.rating_avg,
    seller_rating_count: summary.rating_count,
  };
}

export function createReviewForBuyer(userId, { order_item_id, rating, comment }) {
  if (!order_item_id) {
    return { error: "COMMERCE-400-VALIDATION", status: 400, message: "Thieu order_item_id." };
  }

  if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
    return { error: "COMMERCE-400-RATING", status: 400, message: "Rating phai tu 1 den 5." };
  }

  if (hasReviewForOrderItem(order_item_id)) {
    return {
      error: "COMMERCE-409-REVIEW-EXISTS",
      status: 409,
      message: "Da co danh gia cho san pham nay.",
    };
  }

  const found = findOrderItemForUser(userId, order_item_id);
  if (!found) {
    return { error: "COMMERCE-404-ORDER-ITEM", status: 404, message: "Khong tim thay dong hang." };
  }

  const { summary, item } = found;

  if (item.status !== "COMPLETED") {
    return {
      error: "COMMERCE-409-ORDER-ITEM-REVIEW",
      status: 409,
      message: "Chi danh gia khi don hang da hoan thanh.",
    };
  }

  const reviewId = `rv-buyer-${Date.now().toString(36).slice(-8)}`;
  const now = new Date().toISOString();

  const review = {
    review_id: reviewId,
    order_item_id: order_item_id,
    product_id: item.product_id,
    order_id: summary.order_id,
    seller_id: item.seller_id,
    buyer_id: userId,
    rating,
    comment: comment?.trim() || null,
    status: "VISIBLE",
    created_at: now,
    updated_at: now,
  };

  reviewsById.set(reviewId, review);
  setReviewIdForOrderItem(order_item_id, reviewId);
  appendReviewForProduct(item.product_id, toPublicListReview(review));

  const sellerStats = computeSellerRating(item.product_id);

  return {
    data: {
      review_id: reviewId,
      order_item_id: order_item_id,
      seller_id: item.seller_id,
      buyer_id: userId,
      rating,
      comment: review.comment,
      status: "VISIBLE",
      created_at: now,
      ...sellerStats,
    },
  };
}

export function updateReviewForBuyer(userId, reviewId, patch) {
  const review = reviewsById.get(reviewId);
  if (!review || review.buyer_id !== userId) {
    return { error: "COMMERCE-404-REVIEW", status: 404, message: "Khong tim thay danh gia." };
  }

  if (review.status === "HIDDEN") {
    return {
      error: "COMMERCE-409-REVIEW-VISIBLE",
      status: 409,
      message: "Khong the sua danh gia da bi an.",
    };
  }

  const hasRating = patch.rating !== undefined;
  const hasComment = patch.comment !== undefined;

  if (!hasRating && !hasComment) {
    return {
      error: "COMMERCE-400-VALIDATION",
      status: 400,
      message: "Phai gui rating hoac comment.",
    };
  }

  if (hasRating && (!Number.isInteger(patch.rating) || patch.rating < 1 || patch.rating > 5)) {
    return { error: "COMMERCE-400-RATING", status: 400, message: "Rating phai tu 1 den 5." };
  }

  const ratingChanged = hasRating && patch.rating !== review.rating;
  const nextRating = hasRating ? patch.rating : review.rating;
  const nextComment = hasComment
    ? patch.comment === ""
      ? null
      : String(patch.comment).trim() || null
    : review.comment;

  const now = new Date().toISOString();
  const updated = {
    ...review,
    rating: nextRating,
    comment: nextComment,
    updated_at: now,
  };

  reviewsById.set(reviewId, updated);

  updateReviewInProductList(review.product_id, reviewId, {
    rating: nextRating,
    comment: nextComment,
    created_at: review.created_at,
  });

  const sellerStats = ratingChanged
    ? computeSellerRating(review.product_id)
    : computeSellerRating(review.product_id);

  return {
    data: {
      review_id: reviewId,
      order_item_id: review.order_item_id,
      seller_id: review.seller_id,
      buyer_id: userId,
      rating: nextRating,
      comment: nextComment,
      status: "VISIBLE",
      rating_changed: ratingChanged,
      created_at: review.created_at,
      updated_at: now,
      ...sellerStats,
    },
  };
}

seedBuyerReviews();
