export const ANNOUNCEMENT_LIST_PAGE_SIZE = 20;

export const ANNOUNCEMENT_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const ANNOUNCEMENT_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "draft", label: "Draft" },
  { id: "sent", label: "Đã gửi" },
  { id: "cancelled", label: "Đã hủy" },
  { id: "warning", label: "Cảnh báo" },
];

export const ANNOUNCEMENT_STAT_PRESETS = [
  { id: "DRAFT", label: "Draft", status: "DRAFT" },
  { id: "SENT", label: "Đã gửi", status: "SENT" },
  { id: "CANCELLED", label: "Đã hủy", status: "CANCELLED" },
];

export const ANNOUNCEMENT_STATUS_LABELS = {
  DRAFT: "Draft",
  SENT: "Đã gửi",
  CANCELLED: "Đã hủy",
};

export const ANNOUNCEMENT_SEVERITY_LABELS = {
  INFO: "Thông tin",
  WARNING: "Cảnh báo",
  CRITICAL: "Nghiêm trọng",
};

export const ANNOUNCEMENT_VIEW_MODES = {
  DETAIL: "detail",
  ACTIONS: "actions",
};

export const ANNOUNCEMENT_PUBLISH_AUDIENCE_OPTIONS = [
  { id: "ALL_ACTIVE_USERS", label: "Toàn bộ user đang hoạt động" },
  { id: "RECIPIENT_LIST", label: "Danh sách user cụ thể (UUID)" },
  { id: "DEV_FALLBACK", label: "Dùng cấu hình dev trên server" },
];
