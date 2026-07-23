export const COMMENT_STATUS_LABELS = {
  ACTIVE: "Đang hoạt động",
  DELETED: "Đã xóa",
};

export const COMMENT_MODERATION_STATUS_LABELS = {
  NONE: "Chưa xử lý",
  HIDDEN: "Đã ẩn",
  REMOVED: "Đã gỡ",
};

export const COMMENT_MODERATION_ACTION_LABELS = {
  HIDE: "Ẩn",
  REMOVE: "Gỡ",
  RESTORE: "Khôi phục",
};

export function getCommentStatusLabel(status) {
  const normalized = String(status || "").toUpperCase();
  return COMMENT_STATUS_LABELS[normalized] || normalized || "—";
}

export function getCommentModerationStatusLabel(status) {
  const normalized = String(status || "").toUpperCase();
  return COMMENT_MODERATION_STATUS_LABELS[normalized] || normalized || "—";
}

export function getCommentModerationActionLabel(action) {
  const normalized = String(action || "").toUpperCase();
  return COMMENT_MODERATION_ACTION_LABELS[normalized] || normalized || "—";
}
