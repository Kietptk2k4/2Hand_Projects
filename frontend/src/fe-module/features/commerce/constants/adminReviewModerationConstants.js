export const PAGE_SIZE = 20;
export const REASON_MAX_LENGTH = 1000;

export const REVIEW_STATUS_FILTER_TABS = [
  { id: "all", label: "Tất cả", status: null },
  { id: "visible", label: "Công khai", status: "VISIBLE" },
  { id: "hidden", label: "Bị ẩn", status: "HIDDEN" },
];

export const RATING_FILTER_OPTIONS = [
  { value: "", label: "Tất cả sao" },
  { value: "5", label: "5 sao" },
  { value: "4", label: "4 sao" },
  { value: "3", label: "3 sao" },
  { value: "2", label: "2 sao" },
  { value: "1", label: "1 sao" },
];

export const MODERATION_ACTIONS = {
  HIDE: "HIDE",
  RESTORE: "RESTORE",
};

export const ACTION_LABELS = {
  HIDE: "Ẩn đánh giá (Hide)",
  RESTORE: "Khôi phục công khai (Restore)",
};

export const ACTION_DESCRIPTIONS = {
  HIDE: "Ẩn đánh giá khỏi danh sách công khai trên marketplace.",
  RESTORE: "Khôi phục đánh giá để hiển thị lại công khai.",
};

export const REVIEW_STATUS_LABELS = {
  VISIBLE: "Công khai",
  HIDDEN: "Bị ẩn",
};

export const REVIEW_STATUS_BADGE_CLASS = {
  VISIBLE: "bg-emerald-100 text-emerald-900",
  HIDDEN: "bg-error-container text-on-error-container",
};

export const HIDE_WARNING =
  "Đánh giá sẽ biến mất khỏi danh sách công khai. Rating shop chỉ tính từ review đang hiển thị (VISIBLE).";

export const RESTORE_WARNING =
  "Đánh giá sẽ hiển thị lại công khai. Rating shop (seller_rating_avg / seller_rating_count) sẽ được cập nhật.";

export const ADMIN_REVIEW_MODERATION_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập không hợp lệ.",
  "COMMERCE-403": "Bạn không có quyền kiểm duyệt đánh giá.",
  "COMMERCE-400-VALIDATION": "Vui lòng nhập lý do kiểm duyệt.",
  "COMMERCE-400-PAGINATION": "Tham số phân trang không hợp lệ.",
  "COMMERCE-400-REVIEW-MODERATION": "Hành động không hợp lệ với trạng thái hiện tại.",
  "COMMERCE-404-REVIEW": "Không tìm thấy đánh giá.",
};

export function mapAdminReviewModerationApiError(error) {
  const code = String(error?.code ?? "");
  return (
    ADMIN_REVIEW_MODERATION_ERROR_MESSAGES[code] ||
    error?.message ||
    "Có lỗi xảy ra. Vui lòng thử lại."
  );
}

export function getAllowedActionsForReviewStatus(status) {
  if (status === "VISIBLE") return ["HIDE"];
  if (status === "HIDDEN") return ["RESTORE"];
  return [];
}

export function getDefaultActionForReview(status) {
  const allowed = getAllowedActionsForReviewStatus(status);
  return allowed[0] || "";
}

export function buildModerateReviewSuccessToast(action, result) {
  const ratingPart =
    result?.sellerRatingCount != null && result.sellerRatingCount > 0
      ? ` Shop rating: ${Number(result.sellerRatingAvg).toFixed(1)} (${result.sellerRatingCount} đánh giá).`
      : result?.sellerRatingCount === 0
        ? " Shop chưa có đánh giá công khai."
        : "";

  if (result?.alreadyModerated) {
    return `Đánh giá đã ở trạng thái mục tiêu.${ratingPart}`;
  }
  if (action === "HIDE") return `Đã ẩn đánh giá thành công.${ratingPart}`;
  if (action === "RESTORE") return `Đã khôi phục đánh giá thành công.${ratingPart}`;
  return `Kiểm duyệt thành công.${ratingPart}`;
}
