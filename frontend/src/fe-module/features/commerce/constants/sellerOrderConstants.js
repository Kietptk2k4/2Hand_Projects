export const PAGE_SIZE = 20;

export const VALID_ITEM_STATUSES = [
  "PENDING",
  "PROCESSING",
  "SHIPPED",
  "DELIVERED",
  "COMPLETED",
  "CANCELLED",
  "FAILED",
  "RETURNED",
];

export const VALID_SHIPMENT_STATUSES = [
  "PENDING",
  "PICKING_UP",
  "READY_TO_SHIP",
  "SHIPPED",
  "DELIVERED",
  "FAILED",
  "CANCELLED",
  "RETURNED",
];

export const STATUS_TABS = [
  { id: "all", label: "Tất cả", status: null },
  { id: "pending", label: "Chờ xử lý", status: "PENDING" },
  { id: "processing", label: "Đang chuẩn bị", status: "PROCESSING" },
  { id: "shipped", label: "Đang giao", status: "SHIPPED" },
  { id: "delivered", label: "Đã giao", status: "DELIVERED" },
  { id: "completed", label: "Hoàn tất", status: "COMPLETED" },
  { id: "cancelled", label: "Đã hủy", status: "CANCELLED" },
];

export const ITEM_STATUS_LABELS = {
  PENDING: "Chờ xử lý",
  PROCESSING: "Đang chuẩn bị",
  SHIPPED: "Đang giao",
  DELIVERED: "Đã giao",
  COMPLETED: "Hoàn tất",
  CANCELLED: "Đã hủy",
  FAILED: "Thất bại",
  RETURNED: "Trả hàng",
};

export const ITEM_STATUS_BADGE_CLASS = {
  PENDING: "bg-amber-100 text-amber-900",
  PROCESSING: "bg-primary/10 text-primary",
  SHIPPED: "bg-sky-100 text-sky-900",
  DELIVERED: "bg-emerald-100 text-emerald-900",
  COMPLETED: "bg-emerald-100 text-emerald-900",
  CANCELLED: "bg-error-container text-on-error-container",
  FAILED: "bg-error-container/80 text-on-error-container",
  RETURNED: "bg-surface-container-high text-on-surface-variant",
};

export const SHIPMENT_STATUS_LABELS = {
  PENDING: "Chờ lấy hàng",
  PICKING_UP: "Đang lấy hàng",
  READY_TO_SHIP: "Sẵn sàng giao",
  SHIPPED: "Đang giao",
  DELIVERED: "Đã giao",
  FAILED: "Thất bại",
  CANCELLED: "Đã hủy",
  RETURNED: "Trả hàng",
};

export const ORDER_PAYMENT_STATUS_LABELS = {
  PENDING: "Chờ TT",
  PAID: "Đã TT",
  FAILED: "TT thất bại",
  EXPIRED: "Hết hạn",
};

export const PAYMENT_METHOD_LABELS = {
  PAYOS: "PayOS",
  COD: "COD",
};

export function formatOrderPaymentSubline(orderPaymentStatus, orderPaymentMethod) {
  if (orderPaymentMethod === "COD") return "COD";
  if (orderPaymentStatus === "PAID") return "Đã thanh toán";
  if (orderPaymentStatus === "PENDING") return "Chờ thanh toán";
  return ORDER_PAYMENT_STATUS_LABELS[orderPaymentStatus] || orderPaymentStatus || "";
}

export const SHIPMENT_FILTER_OPTIONS = [
  { value: "", label: "Tất cả vận chuyển" },
  ...VALID_SHIPMENT_STATUSES.map((status) => ({
    value: status,
    label: SHIPMENT_STATUS_LABELS[status] || status,
  })),
];

export const SELLER_ORDER_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập không hợp lệ.",
  "COMMERCE-400-PAGINATION": "Tham số phân trang không hợp lệ.",
  "COMMERCE-400-VALIDATION": "Chưa chọn mục đơn hoặc dữ liệu không hợp lệ.",
  "COMMERCE-404-ORDER-ITEM": "Một hoặc nhiều mục đơn không tồn tại.",
  "COMMERCE-409-ORDER-PROCESSING": "Đơn hàng chưa sẵn sàng xử lý.",
  "COMMERCE-409-PAYMENT-STATE": "Thanh toán PayOS chưa hoàn tất.",
  "COMMERCE-409-ORDER-ITEM-PROCESS": "Trạng thái mục đơn không cho phép thao tác này.",
  "COMMERCE-409-SELLER-SHOP": "Bạn chưa có cửa hàng. Hãy tạo shop trước khi xem đơn bán.",
};

export function mapSellerOrderApiError(error) {
  const code = String(error?.code ?? "");
  return SELLER_ORDER_ERROR_MESSAGES[code] || error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
}

export function buildProcessSuccessToast({ newlyProcessedCount, alreadyProcessingCount }) {
  let message = `Đã xác nhận chuẩn bị ${newlyProcessedCount} mục đơn`;
  if (alreadyProcessingCount > 0) {
    message += ` (${alreadyProcessingCount} mục đã ở trạng thái chuẩn bị)`;
  }
  return message;
}
