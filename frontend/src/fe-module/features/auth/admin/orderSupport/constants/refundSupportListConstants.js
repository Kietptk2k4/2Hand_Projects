export const REFUND_LIST_PAGE_SIZE = 20;

export const REFUND_LIST_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
];

export const REFUND_SUPPORT_VIEW_MODES = {
  SUMMARY: "summary",
  DETAIL: "detail",
  NOTE: "note",
};

export const REFUND_STAT_PRESETS = [
  { id: "REQUESTED", label: "Chờ duyệt", status: "REQUESTED" },
  { id: "CONFIRMED", label: "Đã xác nhận", status: "CONFIRMED" },
  { id: "REJECTED", label: "Từ chối", status: "REJECTED" },
];

export const REFUND_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "REQUESTED", label: "Chờ duyệt" },
  { value: "CONFIRMED", label: "Đã hoàn tiền" },
  { value: "REJECTED", label: "Từ chối" },
];

export const REFUND_LIST_REQUESTED_BY_OPTIONS = [
  { value: "", label: "Tất cả người yêu cầu" },
  { value: "BUYER", label: "Người mua" },
  { value: "SELLER", label: "Người bán" },
];

export const REFUND_LIST_PAYMENT_METHOD_OPTIONS = [
  { value: "", label: "Tất cả phương thức" },
  { value: "VNPAY", label: "VNPay" },
  { value: "PAYOS", label: "PayOS" },
  { value: "COD", label: "COD" },
];

export const REFUND_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "requested", label: "Chờ duyệt" },
  { id: "confirmed", label: "Đã xác nhận" },
  { id: "rejected", label: "Từ chối" },
];

export const REFUND_DEFAULT_STATUS_FILTER = "REQUESTED";
