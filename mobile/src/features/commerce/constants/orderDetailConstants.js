export const ORDER_TRACK_POLL_INTERVAL_MS = 30000;

export const ITEM_STATUS_LABELS = {
  CREATED: "Đã tạo",
  PROCESSING: "Đang xử lý",
  SHIPPED: "Đã gửi hàng",
  DELIVERED: "Đã giao",
  COMPLETED: "Hoàn tất",
  CANCELLED: "Đã hủy",
};

export const SHIPMENT_STATUS_LABELS = {
  CREATED: "Đã tạo vận đơn",
  SHIPPED: "Đang giao",
  DELIVERED: "Đã giao",
  CANCELLED: "Đã hủy",
};

export function getTimelineTransitionLabel(event) {
  const next = event.newStatus || event.new_status;
  const note = event.note;

  const byStatus = {
    CREATED: "Đơn hàng được tạo",
    AWAITING_PAYMENT: "Chờ thanh toán",
    PROCESSING: "Đang xử lý đơn hàng",
    COMPLETED: "Đơn hàng hoàn thành",
    CANCELLED: "Đơn hàng đã hủy",
    PENDING: "Khởi tạo thanh toán",
    PAID: "Thanh toán thành công",
    FAILED: "Thanh toán thất bại",
    SHIPPED: "Đã gửi hàng",
    DELIVERED: "Đã giao hàng",
  };

  if (note) return note;
  return byStatus[next] || next || "Cập nhật trạng thái";
}