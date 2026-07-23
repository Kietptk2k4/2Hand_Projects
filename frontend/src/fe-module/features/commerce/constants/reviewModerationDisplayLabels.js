import { REVIEW_STATUS_LABELS } from "./adminReviewModerationConstants.js";

const REVIEW_MODERATION_ACTION_LABELS = {
  REVIEW_HIDE: "Ẩn đánh giá",
  REVIEW_REMOVE: "Gỡ đánh giá",
  REVIEW_RESTORE: "Khôi phục đánh giá",
};

export function getReviewStatusLabel(status) {
  return REVIEW_STATUS_LABELS[status] || status || "—";
}

export function getReviewModerationActionLabel(action) {
  const normalized = String(action || "").toUpperCase();
  return REVIEW_MODERATION_ACTION_LABELS[normalized] || action || "—";
}
