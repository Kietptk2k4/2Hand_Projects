export const PAGE_SIZE = 20;
export const REASON_MAX_LENGTH = 1000;

export const PRODUCT_STATUS_FILTER_TABS = [
  { id: "all", label: "Tất cả", status: null },
  { id: "active", label: "Đang bán", status: "ACTIVE" },
  { id: "out_of_stock", label: "Hết hàng", status: "OUT_OF_STOCK" },
  { id: "removed", label: "Đã gỡ", status: "REMOVED" },
];

export const STATUS_LABELS = {
  ACTIVE: "Đang bán",
  OUT_OF_STOCK: "Hết hàng",
  REMOVED: "Đã gỡ",
  DRAFT: "Bản nháp",
  PAUSED: "Tạm dừng",
  ARCHIVED: "Đã lưu trữ",
};

export const STATUS_BADGE_CLASS = {
  ACTIVE: "bg-primary-container text-on-primary-container",
  OUT_OF_STOCK: "bg-amber-100 text-amber-950",
  REMOVED: "bg-error-container text-on-error-container",
  DRAFT: "bg-surface-container-high text-on-surface-variant",
  PAUSED: "border border-outline-variant bg-surface-container-low text-on-surface",
  ARCHIVED: "bg-surface-container-high text-outline",
};

export const REMOVE_WARNING_POINTS = [
  "Gỡ mềm (soft remove) — không xóa vật lý khỏi hệ thống.",
  "Ẩn khỏi discovery; chặn thêm giỏ hàng và checkout.",
  "Người bán không thể đăng bán lại sản phẩm này.",
  "Đơn đã đặt giữ snapshot; giỏ hàng đang active có thể chuyển INVALID_PRODUCT.",
];

export const ADMIN_PRODUCT_REMOVAL_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập không hợp lệ.",
  "COMMERCE-403": "Bạn không có quyền gỡ sản phẩm.",
  "COMMERCE-400-VALIDATION": "Vui lòng nhập lý do gỡ sản phẩm.",
  "COMMERCE-400-PAGINATION": "Tham số phân trang không hợp lệ.",
  "COMMERCE-404-PRODUCT": "Không tìm thấy sản phẩm.",
  "COMMERCE-409-PRODUCT-STATUS": "Trạng thái sản phẩm đã thay đổi. Vui lòng tải lại danh sách.",
};

export function mapAdminProductRemovalApiError(error) {
  const code = String(error?.code ?? "");
  return (
    ADMIN_PRODUCT_REMOVAL_ERROR_MESSAGES[code] ||
    error?.message ||
    "Có lỗi xảy ra. Vui lòng thử lại."
  );
}

export function buildRemoveProductSuccessToast(result) {
  if (result?.alreadyRemoved) {
    return "Sản phẩm đã được gỡ trước đó.";
  }

  const invalidated = result?.cartItemsInvalidated ?? 0;
  const cartPart =
    invalidated > 0
      ? ` Đã vô hiệu ${invalidated} mục trong giỏ hàng.`
      : "";

  return `Đã gỡ sản phẩm thành công.${cartPart}`;
}

export function buildRestoreProductSuccessToast() {
  return "Đã khôi phục sản phẩm thành công.";
}
