import {
  ALLOWED_IMAGE_TYPES,
  ALLOWED_VIDEO_TYPES,
  MAX_IMAGE_BYTES,
  MAX_REVIEW_MEDIA,
  MAX_VIDEO_BYTES,
} from "../constants/reviewMediaConstants";

function isImageType(type) {
  return ALLOWED_IMAGE_TYPES.includes(type);
}

function isVideoType(type) {
  return ALLOWED_VIDEO_TYPES.includes(type);
}

export function validateReviewMediaSelection(files, existingMediaCount = 0) {
  const list = Array.from(files || []);
  if (!list.length) {
    return { valid: true, files: [] };
  }

  const remaining = MAX_REVIEW_MEDIA - existingMediaCount;
  if (remaining <= 0) {
    return {
      valid: false,
      message: `Đánh giá đã đủ ${MAX_REVIEW_MEDIA} ảnh/video.`,
    };
  }

  if (list.length > remaining) {
    return {
      valid: false,
      message: `Chỉ còn có thể thêm ${remaining} file (tối đa ${MAX_REVIEW_MEDIA}/đánh giá).`,
    };
  }

  for (const file of list) {
    if (!isImageType(file.type) && !isVideoType(file.type)) {
      return {
        valid: false,
        message: "Định dạng không hỗ trợ. Chọn JPEG, PNG, WebP, MP4 hoặc WebM.",
      };
    }
    if (file.size <= 0) {
      return { valid: false, message: "File rỗng không được phép." };
    }
    if (isImageType(file.type) && file.size > MAX_IMAGE_BYTES) {
      return { valid: false, message: "Ảnh tối đa 5MB mỗi file." };
    }
    if (isVideoType(file.type) && file.size > MAX_VIDEO_BYTES) {
      return { valid: false, message: "Video tối đa 50MB mỗi file." };
    }
  }

  return { valid: true, files: list };
}
