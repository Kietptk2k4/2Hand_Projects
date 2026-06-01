import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { MOCK_DEMO_SELLER_SHOP_ID } from "./commerceSellerShopData";

/** QA: VISIBLE — thử HIDE */
export const MOCK_ADMIN_REVIEW_VISIBLE = "arv-0000-4000-8000-000000000001";
/** QA: HIDDEN — thử RESTORE */
export const MOCK_ADMIN_REVIEW_HIDDEN = "arv-0000-4000-8000-000000000002";

const VALID_ACTIONS = ["HIDE", "RESTORE"];
const VALID_STATUSES = ["VISIBLE", "HIDDEN"];

const BUYERS = [
  {
    buyer_id: "b1000000-0000-4000-8000-000000000001",
    buyer_display_name: "Minh Anh",
    buyer_avatar_url: "https://i.pravatar.cc/80?img=5",
  },
  {
    buyer_id: "b1000000-0000-4000-8000-000000000002",
    buyer_display_name: "Hoàng Tuấn",
    buyer_avatar_url: "https://i.pravatar.cc/80?img=12",
  },
  {
    buyer_id: "b1000000-0000-4000-8000-000000000003",
    buyer_display_name: "Lan Phương",
    buyer_avatar_url: null,
  },
];

const PRODUCTS = [
  {
    product_id: "c2000000-0000-4000-8000-000000000101",
    product_title: "Máy khoan pin 18V",
    product_thumbnail_url: "https://picsum.photos/seed/admin-rev-prod-101/64/64",
  },
  {
    product_id: "c2000000-0000-4000-8000-000000000102",
    product_title: "Bộ tuốc lực điện 20V",
    product_thumbnail_url: "https://picsum.photos/seed/admin-rev-prod-102/64/64",
  },
  {
    product_id: "c2000000-0000-4000-8000-000000000108",
    product_title: "Cờ lê điện nháp",
    product_thumbnail_url: "https://picsum.photos/seed/admin-rev-prod-108/64/64",
  },
];

const COMMENTS = [
  "Sản phẩm tuyệt vời! Giao hàng nhanh và đóng gói cẩn thận.",
  "Pin trâu, dùng công trình cả ngày vẫn ổn.",
  "Hơi nặng tay nhưng công suất tốt.",
  null,
  "Giao trễ 1 ngày nhưng sản phẩm OK.",
  "Nội dung vi phạm chính sách — spam quảng cáo.",
  "Đúng mô tả, giá hợp lý.",
  "Chất lượng kém, không đúng hình.",
];

function buildReview(index, overrides = {}) {
  const seed = String(index + 1).padStart(12, "0");
  const buyer = BUYERS[index % BUYERS.length];
  const product = PRODUCTS[index % PRODUCTS.length];
  const created = new Date(2026, 4, 25 - index, 14, 30, 0).toISOString();

  return {
    review_id: overrides.review_id ?? `arv-0000-4000-8000-${seed}`,
    order_item_id:
      overrides.order_item_id ??
      `so2000000-0000-4000-8000-${String(index + 10).padStart(12, "0")}`,
    product_id: overrides.product_id ?? product.product_id,
    product_title: overrides.product_title ?? product.product_title,
    product_thumbnail_url: overrides.product_thumbnail_url ?? product.product_thumbnail_url,
    buyer_id: overrides.buyer_id ?? buyer.buyer_id,
    buyer_display_name: overrides.buyer_display_name ?? buyer.buyer_display_name,
    buyer_avatar_url: overrides.buyer_avatar_url ?? buyer.buyer_avatar_url,
    seller_id: overrides.seller_id ?? MOCK_CART_DEMO_USER_ID,
    rating: overrides.rating ?? ((index % 5) + 1),
    comment: overrides.comment ?? COMMENTS[index % COMMENTS.length],
    status: overrides.status ?? "VISIBLE",
    created_at: overrides.created_at ?? created,
  };
}

function seedReviews() {
  const list = [
    buildReview(0, {
      review_id: MOCK_ADMIN_REVIEW_VISIBLE,
      rating: 5,
      status: "VISIBLE",
      comment: "Rất hài lòng, shop giao nhanh.",
    }),
    buildReview(1, {
      review_id: MOCK_ADMIN_REVIEW_HIDDEN,
      rating: 1,
      status: "HIDDEN",
      comment: "Nội dung vi phạm chính sách — spam quảng cáo.",
    }),
  ];

  for (let i = 2; i < 14; i += 1) {
    const status = i % 4 === 0 ? "HIDDEN" : "VISIBLE";
    list.push(buildReview(i, { status }));
  }

  return list;
}

