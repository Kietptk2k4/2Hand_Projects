export const SHOP_MODERATION_STATUS_LABELS = {
  ACTIVE: "Đang hoạt động",
  SUSPENDED: "Tạm ngưng",
  CLOSED: "Đã đóng",
};

export const SHOP_MODERATION_ACTION_LABELS = {
  SUSPEND: "Tạm ngưng",
  CLOSE: "Đóng shop",
  RESTORE: "Khôi phục",
};

export const SHOP_MODERATION_ACTION_DESCRIPTIONS = {
  SUSPEND: "Shop sẽ bị ẩn khỏi marketplace và chặn mua hàng mới.",
  CLOSE: "Đóng vĩnh viễn shop; giỏ hàng liên quan có thể bị vô hiệu.",
  RESTORE: "Đưa shop về trạng thái hoạt động.",
};

export function getShopStatusLabel(status) {
  return SHOP_MODERATION_STATUS_LABELS[status] || status || "—";
}

export function getShopModerationActionLabel(action) {
  return SHOP_MODERATION_ACTION_LABELS[action] || action || "—";
}
