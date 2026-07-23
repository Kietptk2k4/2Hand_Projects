export const PRODUCT_MODERATION_LIST_PAGE_SIZE = 20;

export const PRODUCT_MODERATION_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const PRODUCT_MODERATION_LIST_SORT_OPTIONS = [
  { value: "NEWEST", label: "Mới nhất" },
  { value: "OLDEST", label: "Cũ nhất" },
  { value: "UPDATED_AT", label: "Cập nhật gần đây" },
  { value: "PRICE_ASC", label: "Giá thấp → cao" },
  { value: "PRICE_DESC", label: "Giá cao → thấp" },
];

export const PRODUCT_MODERATION_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "ACTIVE", label: "Đang bán" },
  { value: "OUT_OF_STOCK", label: "Hết hàng" },
  { value: "REMOVED", label: "Đã gỡ" },
  { value: "PAUSED", label: "Tạm dừng" },
  { value: "DRAFT", label: "Nháp" },
];

export const PRODUCT_MODERATION_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "active", label: "Đang bán" },
  { id: "needs_attention", label: "Cần xử lý" },
  { id: "removed", label: "Đã gỡ" },
];

export const PRODUCT_MODERATION_STAT_PRESETS = [
  { id: "active", label: "Đang bán", status: "ACTIVE" },
  { id: "out_of_stock", label: "Hết hàng", status: "OUT_OF_STOCK" },
  { id: "removed", label: "Đã gỡ", status: "REMOVED" },
];