const reviewsById = new Map(seedReviews().map((r) => [r.review_id, r]));

function computeSellerRating(sellerId) {
  const visible = [...reviewsById.values()].filter(
    (r) => r.seller_id === sellerId && r.status === "VISIBLE",
  );
  if (!visible.length) {
    return { rating_avg: 0, rating_count: 0 };
  }
  const sum = visible.reduce((acc, r) => acc + r.rating, 0);
  return {
    rating_avg: Math.round((sum / visible.length) * 100) / 100,
    rating_count: visible.length,
  };
}

export function validateAdminReviewListQuery({ page, limit, status, rating, q }) {
  const pageNum = Number(page);
  const limitNum = Number(limit);

  if (!Number.isInteger(pageNum) || pageNum < 1) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (!Number.isInteger(limitNum) || limitNum < 1 || limitNum > 50) {
    return { error: "COMMERCE-400-PAGINATION", status: 400 };
  }

  if (status && !VALID_STATUSES.includes(status)) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  if (rating != null && rating !== "") {
    const ratingNum = Number(rating);
    if (!Number.isInteger(ratingNum) || ratingNum < 1 || ratingNum > 5) {
      return { error: "COMMERCE-400-VALIDATION", status: 400 };
    }
    return { page: pageNum, limit: limitNum, status: status || null, rating: ratingNum, q: q || null };
  }

  return { page: pageNum, limit: limitNum, status: status || null, rating: null, q: q || null };
}

export function listAdminReviewsForAdmin({ page, limit, status, rating, q }) {
  let items = [...reviewsById.values()];

  if (status) {
    items = items.filter((r) => r.status === status);
  }

  if (rating != null) {
    items = items.filter((r) => r.rating === rating);
  }

  if (q) {
    const needle = String(q).trim().toLowerCase();
    if (needle) {
      items = items.filter(
        (r) =>
          r.order_item_id.toLowerCase().includes(needle) ||
          r.review_id.toLowerCase().includes(needle) ||
          r.product_title.toLowerCase().includes(needle),
      );
    }
  }

  items.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / limit) || 1);
  const start = (page - 1) * limit;

  return {
    data: {
      items: items.slice(start, start + limit),
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

export function moderateAdminReview(reviewId, body, { isAdmin }) {
  if (!isAdmin) {
    return { error: "COMMERCE-403", status: 403 };
  }

  const action = body?.action;
  const reason = String(body?.reason ?? "").trim();

  if (!VALID_ACTIONS.includes(action)) {
    return { error: "COMMERCE-400-REVIEW-MODERATION", status: 400 };
  }

  if (!reason) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const record = reviewsById.get(reviewId);
  if (!record) {
    return { error: "COMMERCE-404-REVIEW", status: 404 };
  }

  const previousStatus = record.status;
  let nextStatus = previousStatus;
  let alreadyModerated = false;

  if (action === "HIDE") {
    if (previousStatus === "HIDDEN") {
      alreadyModerated = true;
    } else if (previousStatus !== "VISIBLE") {
      return { error: "COMMERCE-400-REVIEW-MODERATION", status: 400 };
    } else {
      nextStatus = "HIDDEN";
    }
  }

  if (action === "RESTORE") {
    if (previousStatus === "VISIBLE") {
      alreadyModerated = true;
    } else if (previousStatus !== "HIDDEN") {
      return { error: "COMMERCE-400-REVIEW-MODERATION", status: 400 };
    } else {
      nextStatus = "VISIBLE";
    }
  }

  if (!alreadyModerated) {
    record.status = nextStatus;
    reviewsById.set(reviewId, record);
  }

  const ratingSummary = computeSellerRating(record.seller_id);

  return {
    data: {
      review_id: record.review_id,
      order_item_id: record.order_item_id,
      seller_id: record.seller_id,
      buyer_id: record.buyer_id,
      rating: record.rating,
      status: record.status,
      previous_status: previousStatus,
      already_moderated: alreadyModerated,
      seller_rating_avg: ratingSummary.rating_avg,
      seller_rating_count: ratingSummary.rating_count,
      moderated_at: new Date().toISOString(),
    },
    message: action === "HIDE" ? "An review thanh cong." : "Khoi phuc review thanh cong.",
  };
}

export function userHasAdminReviewAccess(user) {
  return Boolean(user?.is_admin);
}
