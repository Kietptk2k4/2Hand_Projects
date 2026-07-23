export const ORDER_LIST_PAGE_SIZE = 20;

export const ORDER_LIST_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const ORDER_SUPPORT_VIEW_MODES = {
  SUMMARY: "summary",
  ITEMS: "items",
  TIMELINE: "timeline",
};

export const ORDER_STAT_PRESETS = [
  { id: "PROCESSING", label: "Đang xử lý", status: "PROCESSING" },
  { id: "COMPLETED", label: "Hoàn thành", status: "COMPLETED" },
  { id: "CANCELLED", label: "Đã hủy", status: "CANCELLED" },
  { id: "AWAITING_PAYMENT", label: "Chờ thanh toán", status: "AWAITING_PAYMENT" },
];

export const ORDER_LIST_SORT_OPTIONS = [
  { value: "created_at", label: "Tạo gần nhất" },
  { value: "updated_at", label: "Cập nhật gần nhất" },
];

export const ORDER_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "CREATED", label: "Mới tạo" },
  { value: "AWAITING_PAYMENT", label: "Chờ thanh toán" },
  { value: "PROCESSING", label: "Đang xử lý" },
  { value: "COMPLETED", label: "Hoàn thành" },
  { value: "CANCELLED", label: "Đã hủy" },
];

export const ORDER_LIST_PAYMENT_METHOD_OPTIONS = [
  { value: "", label: "Tất cả phương thức" },
  { value: "COD", label: "COD" },
  { value: "PAYOS", label: "PayOS" },
  { value: "VNPAY", label: "VNPay" },
];

export const ORDER_LIST_PAYMENT_STATUS_OPTIONS = [
  { value: "", label: "Tất cả thanh toán" },
  { value: "PAID", label: "Đã thanh toán" },
  { value: "PENDING", label: "Chờ thanh toán" },
  { value: "FAILED", label: "Thất bại" },
  { value: "EXPIRED", label: "Hết hạn" },
];

export const ORDER_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "processing", label: "Đang xử lý" },
  { id: "completed", label: "Hoàn thành" },
  { id: "cancelled", label: "Đã hủy" },
  { id: "awaiting_payment", label: "Chờ thanh toán" },
];
