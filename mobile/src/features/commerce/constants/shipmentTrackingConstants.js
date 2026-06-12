export const SHIPMENT_TYPE_LABELS = {
  STANDARD: "Giao tiêu chuẩn",
  EXPRESS: "Giao nhanh",
  ECONOMY: "Giao tiết kiệm",
};

const TIMELINE_STATUS_LABELS = {
  CREATED: "Đã tạo vận đơn",
  SHIPPED: "Đang giao hàng",
  DELIVERED: "Đã giao hàng",
  CANCELLED: "Đã hủy vận chuyển",
};

export function getShipmentTimelineLabel(event) {
  const next = event.newStatus || event.new_status;
  return TIMELINE_STATUS_LABELS[next] || next || "Cập nhật trạng thái";
}