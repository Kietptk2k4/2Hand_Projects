import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import {
  getShopBySellerId,
  MOCK_DEMO_SELLER_SHOP_ID,
} from "./commerceSellerShopData";

/** QA: visible, chưa reply — dùng nút Phản hồi */
export const MOCK_SELLER_REVIEW_UNREPLIED = "srv-0000-4000-8000-000000000001";
/** QA: đã có reply */
export const MOCK_SELLER_REVIEW_REPLIED = "srv-0000-4000-8000-000000000002";
/** QA: status HIDDEN — không reply được */
export const MOCK_SELLER_REVIEW_HIDDEN = "srv-0000-4000-8000-000000000003";

const VALID_STATUSES = ["VISIBLE", "HIDDEN"];

const PRODUCT_NAMES = [
  "Máy khoan pin 18V",
  "Bộ tuốc lực điện 20V",
  "Máy mài góc 100mm",
  "Cờ lê điện nháp",
  "Máy cắt đá mini",
];

const COMMENTS = [
  "Sản phẩm tuyệt vời! Giao hàng nhanh và đóng gói cẩn thận.",
  "Pin trâu, dùng công trình cả ngày vẫn ổn.",
  "Hơi nặng tay nhưng công suất tốt.",
  "Shop tư vấn nhiệt tình, sẽ mua lại.",
  null,
  "Giao trễ 1 ngày nhưng sản phẩm OK.",
  "Hộp bị móp méo khi nhận hàng. Rất thất vọng về đóng gói.",
  "Đúng mô tả, giá hợp lý.",
  "Chất lượng ổn trong tầm giá.",
  "Cần thêm phụ kiện trong hộp.",
];

function buildReview(index, overrides = {}) {
  const seed = String(index + 1).padStart(12, "0");
  const rating = overrides.rating ?? ((index % 5) + 1);
  const hasMedia = index % 4 === 0;
  const createdAt =
    overrides.created_at ??
    new Date(2026, 4, 28 - index, 9, 0, 0).toISOString();

  return {
    review_id: overrides.review_id ?? `srv-0000-4000-8000-${seed}`,
    seller_id: MOCK_CART_DEMO_USER_ID,
    shop_id: MOCK_DEMO_SELLER_SHOP_ID,
    order_item_id:
      overrides.order_item_id ??
      `so2000000-0000-4000-8000-${String(index + 1).padStart(12, "0")}`,
    product_name_snapshot:
      overrides.product_name_snapshot ?? PRODUCT_NAMES[index % PRODUCT_NAMES.length],
    rating,
    comment: overrides.comment ?? COMMENTS[index % COMMENTS.length],
    status: overrides.status ?? "VISIBLE",
    created_at: createdAt,
    media:
      overrides.media ??
      (hasMedia
        ? [
            {
              media_id: `srm-${seed}-0`,
              url: `https://picsum.photos/seed/seller-review-${index}/320/240`,
              media_type: "IMAGE",
            },
          ]
        : []),
    seller_reply: overrides.seller_reply ?? null,
  };
}

function seedReviews() {
  const list = [];

  list.push(
    buildReview(0, {
      review_id: MOCK_SELLER_REVIEW_UNREPLIED,
      rating: 5,
      seller_reply: null,
      comment: "Sản phẩm tuyệt vời! Giao hàng nhanh và đóng gói rất cẩn thận.",
    }),
  );

  list.push(
    buildReview(1, {
      review_id: MOCK_SELLER_REVIEW_REPLIED,
      rating: 2,
      seller_reply: {
        reply_id: "srr-0000-4000-8000-000000000002",
        content:
          "Chào bạn, shop rất xin lỗi về sự cố đóng gói. Shop đã liên hệ để hỗ trợ đổi trả miễn phí.",
        created_at: "2026-05-22T14:00:00Z",
      },
      comment: "Hộp bị móp méo khi nhận hàng. Tai nghe dùng tạm được.",
    }),
  );

  list.push(
    buildReview(2, {
      review_id: MOCK_SELLER_REVIEW_HIDDEN,
      rating: 1,
      status: "HIDDEN",
      seller_reply: null,
      comment: "Nội dung bị ẩn do vi phạm chính sách (mock).",
    }),
  );

  for (let i = 3; i < 24; i += 1) {
    const hasReply = i % 5 === 0;
    list.push(
      buildReview(i, {
        seller_reply: hasReply
          ? {
              reply_id: `srr-0000-4000-8000-${String(i + 1).padStart(12, "0")}`,
              content: "Cảm ơn bạn đã mua và đánh giá. Shop luôn hỗ trợ bảo hành đúng cam kết.",
              created_at: new Date(2026, 4, 29 - i, 15, 0, 0).toISOString(),
            }
          : null,
      }),
    );
  }

  return list;
}

