export const RATING_MIN = 1;
export const RATING_MAX = 5;
export const MAX_COMMENT_LENGTH = 500;

export const PRODUCT_REVIEW_ERROR_MESSAGES = {
  "COMMERCE-400-RATING": "Điểm đánh giá phải từ 1 đến 5 sao.",
  "COMMERCE-400-VALIDATION": "Vui lòng kiểm tra lại thông tin đánh giá.",
  "COMMERCE-404-ORDER-ITEM": "Không tìm thấy sản phẩm trong đơn hàng.",
  "COMMERCE-404-REVIEW": "Không tìm thấy đánh giá.",
  "COMMERCE-409-ORDER-ITEM-REVIEW": "Chỉ có thể đánh giá khi đơn hàng đã hoàn thành.",
  "COMMERCE-409-REVIEW-EXISTS": "Bạn đã đánh giá sản phẩm này rồi.",
  "COMMERCE-409-REVIEW-VISIBLE": "Không thể sửa đánh giá đã bị ẩn.",
};

export function mapProductReviewApiError(error) {
  const code = String(error?.code ?? "");
  return PRODUCT_REVIEW_ERROR_MESSAGES[code] || error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
}
