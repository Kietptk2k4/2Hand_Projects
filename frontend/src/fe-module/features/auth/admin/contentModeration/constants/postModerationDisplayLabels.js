export const POST_STATUS_LABELS = {
  ACTIVE: "Đang hoạt động",
  DRAFT: "Nháp",
  DELETED: "Đã xóa",
};

export const POST_MODERATION_STATUS_LABELS = {
  NONE: "Chưa xử lý",
  HIDDEN: "Đã ẩn",
  REMOVED: "Đã gỡ",
};

export function getPostStatusLabel(status) {
  const normalized = String(status || "").toUpperCase();
  return POST_STATUS_LABELS[normalized] || normalized || "—";
}

export const POST_MODERATION_ACTION_LABELS = {
  HIDE: "Ẩn",
  REMOVE: "Gỡ",
  RESTORE: "Khôi phục",
};

export function getPostModerationActionLabel(action) {
  const normalized = String(action || "").toUpperCase();
  return POST_MODERATION_ACTION_LABELS[normalized] || normalized || "—";
}

export function getPostModerationStatusLabel(status) {
  const normalized = String(status || "").toUpperCase();
  return POST_MODERATION_STATUS_LABELS[normalized] || normalized || "—";
}
