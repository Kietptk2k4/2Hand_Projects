export const PAGE_SIZE = 10;

export const VALID_ORDER_STATUSES = [
  "CREATED",
  "AWAITING_PAYMENT",
  "PROCESSING",
  "COMPLETED",
  "CANCELLED",
];

export const ORDER_STATUS_FILTERS = [
  { id: "all", label: "Tất cả", status: null },
  { id: "awaiting_payment", label: "Chờ thanh toán", status: "AWAITING_PAYMENT" },
  { id: "processing", label: "Đang xử lý", status: "PROCESSING" },
  { id: "completed", label: "Hoàn thành", status: "COMPLETED" },
  { id: "cancelled", label: "Đã hủy", status: "CANCELLED" },
];

export const ORDER_STATUS_LABELS = {
  CREATED: "Đã tạo",
  AWAITING_PAYMENT: "Chờ thanh toán",
  PROCESSING: "Đang xử lý",
  COMPLETED: "Hoàn thành",
  CANCELLED: "Đã hủy",
};

export const ORDER_PAYMENT_STATUS_LABELS = {
  PENDING: "Chờ thanh toán",
  PAID: "Đã thanh toán",
  FAILED: "Thất bại",
  CANCELLED: "Đã hủy",
  EXPIRED: "Hết hạn",
};

export const PAYMENT_METHOD_LABELS = {
  PAYOS: "PayOS",
  COD: "Thanh toán khi nhận hàng",
};