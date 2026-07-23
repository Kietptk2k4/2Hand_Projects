export const WEBHOOK_LIST_PAGE_SIZE = 20;

export const WEBHOOK_LIST_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const WEBHOOK_PROVIDER_OPTIONS = [
  { value: "", label: "Tất cả nhà cung cấp" },
  { value: "PAYOS", label: "PayOS" },
  { value: "GHN", label: "GHN" },
];

export const WEBHOOK_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "PROCESSED", label: "Đã xử lý" },
  { value: "PENDING", label: "Chờ xử lý" },
  { value: "INVALID_SIGNATURE", label: "Chữ ký không hợp lệ" },
];

export const WEBHOOK_QUICK_FILTER_PRESETS = [
  { id: "today", label: "Hôm nay" },
  { id: "pending", label: "Chờ xử lý" },
  { id: "invalid_signature", label: "Lỗi chữ ký" },
  { id: "payos", label: "PayOS" },
  { id: "ghn", label: "GHN" },
];

export const WEBHOOK_PROCESSING_STATUS_LABELS = {
  PROCESSED: "Đã xử lý",
  PENDING: "Chờ xử lý",
  INVALID_SIGNATURE: "Chữ ký không hợp lệ",
};
