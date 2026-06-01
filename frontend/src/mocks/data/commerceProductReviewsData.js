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
  "Máy khoan rất khỏe, dùng công trình cả ngày vẫn ổn.",
  "Giao hàng nhanh, đóng gói cẩn thận.",
  "Pin trâu, đầu khoan chắc. Đáng tiền.",
  null,
  "Hơi nặng tay nhưng công suất tốt.",
  "Shop tư vấn nhiệt tình.",
  "Sản phẩm đúng mô tả, sẽ mua lại.",
  null,
  "Dùng được 2 tuần, chưa có vấn đề gì.",
  "Tốt trong tầm giá.",
  "Cần thêm phụ kiện trong hộp.",
  "Đánh giá trung bình, không quá xuất sắc.",
  "Pin hơi nóng khi khoan liên tục.",
  "Rất hài lòng với chất lượng.",
  "Giao trễ 1 ngày nhưng sản phẩm OK.",
  null,
  "Không phù hợp nhu cầu nhẹ, hơi cồng kềnh.",
  "Tuyệt vời cho thợ chuyên nghiệp.",
  "Đáng tin cậy, recommend.",
  "Chất lượng ổn, giá hơi cao.",
  "Pin sạc nhanh, tiện mang đi công trình.",
  "Đầu khoan bền, chưa mài sau nhiều lần dùng.",
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
