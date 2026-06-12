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

function normalizeMimeType(asset) {
  if (asset?.mimeType) return asset.mimeType;
  if (asset?.type && String(asset.type).includes("/")) return asset.type;
  if (asset?.type === "video") return "video/mp4";
  if (asset?.type === "image") return "image/jpeg";
  return "";
}

function normalizeFileSize(asset) {
  return asset?.fileSize ?? asset?.size ?? 0;
}

function normalizeAssets(assets) {
  if (!assets) return [];
  return Array.isArray(assets) ? assets : [assets];
}

export function validateReviewMediaSelection(assets, existingMediaCount = 0) {
  const list = normalizeAssets(assets);
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

  for (const asset of list) {
    const mimeType = normalizeMimeType(asset);
    if (!asset?.uri) {
      return { valid: false, message: "Không đọc được file đã chọn." };
    }
    if (!isImageType(mimeType) && !isVideoType(mimeType)) {
      return {
        valid: false,
        message: "Định dạng không hỗ trợ. Chọn JPEG, PNG, WebP, MP4 hoặc WebM.",
      };
    }
    const size = normalizeFileSize(asset);
    if (size <= 0) {
      return { valid: false, message: "File rỗng không được phép." };
    }
    if (isImageType(mimeType) && size > MAX_IMAGE_BYTES) {
      return { valid: false, message: "Ảnh tối đa 5MB mỗi file." };
    }
    if (isVideoType(mimeType) && size > MAX_VIDEO_BYTES) {
      return { valid: false, message: "Video tối đa 50MB mỗi file." };
    }
  }

  return { valid: true, files: list };
}
