export const PAGE_SIZE = 20;

export const CARRIERS = [
  { value: "GHN", label: "GHN" },
  { value: "MANUAL", label: "Tự giao (Manual)" },
  { value: "SELF_DELIVERY", label: "Tự vận chuyển" },
];

export const SHIPMENT_TYPES = [
  { value: "STANDARD", label: "Tiêu chuẩn" },
  { value: "EXPRESS", label: "Hỏa tốc" },
  { value: "SAME_DAY", label: "Trong ngày" },
];

/** Khớp checkout — buyer luôn quote STANDARD; seller không đổi khi tạo vận đơn */
export const CREATE_SHIPMENT_TYPE = "STANDARD";

export const STATUS_TABS = [
  { id: "all", label: "Tất cả", status: null },
  { id: "pending", label: "PENDING", status: "PENDING" },
  { id: "ready", label: "READY_TO_SHIP", status: "READY_TO_SHIP" },
  { id: "shipped", label: "SHIPPED", status: "SHIPPED" },
  { id: "delivered", label: "DELIVERED", status: "DELIVERED" },
  { id: "failed", label: "FAILED", status: "FAILED" },
];

export const SHIPMENT_STATUS_LABELS = {
  PENDING: "Chờ xử lý",
  PICKING_UP: "Đang lấy hàng",
  READY_TO_SHIP: "Sẵn sàng giao",
  SHIPPED: "Đang giao",
  DELIVERED: "Đã giao",
  FAILED: "Thất bại",
  CANCELLED: "Đã hủy",
  RETURNED: "Trả hàng",
};

export const SHIPMENT_STATUS_BADGE_CLASS = {
  PENDING: "bg-amber-100 text-amber-900",
  PICKING_UP: "bg-sky-50 text-sky-900",
  READY_TO_SHIP: "bg-primary/10 text-primary",
  SHIPPED: "bg-sky-100 text-sky-900",
  DELIVERED: "bg-emerald-100 text-emerald-900",
  FAILED: "bg-error-container text-on-error-container",
  CANCELLED: "bg-surface-container-high text-on-surface-variant",
  RETURNED: "bg-surface-container-high text-on-surface-variant",
};

export const MANUAL_NEXT_ACTIONS = {
  PENDING: { status: "READY_TO_SHIP", label: "Sẵn sàng giao" },
  READY_TO_SHIP: { status: "SHIPPED", label: "Đã gửi hàng" },
  SHIPPED: [
    { status: "DELIVERED", label: "Giao thành công" },
    { status: "FAILED", label: "Giao thất bại" },
  ],
};

const GHN_CANCELLABLE_STATUSES = ["PENDING", "PICKING_UP", "READY_TO_SHIP"];
const MANUAL_CANCELLABLE_STATUSES = ["PENDING", "READY_TO_SHIP"];

export function canCancelSellerShipment({ carrier, status } = {}) {
  if (!carrier || !status) return false;
  if (carrier === "GHN") return GHN_CANCELLABLE_STATUSES.includes(status);
  if (carrier === "MANUAL" || carrier === "SELF_DELIVERY") {
    return MANUAL_CANCELLABLE_STATUSES.includes(status);
  }
  return false;
}

export const SELLER_SHIPMENT_ERROR_MESSAGES = {
  "COMMERCE-401": "Phiên đăng nhập không hợp lệ.",
  "COMMERCE-400": "Vui lòng nhập trạng thái hoặc mã vận đơn.",
  "COMMERCE-400-VALIDATION": "Dữ liệu không hợp lệ.",
  "COMMERCE-400-SHIPMENT-CARRIER": "Hãng vận chuyển không hợp lệ.",
  "COMMERCE-400-SHIPMENT-TYPE": "Loại vận chuyển không hợp lệ.",
  "COMMERCE-404-SHIPMENT": "Không tìm thấy vận đơn.",
  "COMMERCE-404-ORDER-ITEM": "Một hoặc nhiều mục đơn không tồn tại.",
  "COMMERCE-404-BUYER-ADDRESS": "Người mua chưa có địa chỉ giao hàng.",
  "COMMERCE-409-ORDER-PROCESSING": "Đơn hàng chưa sẵn sàng xử lý.",
  "COMMERCE-409-ORDER-ITEM-SHIPPED": "Mục đơn đã có vận đơn.",
  "COMMERCE-409-ORDER-ITEM-PROCESS": "Mục đơn chưa ở trạng thái chuẩn bị.",
  "COMMERCE-409-PAYMENT-STATE": "Thanh toán PayOS chưa hoàn tất.",
  "COMMERCE-409-SHIPMENT-STATUS": "Không thể chuyển sang trạng thái này.",
  "COMMERCE-409-SHIPMENT-CARRIER": "Vận đơn GHN cập nhật tự động — không chỉnh tay.",
  "COMMERCE-409-TRACKING": "Mã vận đơn đã được sử dụng.",
  "COMMERCE-409-SELLER-SHOP": "Bạn chưa có cửa hàng.",
};

export function mapSellerShipmentApiError(error) {
  const code = String(error?.code ?? "");
  return SELLER_SHIPMENT_ERROR_MESSAGES[code] || error?.message || "Có lỗi xảy ra. Vui lòng thử lại.";
}
