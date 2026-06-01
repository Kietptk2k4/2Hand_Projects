export const PAGE_SIZE = 20;
export const REPLY_CONTENT_MAX = 1000;

export const REVIEW_STATUS_OPTIONS = [
  { value: "VISIBLE", label: "Hiển thị" },
  { value: "HIDDEN", label: "Ẩn (moderation)" },
];

export const REPLY_FILTER_TABS = [
  { id: "all", label: "Tất cả" },
  { id: "unreplied", label: "Chưa trả lời" },
  { id: "replied", label: "Đã trả lời" },
];

export const RATING_FILTER_OPTIONS = [
  { value: "", label: "Tất cả sao" },
  { value: "5", label: "5★" },
  { value: "4", label: "4★" },
  { value: "3", label: "3★" },
  { value: "2", label: "2★" },
  { value: "1", label: "1★" },
];

export const REVIEW_STATUS_LABELS = {
  VISIBLE: "Hiển thị",
  HIDDEN: "Ẩn",
};

export const SELLER_SHOP_REVIEWS_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập không hợp lệ.",
  "COMMERCE-400-PAGINATION": "Tham số phân trang không hợp lệ.",
  "COMMERCE-400-RATING": "Bộ lọc sao không hợp lệ.",
  "COMMERCE-400-VALIDATION": "Dữ liệu không hợp lệ.",
  "COMMERCE-404-REVIEW": "Không tìm thấy đánh giá.",
  "COMMERCE-409-SELLER-SHOP": "Bạn chưa có cửa hàng.",
  "COMMERCE-409-REVIEW-VISIBLE": "Chỉ phản hồi đánh giá đang hiển thị.",
  "COMMERCE-409-REVIEW-REPLY": "Đánh giá này đã có phản hồi.",
};

export function mapSellerShopReviewsApiError(error) {
  const code = String(error?.code ?? "");
  return (
    SELLER_SHOP_REVIEWS_ERROR_MESSAGES[code] || error?.message || "Có lỗi xảy ra. Vui lòng thử lại."
  );
}
