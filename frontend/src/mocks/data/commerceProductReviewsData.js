import {
  buildProductDetail,
  isValidProductId,
  MOCK_PRODUCT_DETAIL_IDS,
} from "./commerceProductDetailData";

export const MOCK_PRODUCT_REVIEW_FIXTURES = {
  MANY_REVIEWS: "c1000000-0000-4000-8000-000000000001",
  FEW_REVIEWS: "c1000000-0000-4000-8000-000000000002",
  NO_REVIEWS: "c1000000-0000-4000-8000-000000000007",
  NOT_FOUND: MOCK_PRODUCT_DETAIL_IDS.NOT_FOUND,
};

const REVIEW_COMMENTS = [
  "Ao dep, dung size nhu mo ta.",
  "Giao hang nhanh, dong goi can than.",
  "Chat lieu tot, gia hop ly second-hand.",
  null,
  "Mau sac hoi khac anh nhung van dep.",
  "Shop tu van nhiet tinh.",
  "San pham dung mo ta, se mua lai.",
  null,
  "Mac duoc 2 tuan, chua co van de gi.",
  "Tot trong tam gia.",
  "Co khuyet diem nho o tay ao.",
  "Danh gia trung binh, khong qua xuat sac.",
  "Form ao hoi rong hon mong doi.",
  "Rat hai long voi chat luong.",
  "Giao tre 1 ngay nhung san pham OK.",
  null,
  "Khong phu hop phong cach cua toi.",
  "Tuyet voi cho nguoi thich vintage.",
  "Dang tin cay, recommend.",
  "Chat luong on, gia hop ly.",
  "Giay con dep, de mac.",
  "Vay nhu moi, rat hai long.",
];

function buildReview(productId, index, rating) {
  const seed = String(index + 1).padStart(12, "0");
  const hasMedia = index % 4 === 0;
  const hasReply = index % 5 === 0;
  const comment = REVIEW_COMMENTS[index % REVIEW_COMMENTS.length];

  const createdAt = new Date(2026, 4, 28 - index, 10, 0, 0).toISOString();

  return {
    review_id: `r1000000-0000-4000-8000-${seed}`,
    rating,
    comment,
    created_at: createdAt,
    media: hasMedia
      ? [
          {
            media_id: `rm-${seed}-0`,
            url: `https://picsum.photos/seed/review-${productId.slice(-4)}-${index}/320/240`,
            media_type: index % 8 === 0 ? "VIDEO" : "IMAGE",
          },
          ...(index % 8 === 0
            ? []
            : [
                {
                  media_id: `rm-${seed}-1`,
                  url: `https://picsum.photos/seed/review2-${index}/320/240`,
                  media_type: "IMAGE",
                },
              ]),
        ]
      : [],
    seller_reply: hasReply
      ? {
          reply_id: `rr-${seed}`,
          content: "Cảm ơn bạn đã mua và đánh giá. Shop luôn hỗ trợ bảo hành đúng cam kết.",
          created_at: new Date(2026, 4, 29 - index, 14, 0, 0).toISOString(),
        }
      : null,
  };
}

function buildReviewsForProduct001() {
  const ratings = [5, 5, 5, 4, 5, 4, 5, 3, 5, 4, 5, 2, 5, 4, 5, 3, 5, 5, 4, 5, 1, 5];
  return ratings.map((rating, index) =>
    buildReview(MOCK_PRODUCT_REVIEW_FIXTURES.MANY_REVIEWS, index, rating)
  );
}

function buildReviewsForProduct002() {
  return [
    buildReview(MOCK_PRODUCT_REVIEW_FIXTURES.FEW_REVIEWS, 0, 5),
    buildReview(MOCK_PRODUCT_REVIEW_FIXTURES.FEW_REVIEWS, 1, 4),
  ];
}

const REVIEWS_BY_PRODUCT_ID = new Map([
  [MOCK_PRODUCT_REVIEW_FIXTURES.MANY_REVIEWS, buildReviewsForProduct001()],
  [MOCK_PRODUCT_REVIEW_FIXTURES.FEW_REVIEWS, buildReviewsForProduct002()],
  [MOCK_PRODUCT_REVIEW_FIXTURES.NO_REVIEWS, []],
]);

export function getAllReviewsForProduct(productId) {
  return REVIEWS_BY_PRODUCT_ID.get(productId) || [];
}

/** Buyer-created reviews (MSW write flow) — prepended to public list */
export function appendReviewForProduct(productId, review) {
  const list = REVIEWS_BY_PRODUCT_ID.get(productId) || [];
  REVIEWS_BY_PRODUCT_ID.set(productId, [review, ...list]);
}

export function updateReviewInProductList(productId, reviewId, patch) {
  const list = REVIEWS_BY_PRODUCT_ID.get(productId) || [];
  const index = list.findIndex((item) => item.review_id === reviewId);
  if (index < 0) return;
  list[index] = { ...list[index], ...patch };
  REVIEWS_BY_PRODUCT_ID.set(productId, list);
}

export function computeRatingSummary(reviews) {
  if (!reviews.length) {
    return { rating_avg: 0, rating_count: 0 };
  }
  const total = reviews.reduce((sum, item) => sum + item.rating, 0);
  const avg = Math.round((total / reviews.length) * 100) / 100;
  return { rating_avg: avg, rating_count: reviews.length };
}

const VALID_SORTS = ["NEWEST", "OLDEST", "RATING_DESC", "RATING_ASC"];

function sortReviews(reviews, sort) {
  const copy = [...reviews];
  if (sort === "OLDEST") {
    copy.sort((a, b) => new Date(a.created_at) - new Date(b.created_at));
    return copy;
  }
  if (sort === "RATING_DESC") {
    copy.sort((a, b) => b.rating - a.rating || new Date(b.created_at) - new Date(a.created_at));
    return copy;
  }
  if (sort === "RATING_ASC") {
    copy.sort((a, b) => a.rating - b.rating || new Date(b.created_at) - new Date(a.created_at));
    return copy;
  }
  copy.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
  return copy;
}

export function buildProductReviewsResponse(productId, { page, limit, rating, sort }) {
  const allReviews = getAllReviewsForProduct(productId);
  const ratingSummary = computeRatingSummary(allReviews);

  let filtered = allReviews;
  if (rating != null) {
    filtered = filtered.filter((item) => item.rating === rating);
  }

  const sorted = sortReviews(filtered, sort);
  const totalItems = sorted.length;
  const totalPages = totalItems === 0 ? 0 : Math.ceil(totalItems / limit);
  const start = (page - 1) * limit;
  const slice = sorted.slice(start, start + limit);

  return {
    product_id: productId,
    rating_summary: ratingSummary,
    reviews: slice,
    pagination: {
      page,
      limit,
      total_items: totalItems,
      total_pages: totalPages,
      has_next: page < totalPages,
    },
  };
}

export function isProductVisibleForReviews(productId) {
  if (!isValidProductId(productId)) return false;
  if (productId === MOCK_PRODUCT_REVIEW_FIXTURES.NOT_FOUND) return false;
  return Boolean(buildProductDetail(productId));
}

export { VALID_SORTS };
