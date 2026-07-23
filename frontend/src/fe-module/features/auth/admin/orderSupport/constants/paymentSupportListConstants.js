export const PAYMENT_LIST_PAGE_SIZE = 20;

export const PAYMENT_LIST_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const PAYMENT_SUPPORT_VIEW_MODES = {
  SUMMARY: "summary",
  TIMELINE: "timeline",
  WEBHOOKS: "webhooks",
};

export const PAYMENT_STAT_PRESETS = [
  { id: "PAID", label: "Đã thanh toán", status: "PAID" },
  { id: "PENDING", label: "Chờ thanh toán", status: "PENDING" },
  { id: "FAILED", label: "Thất bại", status: "FAILED" },
  { id: "EXPIRED", label: "Hết hạn", status: "EXPIRED" },
];

export const PAYMENT_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "PENDING", label: "Chờ thanh toán" },
  { value: "PAID", label: "Đã thanh toán" },
  { value: "FAILED", label: "Thất bại" },
  { value: "CANCELLED", label: "Đã hủy" },
  { value: "EXPIRED", label: "Hết hạn" },
];

export const PAYMENT_LIST_METHOD_OPTIONS = [
  { value: "", label: "Tất cả phương thức" },
  { value: "COD", label: "COD" },
  { value: "PAYOS", label: "PayOS" },
  { value: "VNPAY", label: "VNPay" },
];

export const PAYMENT_LIST_RECONCILIATION_OPTIONS = [
  { value: "", label: "Tất cả đối soát" },
  { value: "RECONCILED", label: "Đã đối soát" },
  { value: "OUTSTANDING", label: "Chưa đối soát" },
  { value: "AWAITING_WEBHOOK", label: "Chờ webhook" },
  { value: "WEBHOOK_RECEIVED", label: "Đã nhận webhook" },
  { value: "NOT_APPLICABLE", label: "Không áp dụng" },
];

export const PAYMENT_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "paid", label: "Đã thanh toán" },
  { id: "pending", label: "Chờ thanh toán" },
  { id: "failed", label: "Thất bại" },
  { id: "expired", label: "Hết hạn" },
];
