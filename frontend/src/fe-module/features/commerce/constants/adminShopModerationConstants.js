export const PAGE_SIZE = 20;

export const SHOP_STATUS_FILTER_TABS = [
  { id: "all", label: "Tất cả", status: null },
  { id: "active", label: "Active", status: "ACTIVE" },
  { id: "suspended", label: "Suspended", status: "SUSPENDED" },
  { id: "closed", label: "Closed", status: "CLOSED" },
];

export const SORT_OPTIONS = [
  { value: "NEWEST", label: "Mới nhất" },
  { value: "OLDEST", label: "Cũ nhất" },
  { value: "NAME_ASC", label: "Tên (A–Z)" },
];

export const MODERATION_ACTIONS = {
  SUSPEND: "SUSPEND",
  CLOSE: "CLOSE",
  RESTORE: "RESTORE",
};

export const ACTION_LABELS = {
  SUSPEND: "Tạm ngưng (Suspend)",
  CLOSE: "Đóng shop (Close)",
  RESTORE: "Khôi phục (Restore)",
};

export const ACTION_DESCRIPTIONS = {
  SUSPEND: "Shop sẽ bị ẩn khỏi marketplace và chặn mua hàng mới.",
  CLOSE: "Đóng vĩnh viễn shop; giỏ hàng liên quan có thể bị vô hiệu.",
  RESTORE: "Đưa shop về trạng thái hoạt động.",
};

export const SHOP_STATUS_LABELS = {
  ACTIVE: "Active",
  SUSPENDED: "Suspended",
  CLOSED: "Closed",
};

export const SHOP_STATUS_BADGE_CLASS = {
  ACTIVE: "bg-emerald-100 text-emerald-900",
  SUSPENDED: "bg-error-container text-on-error-container",
  CLOSED: "bg-surface-container-high text-on-surface-variant",
};

/** Transition theo ModerateShop API doc */
export const ALLOWED_ACTIONS_BY_STATUS = {
  ACTIVE: ["SUSPEND", "CLOSE"],
  SUSPENDED: ["RESTORE", "CLOSE"],
  CLOSED: ["RESTORE"],
};

export const REASON_MAX_LENGTH = 1000;

export const RESTORE_WARNING =
  "Khôi phục shop không tự động publish lại sản phẩm đã archive hoặc removed.";

export const ADMIN_SHOP_MODERATION_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập không hợp lệ.",
  "COMMERCE-403": "Bạn không có quyền thực hiện thao tác này.",
  "COMMERCE-400-VALIDATION": "Vui lòng nhập lý do moderation.",
  "COMMERCE-400-PAGINATION": "Tham số phân trang không hợp lệ.",
  "COMMERCE-400-SHOP-MODERATION": "Hành động moderation không hợp lệ.",
  "COMMERCE-404-SHOP": "Không tìm thấy cửa hàng.",
  "COMMERCE-409-SHOP-STATUS": "Không thể chuyển trạng thái shop theo hành động đã chọn.",
};

export function mapAdminShopModerationApiError(error) {
  const code = String(error?.code ?? "");
  return (
    ADMIN_SHOP_MODERATION_ERROR_MESSAGES[code] ||
    error?.message ||
    "Có lỗi xảy ra. Vui lòng thử lại."
  );
}

export function getAllowedActionsForStatus(status) {
  return ALLOWED_ACTIONS_BY_STATUS[status] || [];
}

export function buildModerateSuccessToast(action, alreadyModerated) {
  if (alreadyModerated) {
    return "Shop đã ở trạng thái mục tiêu — không có thay đổi thêm.";
  }
  if (action === "SUSPEND") return "Đã tạm ngưng shop thành công.";
  if (action === "CLOSE") return "Đã đóng shop thành công.";
  if (action === "RESTORE") return "Đã khôi phục shop thành công.";
  return "Moderation thành công.";
}
