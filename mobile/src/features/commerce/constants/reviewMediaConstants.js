export const MAX_REVIEW_MEDIA = 10;
export const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
export const MAX_VIDEO_BYTES = 50 * 1024 * 1024;

export const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];
export const ALLOWED_VIDEO_TYPES = ["video/mp4", "video/webm"];

export const REVIEW_MEDIA_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập đã hết hạn.",
  "COMMERCE-404-REVIEW": "Không tìm thấy đánh giá.",
  "COMMERCE-409-REVIEW-VISIBLE": "Không thể thêm ảnh/video cho đánh giá đã bị ẩn.",
  "COMMERCE-409-REVIEW-MEDIA": "Mỗi đánh giá chỉ được tối đa 10 ảnh/video.",
  "COMMERCE-400-VALIDATION": "Vui lòng chọn ít nhất một file hợp lệ.",
  "COMMERCE-400-MEDIA-TYPE": "Định dạng file không được hỗ trợ (JPEG, PNG, WebP, MP4, WebM).",
  "COMMERCE-400-MEDIA-SIZE": "File vượt quá dung lượng cho phép (ảnh 5MB, video 50MB).",
  "COMMERCE-503-MINIO": "Không thể tải media lên. Vui lòng thử lại sau.",
};

export const REVIEW_MEDIA_TOAST = {
  uploadSuccess: "Đã tải ảnh/video lên đánh giá.",
  uploadPartialFail:
    "Đánh giá đã được gửi nhưng chưa tải được ảnh/video. Bạn có thể thử lại khi sửa đánh giá.",
};

export function mapReviewMediaApiError(error) {
  const code = String(error?.code ?? "");
  return REVIEW_MEDIA_ERROR_MESSAGES[code] || error?.message || "Không thể tải media. Vui lòng thử lại.";
}
