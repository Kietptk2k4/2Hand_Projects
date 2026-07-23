export const SHOP_MODERATION_LIST_PAGE_SIZE = 20;

export const SHOP_MODERATION_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const SHOP_MODERATION_LIST_SORT_OPTIONS = [
  { value: "NEWEST", label: "Mới nhất" },
  { value: "OLDEST", label: "Cũ nhất" },
  { value: "UPDATED_AT", label: "Cập nhật gần đây" },
  { value: "NAME_ASC", label: "Tên (A–Z)" },
];

export const SHOP_MODERATION_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "ACTIVE", label: "Đang hoạt động" },
  { value: "SUSPENDED", label: "Tạm ngưng" },
  { value: "CLOSED", label: "Đã đóng" },
];

export const SHOP_MODERATION_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "active", label: "Đang hoạt động" },
  { id: "needs_attention", label: "Cần xử lý" },
  { id: "suspended", label: "Tạm ngưng" },
  { id: "closed", label: "Đã đóng" },
];

export const SHOP_MODERATION_STAT_PRESETS = [
  { id: "active", label: "Đang hoạt động", status: "ACTIVE" },
  { id: "suspended", label: "Tạm ngưng", status: "SUSPENDED" },
  { id: "closed", label: "Đã đóng", status: "CLOSED" },
];
