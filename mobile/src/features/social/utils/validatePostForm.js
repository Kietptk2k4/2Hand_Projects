import { MAX_CAPTION_LENGTH } from "../constants/createPostConstants";

export function validatePostForm({ caption, mediaItems, requireContent = true }) {
  const errors = {};

  if ((caption || "").length > MAX_CAPTION_LENGTH) {
    errors.caption = `Mô tả tối đa ${MAX_CAPTION_LENGTH} ký tự.`;
  }

  const hasUploading = mediaItems.some(
    (item) => item.status === "uploading" || item.status === "pending"
  );
  if (hasUploading) {
    errors.media = "Đang tải ảnh hoặc video, vui lòng đợi.";
  }

  const hasMediaError = mediaItems.some((item) => item.status === "error");
  if (hasMediaError) {
    errors.media = "Có file upload lỗi. Xóa hoặc thử lại.";
  }

  const readyCount = mediaItems.filter((item) => item.status === "done").length;
  if (requireContent && !caption?.trim() && readyCount === 0) {
    errors.caption = "Nhập mô tả hoặc thêm ít nhất một ảnh hoặc video.";
  }

  return errors;
}