const reviewsById = new Map(seedReviews().map((r) => [r.review_id, r]));

function computeRatingSummary(reviews) {
  if (!reviews.length) {
    return { rating_avg: 0, rating_count: 0 };
  }
  const sum = reviews.reduce((acc, r) => acc + r.rating, 0);
  return {
    rating_avg: Math.round((sum / reviews.length) * 10) / 10,
    rating_count: reviews.length,
  };
}

export function validateSellerShopReviewsQuery({ page, limit, rating, status }) {
  const pageNum = Number(page);
  const limitNum = Number(limit);

  if (!Number.isInteger(pageNum) || pageNum < 1) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (!Number.isInteger(limitNum) || limitNum < 1 || limitNum > 50) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  const statusValue = status || "VISIBLE";
  if (!VALID_STATUSES.includes(statusValue)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (rating != null && rating !== "") {
    const ratingNum = Number(rating);
    if (!Number.isInteger(ratingNum) || ratingNum < 1 || ratingNum > 5) {
      return { error: "COMMERCE-400-RATING", status: 400 };
    }
    return { page: pageNum, limit: limitNum, rating: ratingNum, status: statusValue };
  }

  return { page: pageNum, limit: limitNum, rating: null, status: statusValue };
}

export function listSellerShopReviewsForUser(userId, { page, limit, rating, status }) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409 };
  }

  const forStatus = [...reviewsById.values()].filter(
    (r) => r.seller_id === userId && r.status === status,
  );

  const ratingSummary = computeRatingSummary(forStatus);

  let filtered = forStatus;
  if (rating != null) {
    filtered = filtered.filter((r) => r.rating === rating);
  }

  filtered.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

  const total = filtered.length;
  const totalPages = Math.max(1, Math.ceil(total / limit) || 1);
  const start = (page - 1) * limit;
  const pageItems = filtered.slice(start, start + limit).map(toListItem);

  return {
    data: {
      shop_id: shop.shop_id,
      rating_summary: ratingSummary,
      reviews: pageItems,
      pagination: {
        page,
        limit,
        total_items: total,
        total_pages: totalPages,
        has_next: page < totalPages,
      },
    },
  };
}

function toListItem(record) {
  return {
    review_id: record.review_id,
    order_item_id: record.order_item_id,
    product_name_snapshot: record.product_name_snapshot,
    rating: record.rating,
    comment: record.comment,
    status: record.status,
    created_at: record.created_at,
    media: record.media,
    seller_reply: record.seller_reply,
  };
}

export function replyToSellerReview(userId, reviewId, body) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409 };
  }

  const content = String(body?.content ?? "").trim();
  if (!content) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const record = reviewsById.get(reviewId);
  if (!record || record.seller_id !== userId) {
    return { error: "COMMERCE-404-REVIEW", status: 404 };
  }

  if (record.status !== "VISIBLE") {
    return { error: "COMMERCE-409-REVIEW-VISIBLE", status: 409 };
  }

  if (record.seller_reply?.reply_id) {
    return { error: "COMMERCE-409-REVIEW-REPLY", status: 409 };
  }

  const now = new Date().toISOString();
  const reply = {
    reply_id: `srr-${Date.now().toString(16)}`,
    content,
    created_at: now,
  };

  record.seller_reply = reply;
  reviewsById.set(reviewId, record);

  return {
    data: {
      reply_id: reply.reply_id,
      review_id: reviewId,
      seller_id: userId,
      content,
      created_at: now,
    },
  };
}
