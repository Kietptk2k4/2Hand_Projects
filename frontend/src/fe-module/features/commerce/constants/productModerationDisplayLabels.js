export const PRODUCT_MODERATION_STATUS_LABELS = {
  ACTIVE: "Đang bán",
  OUT_OF_STOCK: "Hết hàng",
  REMOVED: "Đã gỡ",
  DRAFT: "Nháp",
  PAUSED: "Tạm dừng",
  ARCHIVED: "Đã lưu trữ",
};

export const PRODUCT_MODERATION_ACTION_LABELS = {
  PRODUCT_REMOVE: "Gỡ listing",
  PRODUCT_RESTORE: "Khôi phục",
  REMOVE: "Gỡ listing",
  RESTORE: "Khôi phục",
};

export function getProductStatusLabel(status) {
  return PRODUCT_MODERATION_STATUS_LABELS[status] || status || "—";
}

export function getProductModerationActionLabel(action) {
  return PRODUCT_MODERATION_ACTION_LABELS[action] || action || "—";
}
